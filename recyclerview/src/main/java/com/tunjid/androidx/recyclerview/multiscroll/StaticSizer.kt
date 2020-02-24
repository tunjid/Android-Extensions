package com.tunjid.androidx.recyclerview.multiscroll

import android.content.Context
import android.view.View
import androidx.core.view.children
import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.RecyclerView

@UseExperimental(ExperimentalRecyclerViewMultiScrolling::class)
class StaticSizer(
        @RecyclerView.Orientation override val orientation: Int = RecyclerView.HORIZONTAL,
        private val sizeLookup: (Int) -> Int
) : Sizer, ViewModifier {

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

    override fun positionAndOffsetForDisplacement(displacement: Int): Pair<Int, Int> {
        var offset = displacement
        var position = 0
        while (offset > 0) {
            offset -= sizeAt(position)
            position++
        }

        return position to offset
    }

    private fun includeChild(child: View) {
        val currentColumn = child.currentColumn

        if (currentColumn != Sizer.UNKNOWN) child.updateSize(sizeAt(child.currentColumn))
        else child.doOnNextLayout(this::includeChild)
    }

    private fun excludeChild(child: View) = child.updateSize(Sizer.DETACHED_SIZE)
}
