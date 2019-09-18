package com.tunjid.androidbootstrap.recyclerview


import java.util.ArrayList
import java.util.Collections
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlin.math.abs

abstract class EndlessScroller(private val visibleThreshold: Int, // The minimum amount of items to have below your current scroll position before loading more.
                               private val layoutManager: RecyclerView.LayoutManager
) : RecyclerView.OnScrollListener() {


    private val isReverse: Boolean
    private var previousTotal = 0 // The total number of items in the dataset after the last load
    private var loading = true // True if we are still waiting for the last set of data to load.

    protected val firstVisiblePosition: Int
        get() = when (layoutManager) {
            is LinearLayoutManager -> layoutManager.findFirstVisibleItemPosition()
            is StaggeredGridLayoutManager -> {
                val gridLayoutManager = layoutManager
                val store = IntArray(gridLayoutManager.spanCount)
                gridLayoutManager.findFirstVisibleItemPositions(store)
                val list = ArrayList<Int>(store.size)
                for (value in store) list.add(value)
                Collections.min(list)
            }
            else -> 0
        }

    init {
        isReverse = isReverse()
    }

    fun scrollThresholdFilter(dx: Int, dy: Int): Boolean {
        return abs(dy) < 3
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        if (scrollThresholdFilter(dx, dy)) return

        val visibleItemCount = recyclerView.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItem = firstVisiblePosition

        val numScrolled = totalItemCount - visibleItemCount
        var refreshTrigger = if (isReverse) totalItemCount - firstVisibleItem else firstVisibleItem
        refreshTrigger += visibleThreshold

        if (loading && totalItemCount > previousTotal) {
            loading = false
            previousTotal = totalItemCount
        }

        if (!loading && numScrolled <= refreshTrigger) {
            loading = true
            onLoadMore(totalItemCount)
        }
    }

    internal fun reset() {
        loading = false
    }

    abstract fun onLoadMore(currentItemCount: Int)

    protected fun isReverse(): Boolean = when (layoutManager) {
        is LinearLayoutManager -> layoutManager.stackFromEnd
        is StaggeredGridLayoutManager -> layoutManager.reverseLayout
        else -> false
    }
}
