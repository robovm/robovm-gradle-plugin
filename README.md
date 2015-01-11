RoboVM Gradle plugin
====================

# Usage
To use the RoboVM plugin, include in your build script:

```groovy
// Pull the plugin from Maven Central
buildscript {
    repositories {
        mavenCentral()
        maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
    }
    dependencies {
        classpath 'org.robovm:robovm-gradle-plugin:1.0.0-beta-01'
    }
}

// Apply the plugin
apply plugin: 'robovm'

repositories {
    mavenCentral()
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
}

ext {
    // Configure your application main class
    mainClassName = "org.robovm.sample.ios.RoboVMSampleIOSApp"
    roboVMVersion = "1.0.0-beta-01"
}

robovm {
    // Configure robovm
    iosSignIdentity = ""
    iosProvisioningProfile = ""
    iosSkipSigning = false
    stdoutFifo = ""
    stderrFifo = ""
}
```

## Tasks

The RoboVM plugin defines the following tasks:

* `launchIPhoneSimulator`: Runs Your iOS App in the iPhone Simulator.
* `launchIPadSimulator`: Runs Your iOS App in the iPad Simulator.
* `launchIOSDevice`: Runs Your iOS App in the iOS Device.
* `createIPA`: Creates .ipa file.

## Project properties

The iOS Simulator launcher properties can be set by project properties via `gradle.properties` or `-P` command line parameter:

* `robovm.device.name`: Set the device name property.
 * iPhone-4s: iPhone 4S
 * iPhone-5: iPhone 5
 * iPhone-5s: iPhone 5S
 * iPhone-6: iPhone 6
 * iPhone-6-Plus: iPhone 6 Plus
 * iPad-2: iPad 2
 * iPad-Retina: iPad Retina
 * iPad-Air: iPad Air
 * Resizable-iPhone: Resizable iPhone
 * Resizable-iPad: Resizable iPad
* `robovm.sdk.version`: Set the sdk version property.

The arch can be specified using the `gradle.properties` or `-P` command line parameter. To launch on the simulator in 64-bit mode use:

```
gradle -Probovm.arch=x86_64 -Probovm.device.name=iPhone-5s launchIPhoneSimulator
```

Make sure to specify a 64-bit capable device type to simulate, e.g. `iPhone-5s`.

To launch on device in 64-bit mode:

```
gradle -Probovm.arch=arm64 launchIOSDevice
```

The `createIPA` task will by default just include a 32-bit verison of the app in the IPA. Use the `robovm.ipaArchs` property to specify the archs to include in the IPA:

```
gradle -Probovm.ipaArchs=thumbv7:arm64 createIPA
```
