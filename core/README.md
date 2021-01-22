# Core

### [Delegates](https://kotlinlang.org/docs/reference/delegation.html)

Delegates include:

1. Bundle delegates: Read/write. Similar to the standard library `by map()`, this delegate takes an arbitrary type <T> maps it to a bundle, and reads and writes properties via that bundle.

    a. `by bundleDelegate`: Delegates to the primitive and `Parcelable` values in a `Bundle`. Usage:
        ```
        var Bundle.isSigningOut by bundleDelegate(false)
        ```

    b. `by intentExtras`: Delegates to the extras in an `Intent` `Bundle`. Usage:
        ```
        var Intent.isSigningOut by intentDelegate(false)
        ```

    c. `by activityIntent`: Delegates to the extras in the `Activity` `Intent` `Bundle`. Usage:
        ```
        var Intent.isSigningOut by intentDelegate(false)
        ```

    d. `by fragmentArgs<T>``: Delegates to the arguments `Bundle` of the `Fragment` so that it survives process death. Usage:
        ```
        class MyFragment {
            private var userId: String by fragmentArgs()
        }
        ```

2. `Fragment` `ViewBinding` delegate: Read only. Delegates to the lazy evaluation of the typed `ViewBinding` by invoking `requireView()` on the `Fragment`.
    The delegate will throw if the `Fragment` view has not been created or has been destroyed .
    It is also lifecycle aware, cleaning up after itself when the `Fragment` `View` is destroyed. Usage:
        ```
        class AFragment {
            private val binding by viewLifecycle(AFragmentBinding::bind)
        }
        ```

### HardServiceConnection

A convenience API for interacting with Bound Services in Android, removing a lot of the boilerplate involved with the added benefit of
generic typing.

### ContextExtensions

Extensions to cover ContextCompat methods

### CharSequenceExtensions

Extensions for CharSequence styling with [spans](https://developer.android.com/reference/android/text/Spannable.html) items.

### DrawableExtensions

Extensions to cover Drawable methods