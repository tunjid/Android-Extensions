package com.tunjid.androidx.recyclerview.multiscroll

import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * A class that synchronizes scrolling multiple [RecyclerView]s, useful for creating tables.
 *
 * It is optimized around usage with a [LinearLayoutManager]. The [CellSizer] provides information
 * on how large a cell is in any row to keep all [RecyclerView] instances synchronized
 */
class RecyclerViewMultiScroller(
        @RecyclerView.Orientation private val orientation: Int = RecyclerView.HORIZONTAL,
        private val cellSizer: CellSizer = DynamicCellSizer(orientation)
) {
    var displacement = 0
        private set
    private var active: RecyclerView? = null
    private val syncedScrollers = mutableSetOf<RecyclerView>()
    private val displacementListeners = mutableListOf<(Int) -> Unit>()

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (orientation == RecyclerView.HORIZONTAL && dx == 0) return
            if (orientation == RecyclerView.VERTICAL && dy == 0) return

            if (active != null && recyclerView != active) return

            active = recyclerView
            syncedScrollers.forEach { if (it != recyclerView) it.scrollBy(dx, dy) }
            displacement += if (orientation == RecyclerView.HORIZONTAL) dx else dy
            displacementListeners.forEach { it(displacement) }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (active != recyclerView) return

            displacementListeners.forEach { it(displacement) }
            if (newState == RecyclerView.SCROLL_STATE_IDLE) active = null
        }
    }

    private val onAttachStateChangeListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View?) {
            if (v is RecyclerView) include(v)
        }

        override fun onViewDetachedFromWindow(v: View?) {
            if (v is RecyclerView) exclude(v)
        }
    }

    private val onItemTouchListener = object : RecyclerView.SimpleOnItemTouchListener() {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            // If the user flung the list, then touches any other synced list, stop scrolling
            if (e.actionMasked == MotionEvent.ACTION_DOWN && active != null) active?.stopScroll()
            return false // always return false, we aren't trying to override default scrolling
        }
    }

    fun clear() {
        active = null
        displacementListeners.clear()
        syncedScrollers.clear(this::remove)
        cellSizer.clear()
    }

    fun add(recyclerView: RecyclerView) {
        if (syncedScrollers.contains(recyclerView)) return

        include(recyclerView)
        recyclerView.addOnAttachStateChangeListener(onAttachStateChangeListener)
    }

    fun remove(recyclerView: RecyclerView) {
        exclude(recyclerView)
        recyclerView.removeOnAttachStateChangeListener(onAttachStateChangeListener)
    }

    fun addDisplacementListener(listener: (Int) -> Unit) {
        if (displacementListeners.contains(listener)) return
        displacementListeners.add(listener)
        listener(displacement)
    }

    @Suppress("unused")
    fun removeDisplacementListener(listener: (Int) -> Unit) {
        displacementListeners.remove(listener)
    }

    private fun include(recyclerView: RecyclerView) {
        recyclerView.onIdle {
            if (syncedScrollers.contains(recyclerView)) return@onIdle
            recyclerView.sync()
            syncedScrollers.add(recyclerView)
            cellSizer.include(recyclerView)
            recyclerView.addOnScrollListener(onScrollListener)
            recyclerView.addOnItemTouchListener(onItemTouchListener)
        }
        if (!ViewCompat.isLaidOut(recyclerView) || recyclerView.isLayoutRequested) recyclerView.requestLayout()
    }

    private fun exclude(recyclerView: RecyclerView) {
        recyclerView.removeOnItemTouchListener(onItemTouchListener)
        recyclerView.removeOnScrollListener(onScrollListener)
        cellSizer.exclude(recyclerView)
        syncedScrollers.remove(recyclerView)
    }

    private fun RecyclerView.sync() {
        var offset = displacement
        var position = 0
        while (offset > 0) {
            offset -= cellSizer.sizeAt(position)
            position++
        }

        when (val layoutManager = layoutManager) {
            null -> Unit
            is LinearLayoutManager -> layoutManager.scrollToPositionWithOffset(position, -offset)
            else -> layoutManager.scrollToPosition(position)
        }
    }
}
