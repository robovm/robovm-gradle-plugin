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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.GradleException;
import org.robovm.compiler.AppCompiler;
import org.robovm.compiler.config.Arch;
import org.robovm.compiler.config.Config;
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
            Config.Builder builder = configure(new Config.Builder())
                    .skipInstall(false)
                    .os(OS.ios)
                    .targetType(IOSTarget.TYPE);

            List<Arch> archs = new ArrayList<>();
            String ipaArchs = extension.getIpaArchs();
            if (ipaArchs == null || ipaArchs.trim().isEmpty()) {
                archs.add(Arch.thumbv7);
            } else {
                for (String s : ipaArchs.trim().split(":")) {
                    archs.add(Arch.valueOf(s));
                }
            }
            
            AppCompiler compiler = new AppCompiler(builder.build());
            compiler.createIpa(archs);
        } catch (IOException e) {
            throw new GradleException("Failed to create IPA", e);
        }
    }
}
