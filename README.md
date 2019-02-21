# Android Bootstrap


.             |  .
:-------------------------:|:-------------------------:
![](https://cdn-images-1.medium.com/max/1600/1*sxTblN6YkLnGSoNjuEfGaw.gif)  |  ![](https://cdn-images-1.medium.com/max/1600/1*5tbALvA4vm5S00g7TMsQ6A.gif)

This repository is a collection of modules intending to help bootstrap an Android Application.
There are 4 modules:

1. Core: A module that hosts classes related to ```Activities```, ```Fragments``` and other ```Android``` core components.
2. View: A module containing UI building blocks, like animators and helper methods for views.
3. RecyclerView: Utility classes for the ```RecyclerView``` ```ViewGroup``` like drag and drop, swipe gestures, endless scrolling and much more.
4. ConstraintLayout: Utility classes for the ```ConstraintLayout``` ```ViewGroup```.
5. Material: Utility classes around Google's Material Design components including an expandable floating action button.
6. Communications: Utility classes for Near Service Discovery (NSD) and Bluetooth Low Energy (BLE) communication.
7. Test: Testing utilities built mostly around Espresso.

build.gradle lines

    implementation 'com.tunjid.android-bootstrap:constraintlayout:5.0.0-alpha2'
    implementation 'com.tunjid.android-bootstrap:communications:5.0.0-alpha2'
    implementation 'com.tunjid.android-bootstrap:recyclerview:5.0.0-alpha2'
    implementation 'com.tunjid.android-bootstrap:functions:1.0.3-alpha2'
    implementation 'com.tunjid.android-bootstrap:material:5.0.0-alpha2'
    implementation 'com.tunjid.android-bootstrap:core:5.0.0-alpha2'
    implementation 'com.tunjid.android-bootstrap:view:5.0.0-alpha2'
    implementation 'com.tunjid.android-bootstrap:test:5.0.0-alpha2'


Projects that use This library include:

1. [DigiLux Fingerprint gestures app](https://play.google.com/store/apps/details?id=com.tunjid.fingergestures)
2. [Teammate Sports Management and Tournament Bracket App](https://play.google.com/store/apps/details?id=com.mainstreetcode.teammate)
3. [BluetoothRcSwitch IOT Github project](https://github.com/tunjid/BluetoothRcSwitch)

## Core
Read more about the core module, classes and components [here](https://github.com/tunjid/android-bootstrap/blob/master/core/README.md).

## View
Read more about the view module, classes and components [here](https://github.com/tunjid/android-bootstrap/blob/master/view/README.md).

## RecyclerView
Read more about the RecyclerView module, classes and components [here](https://github.com/tunjid/android-bootstrap/blob/master/recyclerview/README.md).

## ConstraintLayout
Read more about the ConstraintLayout module, classes and components [here](https://github.com/tunjid/android-bootstrap/blob/master/constraintlayout/README.md).

## Material
Read more about the Material module, classes and components [here](https://github.com/tunjid/android-bootstrap/blob/master/material/README.md).

## Communications
Read more about the communications module, classes and components [here](https://github.com/tunjid/android-bootstrap/blob/master/communications/README.md).

## Test
Warning, here be dragons, I haven't updated this in a bit; move fast, break things and all that.
I mean, yeah I could test more, but thank Heavens for QA, amirite? 🙃
You can read more about the testing module and classes [here](https://github.com/tunjid/android-bootstrap/blob/master/test/README.md).
