# Core

## Abstract Classes

1. BaseActivity: An activity that delegates all visible UI to an instance of a BaseFragment except global items like a ToolBar or Floating Action Button.
The Fragments are managed by a FragmentStateManager, therefore all fragment's shown are automatically added to the FragmentManger's back stack. 
It also provides convenience methods to show a fragment and find the currently visible fragment.

2. BaseFragement: A fragment that provides a mechanism to provide deterministic stable fragment tags as soon as an instance is created instead of providing it in a fragment transaction.
It also lets you provide a fragment transaction for a new fragment that is coming into the BaseActivity. This is especially useful for shared element transitions.
It currently does not support "optimized" fragment transactions as the API in the support library is not yet stable. As an added convenience, it implements "BackPressInterceptor" which lets it override the back pressed action in it's hosting BaseActivity.

* BaseRecyclerViewAdapter: A RecyclerView Adapter that takes in an instance of it's typed AdapterListener in it's constructor.

4. BaseViewHolder: A View Holder that takes in an instance of it's typed AdapterListener in it's constructor.

## Components

1. FragmentStateManager: A class that keep track of fragments added to the FragmentManager and prevents duplicate fragments (identified by tag) from being added to the FragmentManager.

2. KeyboarUtils: A class for dynamically changing the window size of a fullscreen app when the software keyboard is shown. This prevents UI elements that would otherwise be hidden by the software keyboard from being inaccessible.
It is especially useful when your app want's to draw UI elements behind the status bar.
Works for API 21 (Lollipop) and above.

3. ServiceConnection: A type safe implementation of the Android framework ServiceConnection interface. It simplifies the process of starting or binding to an Android Service.

## Text

1. Spanbuilder: A class for creating and cocatenating a variety of [Spannable](https://developer.android.com/reference/android/text/Spannable.html) items.

## View

1. ViewHider: A class for translating a view offscreen, making it easy to implement the "quick return" pattern