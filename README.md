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

### FragmentStateManager

This class enforces rules about ```Fragment```s and the container it's in a bid to make working with ```Fragment```s a lot easier.
It's fairly opinionated, but it allows for very predictable ```Fragment``` interactions.

The basic crux of it's operation is that every ```Fragment``` added to a container must be added to the ```Fragment``` back stack, with a deterministic tag.

This way, if during navigation, a ```Fragment``` is requested to be shown, and an identical instance of that ```Fragment``` exists,
that same instance is retrieved from the back stack and placed on top of the stack. This is facilitated with the ```getStableTag()```
method, which usually is implemented via hashing the ```Fragment```'s arguments.

It also helps in preventing the same ```Fragment``` instance being shown twice, one after the other.
If the same ```Fragment``` instance is asked to be shown, the request is simply ignored.

Another benefit is retrieving the current ```Fragment``` in the container, which is extremely useful for shared element transitions.
This is because it allows for the isolation of ```FragmentTransition``` logic from the action that causes or starts them.

If for example clicking a view causes a ```FragmentTransaction``` with a shared element transition, the building of the ```FragmentTransaction``` for the ```Transition``` need not occur
at the click site. In the case of the BaseFragment for example, a ```provideFragmentTransaction(BaseFragment FragmentTo)``` API is exposed,
allowing for the ```Fragment``` leaving to customize the ```FragmentTransaction``` for the ```Fragment``` about to come in, yielding a nice separation of concerns.

### ServiceConnection

A convenience API for interacting with Bound Services in Android, removing a lot of the boilerplate involved with the added benefit of
generic typing.

## View

### FabExtensionAnimator

A utility class for extending and collapsing a ```MaterialButton``` to a ```FloatingActionButton``` and vice versa.
It exposes APIs to allow the customization of certain attributes.

### ViewPagerIndicatorAnimator

A utility class for adding Indicators to a ```ViewPager```, with the caveat that the indicators are housed in a ```ConstraintLayout```.
APIs are exposed for manipulating the indicators created.

### ViewHider

A utility class for quickly implementing the quick return pattern for ```View```'s that come into the screen and leave.
Useful for Hiding ```Toolbars``` and ```FloatingActionButton```s.

### BottomTransientBarBehavior

A utility for animating views above any BottomTransientBar (```SnackBars``` and the like) that appear within a ```CoordinatorLayout```.

### InteractiveAdapter

A ```RecyclerViewAdapter``` that hosts a ```ViewHolder``` with a typed interaction listener;
implementations could be ClickListeners, LongClickListeners, both combined or what have you.

### InsetFlags

A utility class for consuming ```WindowInsets``` if you decide to.
They describe what insets to consume for whatever class that uses them.

### ViewUtil

Static methods for common ```View``` interactions.

## Communications

### Bluetooth

Mostly copies of utilities introduced in newer versions of Android that make Bluetooth low energy a lot easier to work with.

### NSD

Utilities around Android's Near Service Discovery API, I typically use this for my IOT experiments.


## Test

Utilities around Espresso and Android testing.
Warning, here be dragons, I haven't updated this in a bit; move fast, break things and all that.
I mean, yeah I could test more, but thank Heavens for QA, amirite? 🙃
