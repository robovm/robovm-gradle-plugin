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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.gradle.api.GradleException;
import org.robovm.compiler.config.Arch;
import org.robovm.compiler.config.Config;
import org.robovm.compiler.config.Config.TargetType;
import org.robovm.compiler.config.OS;
import org.robovm.compiler.target.ios.DeviceType;
import org.robovm.compiler.target.ios.IOSSimulatorLaunchParameters;
import org.robovm.compiler.target.ios.SDK;

/**
 *
 * @author Junji Takakura
 */
abstract public class AbstractIOSSimulatorTask extends AbstractRoboVMTask {

    protected void launch(DeviceType type) {
        try {
            Config config = build(OS.ios, Arch.x86, TargetType.ios);

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

            config.getTarget().launch(launchParameters).waitFor();
        } catch (InterruptedException | IOException e) {
            throw new GradleException("Failed to launch IOS Simulator", e);
        }
    }

    protected DeviceType getDeviceType(Config.Home home, DeviceType.DeviceFamily family) {
        DeviceType deviceType;

        if (project.hasProperty("robovm.device.name") || project.hasProperty("robovm.sdk.version")) {
            String deviceName = getDeviceName(home, family);
            String sdkVersion = getSDKVersion();
            String deviceTypeId = deviceName + ", " + sdkVersion;

            deviceType = DeviceType.getDeviceType(home, deviceTypeId);

            if (deviceType == null) {
                throw new GradleException("Specified robovm.device.name and robovm.sdk.version are invalid: " + deviceTypeId);
            }
        } else {
            deviceType = DeviceType.getBestDeviceType(home, family);
        }

        return deviceType;
    }

    private String getDeviceName(Config.Home home, DeviceType.DeviceFamily family) {
        String deviceName = null;
        List<DeviceType> deviceTypes = DeviceType.listDeviceTypes(home);

        if (deviceTypes.size() > 0) {
            if (project.hasProperty("robovm.device.name")) {
                String name = (String) project.getProperties().get("robovm.device.name");

                for (DeviceType deviceType : deviceTypes) {
                    if (deviceType.getSimpleDeviceName().equals(name) && deviceType.getFamily().equals(family)) {
                        deviceName = deviceType.getSimpleDeviceName();
                        break;
                    }
                }

                if (deviceName == null) {
                    throw new GradleException("Specified robovm.device.name is invalid: " + name);
                }
            } else {
                deviceName = deviceTypes.get(0).getSimpleDeviceName();
            }
        }

        return deviceName;
    }

    private String getSDKVersion() {
        String sdkVersion = null;
        List<SDK> sdks = SDK.listSimulatorSDKs();

        if (sdks.size() > 0) {
            if (project.hasProperty("robovm.sdk.version")) {
                String version = (String) project.getProperties().get("robovm.sdk.version");

                for (SDK sdk : sdks) {
                    if (sdk.getVersion().equals(version)) {
                        sdkVersion = version;
                        break;
                    }
                }

                if (sdkVersion == null) {
                    throw new GradleException("Specified robovm.sdk.version is invalid: " + version);
                }
            } else {
                SDK latestSdk = null;

                for (SDK sdk : SDK.listSimulatorSDKs()) {
                    if (latestSdk == null || ((sdk.getMajor() << 8) | (sdk.getMinor())) > ((latestSdk.getMajor() << 8) | (latestSdk.getMinor()))) {
                        latestSdk = sdk;
                    }
                }

                if (latestSdk != null) {
                    sdkVersion = latestSdk.getVersion();
                }
            }
        }

        return sdkVersion;
    }
}
