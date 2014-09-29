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

import org.robovm.compiler.config.Config;
import org.robovm.compiler.config.Config.Home;
import org.robovm.compiler.target.ios.DeviceType;
import org.robovm.compiler.target.ios.DeviceType.DeviceFamily;
import java.util.List;

/**
 *
 * @author Junji Takakura
 */
public class IPhoneSimulatorTask extends AbstractIOSSimulatorTask {

	private Home home;

    @Override
    public void invoke() {
        home = new Config.Home(unpack());
		String device = getDescription();
		if (device != null) {
			invoke(device);
		} else {
			launch(DeviceType.getBestDeviceType(home, DeviceFamily.iPhone));
		}
    }

	public void invoke (String deviceId) {
		List<DeviceType> deviceTypes = DeviceType.listDeviceTypes(home);
		for (DeviceType d : deviceTypes) {
			if (d.getDeviceName().endsWith(deviceId)){
				launch(d);
			}
		}
	}
}
