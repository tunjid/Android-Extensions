# Core

## Abstract Classes

### BaseActivity

An activity that delegates all visible UI to an instance of a BaseFragment except global items like a ToolBar or Floating Action Button.
The Fragments are managed by a `FragmentStateManager`, therefore all fragment's shown are automatically added to the FragmentManger's back stack.
It also provides convenience methods to show a fragment and find the currently visible fragment.

### BaseFragment

A fragment that provides a mechanism to provide deterministic stable fragment tags as soon as an instance is created instead of providing it in a fragment transaction.
It also lets you provide a fragment transaction for a new fragment that is coming into the `BaseActivity`. This is especially useful for shared element transitions.
As an added convenience, it implements `BackPressInterceptor` which lets it override the back pressed action in it's hosting `BaseActivity`.

## Components

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

## Text

### Spanbuilder

A class for creating and cocatenating a variety of [Spannable](https://developer.android.com/reference/android/text/Spannable.html) items.
