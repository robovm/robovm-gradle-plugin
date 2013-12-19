gradle-robovm-plugin
====================
RoboVM Gradle plugin

# Usage
To use the RoboVM plugin, include in your build script:

```groovy
// Pull the plugin from Maven Central
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.github.jtakakura:gradle-robovm-plugin:0.0.1'
    }
}
// Apply the plugin
apply plugin: 'robovm'

ext {
    // Configure your application main class
    mainClassName = "org.robovm.sample.ios.RoboVMSampleIOSApp"
}
```

## Tasks

The RoboVM plugin defines the following tasks:

* `launchIPhoneSimulator`: Runs Your iOS App in the iPhone Simulator.
* `launchIPadSimulator`: Runs Your iOS App in the iPad Simulator.
* `launchIOSDevice`: Runs Your iOS App in the iOS Device.
* `createIPA`: Creates .ipa file.
