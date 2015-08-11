/*
 * Copyright (C) 2014 RoboVM AB.
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

import java.io.File;

import org.gradle.api.GradleException;
import org.robovm.compiler.AppCompiler;
import org.robovm.compiler.config.Arch;
import org.robovm.compiler.config.Config;
import org.robovm.compiler.config.OS;
import org.robovm.compiler.target.ios.DeviceType;
import org.robovm.compiler.target.ios.IOSSimulatorLaunchParameters;
import org.robovm.compiler.target.ios.IOSTarget;

/**
 *
 * @author Junji Takakura
 */
abstract public class AbstractIOSSimulatorTask extends AbstractRoboVMTask {
    protected void launch(DeviceType type) {
        try {
            AppCompiler compiler = build(OS.ios, getArch(), IOSTarget.TYPE);
            Config config = compiler.getConfig();

            IOSSimulatorLaunchParameters launchParameters = (IOSSimulatorLaunchParameters) config.getTarget().createLaunchParameters();
            launchParameters.setDeviceType(type);

            if (extension.getStdoutFifo() != null) {
                File stdoutFifo = new File(extension.getStdoutFifo());
                boolean isWritable;

                if (stdoutFifo.exists()) {
                    isWritable = stdoutFifo.isFile() && stdoutFifo.canWrite();
                } else {
                    File parent = stdoutFifo.getParentFile();
                    isWritable = parent != null && parent.isDirectory() && parent.canWrite();
                }

                if (!isWritable) {
                    throw new GradleException("Unwritable 'stdoutFifo' specified for RoboVM compile: " + stdoutFifo);
                }

                launchParameters.setStdoutFifo(stdoutFifo);
            }

            if (extension.getStderrFifo() != null) {
                File stderrFifo = new File(extension.getStderrFifo());
                boolean isWritable;

                if (stderrFifo.exists()) {
                    isWritable = stderrFifo.isFile() && stderrFifo.canWrite();
                } else {
                    File parent = stderrFifo.getParentFile();
                    isWritable = parent != null && parent.isDirectory() && parent.canWrite();
                }

                if (!isWritable) {
                    throw new GradleException("Unwritable 'stderrFifo' specified for RoboVM compile: " + stderrFifo);
                }

                launchParameters.setStderrFifo(stderrFifo);
            }

            compiler.launch(launchParameters);
        } catch (Throwable t) {
            throw new GradleException("Failed to launch IOS Simulator", t);
        }
    }

    protected Arch getArch() {
        Arch arch = Arch.x86_64;
        if (extension.getArch() != null && extension.getArch().equals(Arch.x86.toString())) {
            arch = Arch.x86;
        }
        return arch;
    }

    protected Arch getTaskArch(String archIn) {
        Arch arch = Arch.x86_64;
        if (archIn != null && archIn.equals(Arch.x86.toString())) {
            arch = Arch.x86;
        }
        return arch;
    }

    protected String getProjectOrLocal(String propertyName) {
        if (hasProperty(propertyName)) {
            return (String )property(propertyName);
        } else {
            return (String) project.getProperties().get(propertyName);
        }
    }

    protected DeviceType getDeviceType(DeviceType.DeviceFamily family) {

        // Prefer the task properties over project ones, so the concrete simulator tasks can be subclasses with overriden properties
        String DEVICE_NAME = "robovm.device.name";
        String SDK_VERSION = "robovm.sdk.version";
        String ARCH = "robovm.arch";
        String deviceName;
        String sdkVersion;
        Arch arch;
        deviceName = getProjectOrLocal(DEVICE_NAME);
        sdkVersion = getProjectOrLocal(SDK_VERSION);
        if (hasProperty(ARCH)) {
            arch = getTaskArch((String) property(ARCH));
        } else {
            arch = getArch();
        }
        return DeviceType.getBestDeviceType(arch, family, deviceName, sdkVersion);
    }

}
