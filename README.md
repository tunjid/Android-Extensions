# Android Bootstrap


.             |  .
:-------------------------:|:-------------------------:
![](https://cdn-images-1.medium.com/max/1600/1*sxTblN6YkLnGSoNjuEfGaw.gif)  |  ![](https://cdn-images-1.medium.com/max/1600/1*5tbALvA4vm5S00g7TMsQ6A.gif)

This repository is a collection of modules intending to help bootstrap an Android Application.
There are 4 modules:

1. Core: A module that hosts classes related to Activities, Fragments and other Android core components.
2. View: A module containing UI building blocks, like animators and helper methods for views.
3. Communications: Utility classes for Near Service Discovery (NSD) and Bluetooth Low Energy (BLE) communication.
4. Test: Testing utilities built mostly around Espresso.

build.gradle lines

    implementation 'com.tunjid.android-bootstrap:core:3.0.1'
    implementation 'com.tunjid.android-bootstrap:communications:3.0.1'
    implementation 'com.tunjid.android-bootstrap:test:3.0.1'
    implementation 'com.tunjid.android-bootstrap:view:3.0.1'


Projects that use This library include:

1. [DigiLux Fingerprint gestures app](https://play.google.com/store/apps/details?id=com.tunjid.fingergestures)
2. [Teammate Sports Management and Tournament Bracket App](https://play.google.com/store/apps/details?id=com.mainstreetcode.teammate)
3. [BluetoothRcSwitch IOT Github project](https://github.com/tunjid/BluetoothRcSwitch)

## Core
Read more about the core module, classes and components [here](https://github.com/tunjid/android-bootstrap/blob/master/core/README.md).

## View
Read more about the view module, classes and components [here](https://github.com/tunjid/android-bootstrap/blob/master/view/README.md).

## Communications
Read more about the communications module, classes and components [here](https://github.com/tunjid/android-bootstrap/blob/master/communications/README.md).

## Test
Warning, here be dragons, I haven't updated this in a bit; move fast, break things and all that.
I mean, yeah I could test more, but thank Heavens for QA, amirite? 🙃 But you can read more about the testing module and classes [here](https://github.com/tunjid/android-bootstrap/blob/master/test/README.md).
