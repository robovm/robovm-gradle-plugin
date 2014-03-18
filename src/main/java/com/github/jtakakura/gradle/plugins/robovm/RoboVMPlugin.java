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
package com.github.jtakakura.gradle.plugins.robovm;

import com.github.jtakakura.gradle.tasks.robovm.CreateIPATask;
import com.github.jtakakura.gradle.tasks.robovm.IOSDeviceTask;
import com.github.jtakakura.gradle.tasks.robovm.IPadSimulatorTask;
import com.github.jtakakura.gradle.tasks.robovm.IPhoneSimulatorTask;
import java.util.HashMap;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.robovm.compiler.Version;

/**
 * Gradle plugin that extends the Java plugin for RoboVM development.
 *
 * @author Junji Takakura
 */
public class RoboVMPlugin implements Plugin<Project> {

    public static String getRoboVMVersion() {
        return Version.getVersion();
    }

    @Override
    public void apply(Project project) {
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
    }
}
