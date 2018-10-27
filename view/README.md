# View

## FabExtensionAnimator

A utility class for extending and collapsing a ```MaterialButton``` to a ```FloatingActionButton``` and vice versa.
It exposes APIs to allow the customization of certain attributes.

## ViewPagerIndicatorAnimator

A utility class for adding Indicators to a ```ViewPager```, with the caveat that the indicators are housed in a ```ConstraintLayout```.
APIs are exposed for manipulating the indicators created.

## ViewHider

A utility class for quickly implementing the quick return pattern for ```View```'s that come into the screen and leave.
Useful for Hiding ```Toolbars``` and ```FloatingActionButton```s.

## BottomTransientBarBehavior

A utility for animating views above any BottomTransientBar (```SnackBars``` and the like) that appear within a ```CoordinatorLayout```.

## InteractiveAdapter

A ```RecyclerViewAdapter``` that hosts a ```ViewHolder``` with a typed interaction listener;
implementations could be ClickListeners, LongClickListeners, both combined or what have you.

## InsetFlags

A utility class for consuming ```WindowInsets``` if you decide to.
They describe what insets to consume for whatever class that uses them.

## ViewUtil

Static methods for common ```View``` interactions.
