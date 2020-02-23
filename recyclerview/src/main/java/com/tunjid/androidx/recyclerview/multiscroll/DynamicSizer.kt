package com.tunjid.androidx.recyclerview.multiscroll

import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.recyclerview.R
import kotlin.math.max

@UseExperimental(ExperimentalRecyclerViewMultiScrolling::class)
class DynamicSizer(
        @RecyclerView.Orientation override val orientation: Int = RecyclerView.HORIZONTAL
) : Sizer, ViewModifier {

    private val columnSizeMap = mutableMapOf<Int, Int>()
    private val syncedScrollers = mutableSetOf<RecyclerView>()

    private val onChildAttachStateChangeListener = object : RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewDetachedFromWindow(view: View) = excludeChild(view)

        override fun onChildViewAttachedToWindow(view: View) = includeChild(view)
    }

    override fun sizeAt(position: Int): Int = columnSizeMap[position] ?: Sizer.UNKNOWN

    override fun include(recyclerView: RecyclerView) {
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
            val sizeAtPosition = columnSizeMap[position]
            offset -= (sizeAtPosition ?: -1)
            if (sizeAtPosition != null) position++
        }

        return position to offset
    }

    private fun includeChild(child: View) {
        child.setSizer()

        val column = child.currentColumn
        val lastSize = (if (column != Sizer.UNKNOWN) columnSizeMap[column] else null) ?: return

        child.updateSize(lastSize)
    }

    private fun excludeChild(child: View) {
        child.removeSizer()
        child.updateSize(Sizer.DETACHED_SIZE)
    }

    private fun View.dynamicResize() {
        val recyclerView = parentRecyclerView ?: return

        val column = currentColumn
        if (column == Sizer.UNKNOWN) return

        val currentSize = measure()

        val oldMaxSize = columnSizeMap[column] ?: 0
        val newMaxSize = max(oldMaxSize, currentSize)

        columnSizeMap[column] = newMaxSize

        if (currentSize != newMaxSize) updateSize(newMaxSize)

        if (oldMaxSize != newMaxSize) for (it in syncedScrollers) {
            if (it == recyclerView) continue
            it.childIn(column)?.updateSize(newMaxSize)
        }
    }

    private fun View.measure(): Int {
        measure(
                View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        )

        return if (isHorizontal) measuredWidth else measuredHeight
    }

    private fun RecyclerView.childIn(column: Int): View? =
            findViewHolderForLayoutPosition(column)?.itemView

    private fun View.setSizer() {
        val existing = getTag(R.id.recyclerview_pre_draw) as? ViewTreeObserver.OnPreDrawListener
        if (existing != null) return

        val listener = ViewTreeObserver.OnPreDrawListener {
            dynamicResize()
            true
        }

        viewTreeObserver.addOnPreDrawListener(listener)
        setTag(R.id.recyclerview_pre_draw, listener)
    }

    private fun View.removeSizer() {
        val listener = getTag(R.id.recyclerview_pre_draw) as? ViewTreeObserver.OnPreDrawListener
                ?: return
        viewTreeObserver.removeOnPreDrawListener(listener)
        setTag(R.id.recyclerview_pre_draw, null)
    }
}
