# Android Extensions

![Navigation Tests](https://github.com/tunjid/Android-Extensions/workflows/Navigation%20Tests/badge.svg)

This repository is a collection of modules to help bootstrap an Android Application.
There are 10 modules:

|   | Name  | Description  |   |   |
| ------------| ------------ | ------------ | ------------ | ------------ |
| 1  | App | A sample app demoing the modules in the repository.  |   |   |
| 2  | [![Core](https://img.shields.io/maven-central/v/com.tunjid.androidx/core/1?label=core)](https://github.com/tunjid/Android-Extensions/blob/develop/core/README.md)  | A few added utilities to the core Android KTX libraries.  Includes extension methods on objects to side step ContextCompat, DrawableCompat, and a fluent SpannableString API | ![](https://i.imgur.com/AFZpZ1K.png)  | ![](https://miro.medium.com/max/4904/1*o4p71Uid8vzAHvHH7W_LjQ.png)  |
| 3  | [![Navigation](https://img.shields.io/maven-central/v/com.tunjid.androidx/navigation/1?label=navigation)](https://github.com/tunjid/Android-Extensions/blob/develop/navigation/README.md)  | Interfaces and implementations of Fragment based Navigators, including single and multiple stacks. The APIs allow for hooking into the raw `FragmentTransactions` that run allow you to customize it to your heart's content. Suspending APIs are also available to preform navigation actions sequentially without having to deal with the asynchrosity of the `FragmentManager`.| ![](https://i.imgur.com/2Ai74xI.png)  | ![](https://cdn-images-1.medium.com/max/1600/1*q1WqvY91CWlmAjdEiwbA_g.gif)  |
| 4  | [![View](https://img.shields.io/maven-central/v/com.tunjid.androidx/view/1?label=view)](https://github.com/tunjid/Android-Extensions/blob/develop/view/README.md)  | A module containing UI building blocks, like animators and helper methods for views.  Built mostly around the `SpringAnimation` from the Jetpack `DynamicAnimation` library. In the example to the right, it is responsible for animating the margin, and padding of the container views, and the hiding and showing of bouncing FABs.| ![](https://i.imgur.com/K3qGDKb.gif)  |   |
| 5  | [![RecyclerView](https://img.shields.io/maven-central/v/com.tunjid.androidx/recyclerview/1?label=recyclerview)](https://github.com/tunjid/Android-Extensions/blob/develop/recyclerview/README.md)  | Utility classes for the ```RecyclerView``` ```ViewGroup``` like drag and drop, swipe gestures, endless scrolling, a composable adapter, diffing, tables and much more.  | ![](https://miro.medium.com/max/580/1*SjjLx1ghigvJP7kax-K6gA.gif)  | ![](https://i.imgur.com/hpb3YFu.gif)  |
| 6  | [![ViewPager2](https://img.shields.io/maven-central/v/com.tunjid.androidx/viewpager2/1?label=viewpager2)](https://github.com/tunjid/Android-Extensions/blob/develop/viewpager2/README.md)  | Utility classes for the ```ViewPager2``` ```ViewGroup``` particularly diff util for dispatching updates  | ![](https://i.imgur.com/G1KxP4m.gif)  |   |
| 7  | [![Material](https://img.shields.io/maven-central/v/com.tunjid.androidx/material/1?label=material)](https://github.com/tunjid/Android-Extensions/blob/develop/material/README.md)  | Utility classes around Google's Material Design components including an expandable floating action button.  | ![](https://miro.medium.com/max/648/1*NHgDmR6QVqQwj7VJToQE5w.gif)  |   |
| 8  | [![Communications](https://img.shields.io/maven-central/v/com.tunjid.androidx/communications/1?label=communications)](https://github.com/tunjid/Android-Extensions/blob/develop/communications/README.md)  | Utility classes for Near Service Discovery (NSD) and Bluetooth Low Energy (BLE) communication.  |   |   |
| 9  | [![Saved State](https://img.shields.io/maven-central/v/com.tunjid.androidx/savedstate/1?label=savedstate)](https://github.com/tunjid/Android-Extensions/blob/develop/savedstate/README.md)  | Delegated implementation of Android Jetpack's Saved State Registry for Components that also have a lifecycle.  |   |   |
| 10  | [ConstraintLayout (Unmaintained, MotionLayout is here)](https://github.com/tunjid/Android-Extensions/blob/develop/constraintlayout/README.md)  | Utility classes for the ```ConstraintLayout``` ```ViewGroup```.  |   |   |
| 11  | [Test (Unmaintained)](https://github.com/tunjid/Android-Extensions/blob/develop/test/README.md)  | Testing utilities built mostly around Espresso.  |   |   |

build.gradle lines

The built artifacts are hosted on Jcenter (pending the looming shutdown) and Maven Central:

```
    allprojects {
        repositories {
            // Pre jcenter shutdown
            Jcenter()
            // Now or post Jcenter shutdown
            mavenCentral()
        }
    }

    implementation 'com.tunjid.androidx:module:x.x.x'
```

Refer to the table above for the latest version information.

If you so desire, you can fork the repo and host the artifacts your self using github packages.
Refer to publishing information below for more details.

Projects that use This library include:

1. [DigiLux Fingerprint gestures app](https://play.google.com/store/apps/details?id=com.tunjid.fingergestures)
2. [BluetoothRcSwitch IOT Github project](https://github.com/tunjid/BluetoothRcSwitch)

## Core
Read more about the core module, classes and components [here](https://github.com/tunjid/Android-Extensions/blob/develop/core/README.md).

A medium post with some of it's offerings can be read [here](https://medium.com/@Tunji_D/i-want-it-all-owning-the-system-window-and-consuming-insets-718b7e19960)
                                                             and [here](https://medium.com/@Tunji_D/concatenating-arbitrary-text-spans-in-android-90305ebb8e9b) .

## View
Read more about the view module, classes and components [here](https://github.com/tunjid/Android-Extensions/blob/develop/view/README.md).
A medium post with some of it's offerings can be read [here](https://proandroiddev.com/creating-an-expandable-floating-action-button-in-android-6626b968559e).

## RecyclerView
Read more about the RecyclerView module, classes and components [here](https://github.com/tunjid/Android-Extensions/blob/develop/recyclerview/README.md).
A medium post with some of it's offerings can be read [here](https://medium.com/@Tunji_D/composing-attributes-of-a-dynamic-recyclerview-with-functions-300064990bd4).

## ConstraintLayout
Read more about the ConstraintLayout module, classes and components [here](https://github.com/tunjid/Android-Extensions/blob/develop/constraintlayout/README.md).
A medium post with some of it's offerings can be read [here](https://proandroiddev.com/sliding-along-composing-a-dynamic-reusable-viewpager-indicator-animator-f7c46d559a21).

## Material
Read more about the Material module, classes and components [here](https://github.com/tunjid/Android-Extensions/blob/develop/material/README.md).
A medium post with some of it's offerings can be read [here](https://proandroiddev.com/creating-an-expandable-floating-action-button-in-android-6626b968559e).

## Communications
Read more about the communications module, classes and components [here](https://github.com/tunjid/Android-Extensions/blob/develop/communications/README.md).
A project using it can be seen [here](https://github.com/tunjid/BluetoothRcSwitch).

## Test
Warning, here be dragons, I haven't updated this in a bit; move fast, break things and all that.
I mean, yeah I could test more, but thank Heavens for QA, amirite? 🙃
You can read more about the testing module and classes [here](https://github.com/tunjid/Android-Extensions/blob/develop/test/README.md).

Image attribution
App icon made by [Freepik](https://www.freepik.com/?__hstc=57440181.7a5d7d3cc018b38de5851a6c095932c9.1558869007278.1558869007278.1558869007278.1&__hssc=57440181.5.1558869007279&__hsfp=1983466168 "Freepik") from [www.flaticon.com](https://www.flaticon.com/ "Flaticon") is licensed by [CC 3.0 BY](http://creativecommons.org/licenses/by/3.0/ "Creative Commons BY 3.0"

## Publishing

Publishing is done with the `maven-publish` plugin.

To publish, run:

`./gradlew incrementalPublish`

This will publish the latest version of every module, and will not override existing versions.
The version of the module is determined by the version.properties file. To bump a module version, bump it in version.properties.

Publishing configuration is done by using a `publishInfo.json` file in the project root directory.
A sample `publishInfo.json` looks like:

```
{
  "signArtifacts": true,
  "signingPassword": "mySigningPassword",
  "signingKey": "-----BEGIN PGP PRIVATE KEY BLOCK-----ADD YOUR GPG KEY HERE-----END PGP PRIVATE KEY BLOCK-----\n",
  "versionSuffix": "myVersionSuffix",
  "repositories": [
    {
      "name": "TunjiGithub",
      "publishUrl": "https://maven.pkg.github.com/tunjid/Android-Extensions",
      "downloadUrl": "https://maven.pkg.github.com/tunjid/Android-Extensions",
      "credentials": {
        "username": "tunjid",
        "password": "githubAuthToken"
      }
    },
    {
      "name": "TunjiMavenCentral",
      "publishUrl": "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/",
      "downloadUrl": "https://repo1.maven.org/maven2",
      "credentials": {
        "username": "myMavenCentralUsername",
        "password": "myMavenCentralPassword"
      }
    }
  ]
}
```

`signArtifacts`, `signingPassword`, `signingKey` and `versionSuffix` properties are not necessary and may be omitted;
in fact, I personally only need to sign my artifacts when uploading to maven central.

Note that the repository name *NEEDS* to be camel cased as it's used when finding the gradle task to publish the artifact.

You do however need to specify at least 1 maven repository to publish to if you intend to publish artifacts.

If you intend to host your own version of the artifacts on github packages after forking the repository, you will need to add a secret to your github action pipeline.
The secret key is `PUBLISH_INFO_JSON`, and the value is the `publishInfo.json` described above, substituting the values of `publishUrl` and `downloadUrl`
to match the url of your github fork and the credentials matching your username and github auth token.