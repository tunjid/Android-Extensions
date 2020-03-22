package com.tunjid.androidx.recyclerview.multiscroll

import android.view.View
import androidx.core.view.doOnDetach
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.recyclerview.R

/**
 * Provides information used to synchronize [RecyclerView] instances in a
 * [RecyclerViewMultiScroller]
 */
interface CellSizer {
    /**
     * One of [RecyclerView.HORIZONTAL] or [RecyclerView.VERTICAL]
     */
    val orientation: Int

    /**
     * Called when [RecyclerViewMultiScroller.clear] is. If any references to [RecyclerView]
     * instances are kept in your implementation, they should be cleared out here.
     */
    fun clear()

    /**
     * The size of a Cell at a particular index. Used to synchronize [RecyclerView] scroll
     * positions
     */
    fun sizeAt(position: Int): Int

    /**
     * Called when a [RecyclerView] instance is added to be synchronized. Logic for updating or
     * measuring child views in the [RecyclerView] run here.
     */
    fun include(recyclerView: RecyclerView)

    /**
     * Called when a [RecyclerView] instance is removed from synchronization, usually when it is
     * detached from the [View] window as a result of recycling or [RecyclerViewMultiScroller.clear].
     *
     * If this [RecyclerView] was used to ascertain some cell properties, it should not be used for
     * it anymore as it is not currently visible to the user.
     */
    fun exclude(recyclerView: RecyclerView)

    companion object {
        const val UNKNOWN = -1
        const val DETACHED_SIZE = 80
    }
}

internal interface ViewModifier {
    val orientation: Int

    fun View.updateSize(updatedSize: Int) {
        val layoutParams = layoutParams
        val currentSize = if (isHorizontal) layoutParams.width else layoutParams.height

        if (currentSize == updatedSize) return

        invalidate()
        updateLayoutParams { if (isHorizontal) width = updatedSize else height = updatedSize }

        onParentIdle { requestLayout() }
    }
}

internal inline val ViewModifier.isHorizontal get() = orientation == RecyclerView.HORIZONTAL

internal inline val View.currentColumn: Int
    get() {
        val recyclerView = parentRecyclerView ?: return CellSizer.UNKNOWN
        val viewHolder = recyclerView.getChildViewHolder(this) ?: return CellSizer.UNKNOWN

        return viewHolder.layoutPosition
    }

internal inline val View.parentRecyclerView: RecyclerView?
    get() = parent as? RecyclerView

private inline var View.loopingRunnable: Runnable?
    get() = getTag(R.id.recyclerview_dynamic_sizing_handler) as? Runnable
    set(value) {
        (getTag(R.id.recyclerview_dynamic_sizing_handler) as? Runnable)?.let(this::removeCallbacks)
        setTag(R.id.recyclerview_dynamic_sizing_handler, value)
    }

internal inline val RecyclerView.isBusy get() = !isLaidOut || isLayoutRequested || isComputingLayout

private inline fun View.onParentIdle(crossinline action: () -> Unit) = loop(object : Runnable {
    override fun run() {
        val parent = parentRecyclerView
        if (parent != null && parent.isBusy) post(this)
        else {
            if (parent != null) action()
            loopingRunnable = null
        }
    }
})

internal inline fun RecyclerView.onIdle(crossinline action: () -> Unit) = loop(object : Runnable {
    override fun run() {
        if (isBusy) post(this)
        else action()
    }
})

private fun View.loop(runnable: Runnable) {
    loopingRunnable = runnable

    post(runnable)
    doOnDetach { it.loopingRunnable = null }
}

internal inline fun <T> MutableCollection<T>.clear(afterRemove: (T) -> Unit) {
    val iterator = iterator()
    while (iterator.hasNext()) {
        val next = iterator.next()
        iterator.remove()
        afterRemove(next)
    }
}