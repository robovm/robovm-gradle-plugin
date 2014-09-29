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
package org.robovm.gradle;

import org.robovm.gradle.tasks.CreateIPATask;
import org.robovm.gradle.tasks.IOSDeviceTask;
import org.robovm.gradle.tasks.IPadSimulatorTask;
import org.robovm.gradle.tasks.IPhoneSimulatorTask;
import java.util.HashMap;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.robovm.compiler.Version;
import java.util.List;
import org.robovm.compiler.target.ios.DeviceType;
import org.robovm.compiler.config.Config.Home;
import org.robovm.gradle.tasks.AbstractRoboVMTask;

/**
 * Gradle plugin that extends the Java plugin for RoboVM development.
 *
 * @author Junji Takakura
 */
public class RoboVMPlugin implements Plugin<Project> {

	private static String iDeviceTypes [] = {
							"iPhone-4s", "iPhone-5", "iPhone-5s",
							"iPad-2", "iPad-Retina", "iPad-Air",
							"iPhone-6-Plus", "iPhone-6", "Resizable-iPhone",
							"Resizable-iPad"
						};

    public static String getRoboVMVersion() {
        return Version.getVersion();
    }

    @Override
    public void apply(final Project project) {
        project.getExtensions().create(RoboVMPluginExtension.NAME, RoboVMPluginExtension.class, project);
        project.task(new HashMap<String, Object>() {
            {
                put("type", IPhoneSimulatorTask.class);
            }
        }, "launchIPhoneSimulator");
        project.task(new HashMap<String, Object>() {
            {
                put("type", IPadSimulatorTask.class);
            }
        }, "launchIPadSimulator");
        project.task(new HashMap<String, Object>() {
            {
                put("type", IOSDeviceTask.class);
            }
        }, "launchIOSDevice");
        project.task(new HashMap<String, Object>() {
            {
                put("type", CreateIPATask.class);
            }
        }, "createIPA");

		for (int i=0; i<iDeviceTypes.length; i++) {
			final String deviceName = iDeviceTypes[i];
			if (deviceName.contains("iPhone")){
				project.task(new HashMap<String, Object>() {
					{
						put("type",IPhoneSimulatorTask.class);
						put("description",deviceName);
					}
				}, "launch" + deviceName + "Simulator");
			} else if (deviceName.contains("iPad")) {
				project.task(new HashMap<String, Object>() {
					{
						put("type",IPadSimulatorTask.class);
						put("description",deviceName);
					}
				}, "launch" + deviceName + "Simulator");
			}
		}
    }
}
