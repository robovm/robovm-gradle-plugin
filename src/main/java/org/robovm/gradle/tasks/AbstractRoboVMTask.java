/*
 * Copyright (C) 2014 Trillian Mobile AB.
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
package org.robovm.gradle.tasks;

import org.robovm.gradle.RoboVMPlugin;
import org.robovm.gradle.RoboVMPluginExtension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.gradle.mvn3.org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.gradle.mvn3.org.apache.maven.repository.internal.MavenServiceLocator;
import org.gradle.mvn3.org.apache.maven.wagon.Wagon;
import org.gradle.mvn3.org.apache.maven.wagon.providers.http.HttpWagon;
import org.gradle.mvn3.org.sonatype.aether.RepositorySystem;
import org.gradle.mvn3.org.sonatype.aether.RepositorySystemSession;
import org.gradle.mvn3.org.sonatype.aether.connector.wagon.WagonProvider;
import org.gradle.mvn3.org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.gradle.mvn3.org.sonatype.aether.repository.LocalRepository;
import org.gradle.mvn3.org.sonatype.aether.repository.RemoteRepository;
import org.gradle.mvn3.org.sonatype.aether.resolution.ArtifactRequest;
import org.gradle.mvn3.org.sonatype.aether.resolution.ArtifactResolutionException;
import org.gradle.mvn3.org.sonatype.aether.resolution.ArtifactResult;
import org.gradle.mvn3.org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.gradle.mvn3.org.sonatype.aether.util.artifact.DefaultArtifact;
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

        Config.Builder builder;
        try {
            builder = new Config.Builder();
        } catch (IOException e) {
            throw new GradleException(e.getMessage(), e);
        }

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
        try {
            FileUtils.deleteDirectory(temporaryDirectory);
        } catch (IOException e) {
            throw new GradleException("Failed to clean output dir " + temporaryDirectory, e);
        }
        temporaryDirectory.mkdirs();

        builder.home(new Config.Home(unpack()))
                .logger(getRoboVMLogger())
                .tmpDir(temporaryDirectory)
                .targetType(targetType)
                .skipInstall(skipInstall)
                .installDir(installDirectory)
                .os(os)
                .arch(arch);

        if (project.hasProperty("mainClassName")) {
            builder.mainClass((String) project.property("mainClassName"));
        }

        if (extension.isIosSkipSigning()) {
            builder.iosSkipSigning(true);
        } else {
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
        final File distTarFile = resolveArtifact("org.robovm:robovm-dist:tar.gz:nocompiler:" + RoboVMPlugin.getRoboVMVersion());
        final File unpackedDirectory = new File(distTarFile.getParent(), "unpacked");
        final File unpackedDistDirectory = new File(unpackedDirectory, "robovm-" + RoboVMPlugin.getRoboVMVersion());

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
        MavenServiceLocator locator = new MavenServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);
        locator.setService(WagonProvider.class, ManualWagonProvider.class);

        return locator.getService(RepositorySystem.class);
    }

    private RepositorySystemSession createRepositorySystemSession() {
        LocalRepository localRepository = new LocalRepository(System.getProperty("user.home") + "/.m2/repository");
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();
        session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(localRepository));

        return session;
    }

    private List<RemoteRepository> createRemoteRepositories() {
        List<RemoteRepository> repositories = new ArrayList<>();
        repositories.add(new RemoteRepository("maven-central", "default", "http://repo1.maven.org/maven2/"));
        repositories.add(new RemoteRepository("oss.sonatype.org-snapshots", "default", "https://oss.sonatype.org/content/repositories/snapshots/"));

        return repositories;
    }

    public static class ManualWagonProvider implements WagonProvider {

        @Override
        public Wagon lookup(String roleHint) throws Exception {
            if ("http".equals(roleHint) || "https".equals(roleHint)) {
                return new HttpWagon();
            }

            return null;
        }

        @Override
        public void release(Wagon wagon) {
            // noop
        }
    }
}
