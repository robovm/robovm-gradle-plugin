/*
 * Copyright (C) 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jtakakura.gradle.tasks.robovm;

import com.github.jtakakura.gradle.plugins.robovm.RoboVMPlugin;
import com.github.jtakakura.gradle.plugins.robovm.RoboVMPluginExtension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.wagon.Wagon;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.connector.wagon.WagonProvider;
import org.eclipse.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.impl.VersionResolver;
import org.eclipse.aether.impl.DependencyCollector;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.internal.impl.DefaultDependencyCollector;
import org.eclipse.aether.internal.impl.DefaultTransporterProvider;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository.Builder;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.spi.connector.transport.TransporterProvider;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.transport.wagon.WagonTransporterFactory;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

import org.robovm.compiler.AppCompiler;
import org.robovm.compiler.config.Arch;
import org.robovm.compiler.config.Config;
import org.robovm.compiler.config.Config.TargetType;
import org.robovm.compiler.config.OS;
import org.robovm.compiler.log.Logger;
import org.robovm.compiler.target.ios.ProvisioningProfile;
import org.robovm.compiler.target.ios.SigningIdentity;

/**
 *
 * @author Junji Takakura
 */
abstract public class AbstractRoboVMTask extends DefaultTask {

    protected final Project project;
    protected final RoboVMPluginExtension extension;
    protected final RepositorySystem repositorySystem;
    protected final RepositorySystemSession repositorySystemSession;
    protected final List<RemoteRepository> remoteRepositories;
    protected Logger roboVMLogger;

    public AbstractRoboVMTask() {
        project = getProject();
        extension = (RoboVMPluginExtension) project.getExtensions().getByName(RoboVMPluginExtension.NAME);
        repositorySystem = createRepositorySystem();
        repositorySystemSession = createRepositorySystemSession();
        remoteRepositories = createRemoteRepositories();
    }

    public Config build(OS os, Arch arch, TargetType targetType) {
        return build(os, arch, targetType, false);
    }

    public Config build(OS os, Arch arch, TargetType targetType, boolean skipInstall) {
        getLogger().info("Building RoboVM app for: " + os + " (" + arch + ")");

        Config.Builder builder = new Config.Builder();

        if (!project.hasProperty("mainClassName")) {
            throw new GradleException("No main class specified");
        }

        String mainClass = (String) project.property("mainClassName");

        if (extension.getPropertiesFile() != null) {
            File propertiesFile = new File(extension.getPropertiesFile());

            if (!propertiesFile.exists()) {
                throw new GradleException("Invalid 'propertiesFile' specified for RoboVM compile: " + propertiesFile);
            }
            try {
                getLogger().debug("Including properties file in RoboVM compiler config: " + propertiesFile.getAbsolutePath());
                builder.addProperties(propertiesFile);
            } catch (IOException e) {
                throw new GradleException("Failed to add properties file to RoboVM config: " + propertiesFile);
            }
        } else {
            File file = new File(project.getProjectDir(), "robovm.properties");

            if (file.exists()) {
                getLogger().debug("Using default properties file: " + file.getAbsolutePath());

                try {
                    builder.addProperties(file);
                } catch (IOException e) {
                    throw new GradleException("Failed to add properties file to RoboVM config: " + file, e);
                }
            }
        }

        if (extension.getConfigFile() != null) {
            File configFile = new File(extension.getConfigFile());

            if (!configFile.exists()) {
                throw new GradleException("Invalid 'configFile' specified for RoboVM compile: " + configFile);
            }
            try {
                getLogger().debug("Loading config file for RoboVM compiler: " + configFile.getAbsolutePath());
                builder.read(configFile);
            } catch (Exception e) {
                throw new GradleException("Failed to read RoboVM config file: " + configFile);
            }
        } else {
            File file = new File(project.getProjectDir(), "robovm.xml");

            if (file.exists()) {
                getLogger().debug("Using default config file: " + file.getAbsolutePath());
                try {
                    builder.read(file);
                } catch (Exception e) {
                    throw new GradleException("Failed to read RoboVM config file: " + file, e);
                }
            }
        }

        File installDirectory = new File(project.getBuildDir(), "robovm");
        File temporaryDirectory = new File(new File(installDirectory, os.name()), arch.name());

        builder.home(new Config.Home(unpack()))
                .logger(getRoboVMLogger())
                .tmpDir(temporaryDirectory)
                .targetType(targetType)
                .skipInstall(skipInstall)
                .installDir(installDirectory)
                .mainClass(mainClass)
                .os(os)
                .arch(arch);

        if (extension.getIosSignIdentity() != null) {
            String iosSignIdentity = extension.getIosSignIdentity();

            getLogger().debug("Using explicit iOS Signing identity: " + iosSignIdentity);
            builder.iosSignIdentity(SigningIdentity.find(SigningIdentity.list(), iosSignIdentity));
        }

        if (extension.getIosProvisioningProfile() != null) {
            String iosProvisioningProfile = extension.getIosProvisioningProfile();

            getLogger().debug("Using explicit iOS provisioning profile: " + iosProvisioningProfile);
            builder.iosProvisioningProfile(ProvisioningProfile.find(ProvisioningProfile.list(), iosProvisioningProfile));
        }

        builder.clearClasspathEntries();

        // configure the runtime classpath
        Set<File> classpathEntries = project.getConfigurations().getByName("runtime").getFiles();
        classpathEntries.add(new File(project.getBuildDir(), "classes/main"));

        if (project.hasProperty("output.classesDir")) {
            classpathEntries.add((File) project.property("output.classesDir"));
        }

        for (File classpathEntry : classpathEntries) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Including classpath element for RoboVM app: " + classpathEntry.getAbsolutePath());
            }

            builder.addClasspathEntry(classpathEntry);
        }

        // execute the RoboVM build
        Config config;

        try {
            getLogger().info("Compiling RoboVM app, this could take a while, especially the first time round");
            config = builder.build();
            AppCompiler compiler = new AppCompiler(config);
            compiler.compile();
            getLogger().info("Compile RoboVM app completed.");
        } catch (IOException e) {
            throw new GradleException("Error building RoboVM executable for app", e);
        }

        return config;
    }

    @TaskAction
    abstract public void invoke();

    protected File unpack() throws GradleException {
        final File distTarFile = resolveArtifact("org.robovm:robovm-dist:tar.gz:nocompiler:" + RoboVMPlugin.ROBO_VM_VERSION);
        final File unpackedDirectory = new File(distTarFile.getParent(), "unpacked");
        final File unpackedDistDirectory = new File(unpackedDirectory, "robovm-" + RoboVMPlugin.ROBO_VM_VERSION);

        if (unpackedDirectory.exists()) {
            getLogger().debug("Archive '" + distTarFile + "' was already unpacked in: " + unpackedDirectory);
        } else {
            getLogger().info("Extracting '" + distTarFile + "' to: " + unpackedDirectory);

            if (!unpackedDirectory.mkdirs()) {
                throw new GradleException("Unable to create base directory to unpack into: " + unpackedDirectory);
            }

            getAnt().invokeMethod("untar", new HashMap<String, Object>() {
                {
                    put("src", distTarFile.getAbsolutePath());
                    put("dest", unpackedDirectory.getAbsolutePath());
                    put("compression", "gzip");
                }
            });

            if (!unpackedDistDirectory.exists()) {
                throw new GradleException("Unable to unpack archive");
            }

            getLogger().debug("Archive '" + distTarFile + "' unpacked to: " + unpackedDirectory);
        }

        getAnt().invokeMethod("chmod", new HashMap<String, Object>() {
            {
                put("dir", new File(unpackedDistDirectory, "bin").getAbsoluteFile());
                put("perm", "+x");
                put("includes", "*");
            }
        });

        return unpackedDistDirectory;
    }

    protected File resolveArtifact(String artifactLocator) throws GradleException {
        ArtifactRequest request = new ArtifactRequest();
        DefaultArtifact artifact = new DefaultArtifact(artifactLocator);
        request.setArtifact(artifact);
        request.setRepositories(remoteRepositories);

        getLogger().debug("Resolving artifact " + artifact + " from " + remoteRepositories);

        ArtifactResult result;

        try {
            result = repositorySystem.resolveArtifact(repositorySystemSession, request);
        } catch (ArtifactResolutionException e) {
            throw new GradleException(e.getMessage(), e);
        }

        getLogger().debug("Resolved artifact " + artifact + " to " + result.getArtifact().getFile() + " from " + result.getRepository());

        return result.getArtifact().getFile();
    }

    protected Logger getRoboVMLogger() {
        if (roboVMLogger == null) {
            roboVMLogger = new Logger() {
                @Override
                public void debug(String s, Object... objects) {
                    getLogger().debug(String.format(s, objects));
                }

                @Override
                public void info(String s, Object... objects) {
                    getLogger().info(String.format(s, objects));
                }

                @Override
                public void warn(String s, Object... objects) {
                    getLogger().warn(String.format(s, objects));
                }

                @Override
                public void error(String s, Object... objects) {
                    getLogger().error(String.format(s, objects));
                }
            };
        }

        return roboVMLogger;
    }

    private RepositorySystem createRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService( RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class );
        locator.addService(VersionResolver.class, DefaultVersionResolver.class);
        locator.addService(TransporterProvider.class, DefaultTransporterProvider.class);
        locator.addService( TransporterFactory.class, HttpTransporterFactory.class );
        locator.addService(DependencyCollector.class, DefaultDependencyCollector.class);

        //MKMK MavenServiceLocator locator = new MavenServiceLocator();
        //MKMK locator.addService( TransporterFactory.class, FileTransporterFactory.class );
        //MKMK locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);
        //MKMK locator.setService(WagonProvider.class, ManualWagonProvider.class);

        RepositorySystem rep_sys = locator.getService(RepositorySystem.class);
        if (repositorySystem == null) {
            System.err.println("Couldn't initialize local maven repository system.");
            System.exit(0);
        }

        return rep_sys;
    }

    private RepositorySystemSession createRepositorySystemSession() {
        LocalRepository localRepository = new LocalRepository(System.getProperty("user.home") + "/.m2/repository");
        // MavenRepositorySystemSession session = new MavenRepositorySystemSession();
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        System.err.println("session: "+session+", localrepo: "+localRepository+", reposys: "+repositorySystem);
        LocalRepositoryManager repoman = repositorySystem.newLocalRepositoryManager(session, localRepository);
        System.err.println("repoman: "+repoman);
        session.setLocalRepositoryManager(repoman);

        return session;
    }

    private List<RemoteRepository> createRemoteRepositories() {
        List<RemoteRepository> repositories = new ArrayList<RemoteRepository>();
        //MKMKrepositories.add(new RemoteRepository("maven-central", "default", "http://repo1.maven.org/maven2/"));
        repositories.add((new RemoteRepository.Builder("maven-central", "default", "http://repo1.maven.org/maven2/")).build());

        return repositories;
    }

    // MKMK Commented out code below; probably/hopefully obsolete.
    // public static class ManualWagonProvider implements WagonProvider {
    //     @Override
    //     public Wagon lookup(String roleHint) throws Exception {
    //         if ("http".equals(roleHint)) {
    //             return new HttpWagon();
    //         }

    //         return null;
    //     }

    //     @Override
    //     public void release(Wagon wagon) {
    //         // noop
    //     }
    // }
}
