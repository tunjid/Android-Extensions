package com.tunjid.androidx.recyclerview.multiscroll

import android.content.Context
import android.util.Log
import android.view.View
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.recyclerview.R

@UseExperimental(ExperimentalRecyclerViewMultiScrolling::class)
class StaticSizer(
        @RecyclerView.Orientation private val orientation: Int = RecyclerView.HORIZONTAL,
        private val sizeLookup: (Int) -> Int
) : Sizer {

    private var appContext: Context? = null
    private val syncedScrollers = mutableSetOf<RecyclerView>()

    private val onChildAttachStateChangeListener = object : RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewDetachedFromWindow(view: View) = excludeChild(view)

        override fun onChildViewAttachedToWindow(view: View) = includeChild(view)
    }

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

        Log.i("TEST", "Syncing. position: $position; offset: $offset")

        return position to offset
    }

    private fun includeChild(child: View) {
        child.updateSize(sizeAt(child.currentColumn))
    }

    private fun excludeChild(child: View) {
        child.updateSize(80)
    }

    private fun View.updateSize(updatedSize: Int) = updateLayoutParams {
        if (orientation == RecyclerView.HORIZONTAL) width = updatedSize else height = updatedSize
    }
}