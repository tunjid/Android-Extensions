package com.tunjid.androidx.functions.collections

fun <T> MutableCollection<T>.replace(src: Collection<T>) {
    clear()
    addAll(src)
}

/**
 * Returns an immutable list that reflects the source list, i.e
 * changes in the source are immediately visible.
 */
fun <F, T> MutableList<F>.transform(fromFunction: (F) -> T): List<T> =
        TransformingSequentialList(this, fromFunction, null)

/**
 * Returns a list that reflects the source list, i.e
 * changes in the source are immediately visible.
 * It is important that the toFunction return the item from the original source list to maintain
 * consistency.
 */
fun <F, T> MutableList<F>.transform(fromFunction: (F) -> T, toFunction: ((T) -> F)?): MutableList<T> =
        TransformingSequentialList(this, fromFunction, toFunction)
