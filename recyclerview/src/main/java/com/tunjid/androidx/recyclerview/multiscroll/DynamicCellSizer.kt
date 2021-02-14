package com.tunjid.androidx.recyclerview.multiscroll

import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.children
import androidx.core.view.doOnDetach
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.view.util.viewDelegate
import kotlin.math.max

/**
 * A [CellSizer] instance that measures the largest cell and makes sure all [RecyclerView]
 * instances in the [RecyclerViewMultiScroller] match up to it. Each individual cell may be
 * arbitrarily sized.
 */
class DynamicCellSizer(
    @RecyclerView.Orientation
    override val orientation: Int = RecyclerView.HORIZONTAL
) : CellSizer, ViewModifier {

    private val columnSizeMap = mutableMapOf<Int, Int>()
    private val syncedScrollers = mutableSetOf<RecyclerView>()

    private val onChildAttachStateChangeListener = object : RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewDetachedFromWindow(view: View) = excludeChild(view)

        override fun onChildViewAttachedToWindow(view: View) = includeChild(view)
    }

    override fun clear() = syncedScrollers.clear(this::exclude)

    override fun sizeAt(position: Int): Int = columnSizeMap[position] ?: CellSizer.DETACHED_SIZE

    override fun include(recyclerView: RecyclerView) {
        syncedScrollers.add(recyclerView)
        recyclerView.addOnChildAttachStateChangeListener(onChildAttachStateChangeListener)
        recyclerView.children.forEach(::includeChild)
    }

    override fun exclude(recyclerView: RecyclerView) {
        syncedScrollers.remove(recyclerView)
        recyclerView.removeOnChildAttachStateChangeListener(onChildAttachStateChangeListener)
        recyclerView.children.forEach(::excludeChild)
    }

    private fun includeChild(child: View) {
        child.ensureDynamicSizer()

        val column = child.currentColumn
        if (column == CellSizer.UNKNOWN) return

        child.updateSize(sizeAt(column))
    }

    private fun excludeChild(child: View) {
        child.removeDynamicSizer()
        child.updateSize(CellSizer.DETACHED_SIZE)
    }

    private fun View.measureSize(): Int {
        measure(
            View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        )

        return if (isHorizontal) measuredWidth else measuredHeight
    }

    private fun View.ensureDynamicSizer() {
        val existing = dynamicSizer
        if (existing != null) return

        val listener = DynamicSizer(this)
        dynamicSizer = listener

        val observer = viewTreeObserver

        observer.addOnPreDrawListener(listener)
        doOnDetach {
            if (observer.isAlive) observer.removeOnPreDrawListener(listener)
            it.viewTreeObserver.takeIf(ViewTreeObserver::isAlive)?.removeOnPreDrawListener(listener)
            it.dynamicSizer = null
        }
    }

    private fun View.removeDynamicSizer() {
        val listener = dynamicSizer ?: return
        viewTreeObserver.removeOnPreDrawListener(listener)
        dynamicSizer = null
    }

    private inner class DynamicSizer(private val view: View) : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            val column = view.currentColumn
            if (column == CellSizer.UNKNOWN) return true

            val currentSize = view.measureSize()

            val oldMaxSize = sizeAt(column)
            val newMaxSize = max(oldMaxSize, currentSize)

            columnSizeMap[column] = newMaxSize

            if (oldMaxSize != newMaxSize) for (it in syncedScrollers) it.childIn(column)?.updateSize(newMaxSize)

            return true
        }
    }

    private var View.dynamicSizer by viewDelegate<DynamicSizer?>()
}

private fun RecyclerView.childIn(column: Int): View? =
    findViewHolderForLayoutPosition(column)?.itemView
