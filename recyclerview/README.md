# RecyclerView

Utility classes for the ```RecyclerView``` ```ViewGroup```.

## Diffable

An interface used to calculate a ```DiffUtil.DiffResult``` without having to implement a ```DiffUtil.Callback```.
It exposes methods that distinguish items in a ```RecyclerView``` from each other.

## Diff

A POJO hosting pure static functions for calculating the diff of two lists. the fields of the POJO
are the ```DiffUtil.DiffResult```, and a ```List``` containing the items of the result of the
bi function applied to the ```Lists``` to diff.


## [Delegates](https://kotlinlang.org/docs/reference/delegation.html)

Delegates include:

1. `viewHolderDelegate`: Read/write. Similar to the standard library `by map()`, lets you read/write properties from/to a `RecyclerView.ViewHolder` instance.
    Usage:
        ```
        var PuppyViewHolder.boundItem by viewDelegate<Puppy>()
        ```

## ComposedAdapter / ComposedListAdapter

```RecyclerView.Adapter``` that can be built with the `adapterOf` or `listAdapterOf` function that delegates all callbacks to individually composed functions.

## BindingViewHolder

Create ```RecyclerView.ViewHolder``` instances directly from ```ViewBinding``` generated classes with a simple ```viewGroup.viewHolderFrom``` extension method.

## EndlessScroller

A class that makes infinite scrolling for a RecyclerView easier

## RecyclerViewMultiScroller

A class that synchronizes scrolling multiple [RecyclerView]s, useful for creating tables.

## ListPlaceholder

Interface for displaying a placeholder in an empty RecyclerView with a ListManager




