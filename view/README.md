# View

Utility methods for Android ```Views```.

## [Delegates](https://kotlinlang.org/docs/reference/delegation.html)

Delegates for reading and writing arbitrary properties for a `View`.

Delegates include:

1. `viewDelegate`: Read/write. Similar to the standard library `by map()`, lets you read/write properties from/to a `View` instance.
    Usage:
        ```
        var ImageView.imageUrl by viewDelegate<String?>()
        ```
2. viewBindingDelegate: Read/write. Similar to the standard library `by map()`, lets you read/write properties from/to a `ViewBinding` instance.
    Usage:
        ```
        var UserBinding.user by viewDelegate<User?>()
        ```

## ViewHider

A utility class for quickly implementing the quick return pattern for ```View```'s that come into the screen and leave.
Useful for Hiding ```Toolbars``` and ```FloatingActionButton```s.
It uses the SpringAnimation from the Jetpack dynamic animations library.

## InsetFlags

A utility class for consuming ```WindowInsets``` if you decide to.
They describe what insets to consume for whatever class that uses them.

## View.popOver

A utility method for popping any arbitrary ```View``` over an anchor View

## View.innermostFocusedChild

A utility method that returns the deepest nested ```View``` that has focus
