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

import java.io.IOException;
import org.gradle.api.GradleException;
import org.robovm.compiler.config.Arch;
import org.robovm.compiler.config.Config;
import org.robovm.compiler.config.Config.TargetType;
import org.robovm.compiler.config.OS;
import org.robovm.compiler.target.ios.IOSSimulatorLaunchParameters;

/**
 *
 * @author Junji Takakura
 */
abstract public class AbstractIOSSimulatorTask extends AbstractRoboVMTask {

    protected void launch(IOSSimulatorLaunchParameters.Family targetFamily) {
        try {
            Config config = build(OS.ios, Arch.x86, TargetType.ios);
            IOSSimulatorLaunchParameters launchParameters = (IOSSimulatorLaunchParameters) config.getTarget().createLaunchParameters();
            launchParameters.setFamily(targetFamily);
            launchParameters.setSdk(extension.getIOSSimulatorSdk());
            config.getTarget().launch(launchParameters).waitFor();
        } catch (InterruptedException e) {
            throw new GradleException("Failed to launch IOS Simulator", e);
        } catch (IOException e) {
            throw new GradleException("Failed to launch IOS Simulator", e);
        }
    }
}
