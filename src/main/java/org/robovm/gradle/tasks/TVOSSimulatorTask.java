/*
 * Copyright (C) 2015 RoboVM AB.
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

import org.robovm.compiler.config.Arch;
import org.robovm.compiler.config.OS;
import org.robovm.compiler.target.ios.DeviceType.DeviceFamily;
import org.robovm.compiler.target.ios.TVOSTarget;

/**
 *
 */
public class TVOSSimulatorTask extends AbstractSimulatorTask {

    @Override
    protected String getTargetType() {
        return TVOSTarget.TYPE;
    }

    @Override
    protected OS getOs() {
        return OS.tvos;
    }

    @Override
    protected Arch getArch() {
        return Arch.x86_64;
    }

    @Override
    public void invoke() {
        launch(getDeviceType(DeviceFamily.AppleTV));
    }

    

}
