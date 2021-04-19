package com.tunjid.androidx.recyclerview.multiscroll

import android.content.Context
import android.view.View
import androidx.core.view.children
import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.RecyclerView

/**
 * A [CellSizer] that fixes the size of each cell to the size provided by the [sizeLookup]
 * function at each index.
 */
class StaticCellSizer(
    @RecyclerView.Orientation override val orientation: Int = RecyclerView.HORIZONTAL,
    private val sizeLookup: (Int) -> Int
) : CellSizer, ViewModifier {

    private var appContext: Context? = null
    private val syncedScrollers = mutableSetOf<RecyclerView>()

    private val onChildAttachStateChangeListener = object : RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewDetachedFromWindow(view: View) = excludeChild(view)

        override fun onChildViewAttachedToWindow(view: View) = includeChild(view)
    }

    override fun clear() = syncedScrollers.clear(this::exclude)

    override fun sizeAt(position: Int): Int = sizeLookup(position)

    override fun include(recyclerView: RecyclerView) {
        appContext = recyclerView.context.applicationContext

        syncedScrollers.add(recyclerView)
        recyclerView.addOnChildAttachStateChangeListener(onChildAttachStateChangeListener)
        recyclerView.children.forEach { includeChild(it) }
    }

    override fun exclude(recyclerView: RecyclerView) {
        syncedScrollers.remove(recyclerView)
        recyclerView.removeOnChildAttachStateChangeListener(onChildAttachStateChangeListener)
        recyclerView.children.forEach { excludeChild(it) }
    }

    private fun includeChild(child: View) {
        val currentColumn = child.currentColumn

        if (currentColumn != CellSizer.UNKNOWN) child.updateSize(sizeAt(child.currentColumn))
        else child.doOnNextLayout(this::includeChild)
    }

    private fun excludeChild(child: View) = child.updateSize(CellSizer.DETACHED_SIZE)
}
