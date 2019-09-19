# Android Bootstrap


.             |  .
:-------------------------:|:-------------------------:
![](https://cdn-images-1.medium.com/max/1600/1*sxTblN6YkLnGSoNjuEfGaw.gif)  |  ![](https://cdn-images-1.medium.com/max/1600/1*5tbALvA4vm5S00g7TMsQ6A.gif)

This repository is a collection of modules intending to help bootstrap an Android Application.
There are 4 modules:

1. App: A sample app demoing the modules in the repository. 
2. Core: A module that hosts classes related to ```Activities```, ```Fragments``` and other ```Android``` core components.
3. View: A module containing UI building blocks, like animators and helper methods for views.
4. RecyclerView: Utility classes for the ```RecyclerView``` ```ViewGroup``` like drag and drop, swipe gestures, endless scrolling and much more.
5. ConstraintLayout: Utility classes for the ```ConstraintLayout``` ```ViewGroup```.
6. Material: Utility classes around Google's Material Design components including an expandable floating action button.
7. Communications: Utility classes for Near Service Discovery (NSD) and Bluetooth Low Energy (BLE) communication.
8. Test: Testing utilities built mostly around Espresso.

build.gradle lines

    implementation 'com.tunjid.android-bootstrap:constraintlayout:6.0.0-alpha-02'
    implementation 'com.tunjid.android-bootstrap:communications:6.0.0-alpha-02'
    implementation 'com.tunjid.android-bootstrap:recyclerview:6.0.0-alpha-02'
    implementation 'com.tunjid.android-bootstrap:functions:1.0.4'
    implementation 'com.tunjid.android-bootstrap:material:6.0.0-alpha-02'
    implementation 'com.tunjid.android-bootstrap:core:6.0.0-alpha-02'
    implementation 'com.tunjid.android-bootstrap:view:6.0.0-alpha-02'
    implementation 'com.tunjid.android-bootstrap:test:6.0.0-alpha-02'


Projects that use This library include:

1. [DigiLux Fingerprint gestures app](https://play.google.com/store/apps/details?id=com.tunjid.fingergestures)
2. [Teammate Sports Management and Tournament Bracket App](https://play.google.com/store/apps/details?id=com.mainstreetcode.teammate)
3. [BluetoothRcSwitch IOT Github project](https://github.com/tunjid/BluetoothRcSwitch)

## Core
Read more about the core module, classes and components [here](https://github.com/tunjid/android-bootstrap/blob/master/core/README.md).

A medium post with some of it's offerings can be read [here](https://medium.com/@Tunji_D/i-want-it-all-owning-the-system-window-and-consuming-insets-718b7e19960)
                                                             and [here](https://medium.com/@Tunji_D/concatenating-arbitrary-text-spans-in-android-90305ebb8e9b) .

## View
Read more about the view module, classes and components [here](https://github.com/tunjid/android-bootstrap/blob/master/view/README.md).
A medium post with some of it's offerings can be read [here].

## RecyclerView
Read more about the RecyclerView module, classes and components [here](https://github.com/tunjid/android-bootstrap/blob/master/recyclerview/README.md).
A medium post with some of it's offerings can be read [here](https://medium.com/@Tunji_D/composing-attributes-of-a-dynamic-recyclerview-with-functions-300064990bd4).

## ConstraintLayout
Read more about the ConstraintLayout module, classes and components [here](https://github.com/tunjid/android-bootstrap/blob/master/constraintlayout/README.md).
A medium post with some of it's offerings can be read [here](https://proandroiddev.com/sliding-along-composing-a-dynamic-reusable-viewpager-indicator-animator-f7c46d559a21).

## Material
Read more about the Material module, classes and components [here](https://github.com/tunjid/android-bootstrap/blob/master/material/README.md).
A medium post with some of it's offerings can be read [here](https://proandroiddev.com/creating-an-expandable-floating-action-button-in-android-6626b968559e).

## Communications
Read more about the communications module, classes and components [here](https://github.com/tunjid/android-bootstrap/blob/master/communications/README.md).
A project using it can be seen [here](https://github.com/tunjid/BluetoothRcSwitch).

## Test
Warning, here be dragons, I haven't updated this in a bit; move fast, break things and all that.
I mean, yeah I could test more, but thank Heavens for QA, amirite? 🙃
You can read more about the testing module and classes [here](https://github.com/tunjid/android-bootstrap/blob/master/test/README.md).

Image attribution
App icon made by [Freepik](https://www.freepik.com/?__hstc=57440181.7a5d7d3cc018b38de5851a6c095932c9.1558869007278.1558869007278.1558869007278.1&__hssc=57440181.5.1558869007279&__hsfp=1983466168 "Freepik") from [www.flaticon.com](https://www.flaticon.com/ "Flaticon") is licensed by [CC 3.0 BY](http://creativecommons.org/licenses/by/3.0/ "Creative Commons BY 3.0"
