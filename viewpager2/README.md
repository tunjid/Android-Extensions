# ViewPager2

### FragmentListAdapter

Allows for efficient display `Fragments` in a `ViewPager2` by using `DiffUtil` to diff changes in
the adapter. Extensions are available for both `Fragment` and `FragmentActivity` types.

There are 1 main building blocks:

1. `FragmentTab` a type that represents a `Fragment` in the `ViewPager2`, it's purpose is to map to
a `Fragment` and uniquely identify a it by it's contents, you want to use a `data` class to
implement this, or at the very least a class with stable `equals` and `hashcode` implementations.

With that, updating the `ViewPager2` is as simple as `FragmentListAdapter.submitList(newTabs)`.
