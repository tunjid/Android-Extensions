package com.tunjid.androidx.recyclerview


import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import java.util.*
import kotlin.math.abs

internal class ComposedEndlessScroller(
        private val visibleThreshold: Int, // The minimum amount of items to have below your current scroll position before loading more.
        @RecyclerView.Orientation
        private val orientation: Int,
        private val isLayoutManagerReverse: (RecyclerView.LayoutManager) -> Boolean,
        private val firstVisibleItemFunction: (RecyclerView.LayoutManager) -> Int,
        private val loadMore: (Int) -> Unit
) : RecyclerView.OnScrollListener() {

    private var previousTotal = 0 // The total number of items in the dataset after the last load
    private var loading = true // True if we are still waiting for the last set of data to load.

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val change = if(orientation == RecyclerView.HORIZONTAL) dx else dy
        val layoutManager = recyclerView.layoutManager ?: return
        if (abs(change) < 3) return

        val visibleItemCount = recyclerView.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItem = firstVisibleItemFunction(layoutManager)

        val numScrolled = totalItemCount - visibleItemCount
        var refreshTrigger = if (isLayoutManagerReverse(layoutManager)) totalItemCount - firstVisibleItem else firstVisibleItem
        refreshTrigger += visibleThreshold

        if (loading && totalItemCount > previousTotal) {
            loading = false
            previousTotal = totalItemCount
        }

        if (!loading && numScrolled <= refreshTrigger) {
            loading = true
            loadMore(totalItemCount)
        }
    }

    internal fun reset() {
        loading = false
    }
}

internal fun firstVisiblePosition(layoutManager: RecyclerView.LayoutManager): Int = when (layoutManager) {
    is LinearLayoutManager -> layoutManager.findFirstVisibleItemPosition()
    is StaggeredGridLayoutManager -> {
        val store = IntArray(layoutManager.spanCount)
        layoutManager.findFirstVisibleItemPositions(store)
        val list = ArrayList<Int>(store.size)
        for (value in store) list.add(value)
        Collections.min(list)
    }
    else -> 0
}

internal fun isReverse(layoutManager: RecyclerView.LayoutManager): Boolean = when (layoutManager) {
    is LinearLayoutManager -> layoutManager.stackFromEnd
    is StaggeredGridLayoutManager -> layoutManager.reverseLayout
    else -> false
}