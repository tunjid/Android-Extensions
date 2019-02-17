# RecyclerView

Utility classes for the ```RecyclerView``` ```ViewGroup```.

## Differentiable

An interface used to calculate a ```DiffUtil.DiffResult``` without having to implement a ```DiffUtil.Callback```.
It exposes methods that distinguish items in a ```RecyclerView``` from each other.

## Diff

A POJO hosting pure static functions for calculating the diff of two lists. the fields of the POJO
are the ```DiffUtil.DiffResult```, and a ```List``` containing the items of the result of the
bi function applied to the ```Lists``` to diff.


## InteractiveAdapter

A ```RecyclerViewAdapter``` that hosts a ```ViewHolder``` with a typed interaction listener;
Implementations could be ClickListeners, LongClickListeners, both combined or what have you.

## ScrollManager

A class that integrates very common behaviors for a RecyclerView into a highly functional and fluent API.
Convenience methods for dispatching updates to the RecyclerView are also exposed.

## EndlessScroller

A class that makes infinite scrolling for a RecyclerView easier

## ListPlaceholder

Interface for displaying a placeholder in an empty RecyclerView with a ListManager




