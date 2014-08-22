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

import java.io.IOException;
import org.gradle.api.GradleException;
import org.robovm.compiler.config.Arch;
import org.robovm.compiler.config.Config;
import org.robovm.compiler.config.Config.TargetType;
import org.robovm.compiler.config.OS;
import org.robovm.compiler.target.ios.IOSTarget;

/**
 *
 * @author Junji Takakura
 */
public class CreateIPATask extends AbstractRoboVMTask {

    @Override
    public void invoke() {
        try {
            Config config = build(OS.ios, Arch.thumbv7, TargetType.ios, false);
            IOSTarget target = (IOSTarget) config.getTarget();
            target.createIpa();
        } catch (IOException e) {
            throw new GradleException("Failed to create IPA", e);
        }
    }
}
