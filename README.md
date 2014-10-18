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
        classpath 'org.robovm:robovm-gradle-plugin:1.0.0-alpha-04'
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
    roboVMVersion = "1.0.0-alpha-04"
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
