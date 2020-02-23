package com.tunjid.androidx.recyclerview.multiscroll

import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.doOnDetach
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView

interface Sizer {
    val orientation: Int

    fun sizeAt(position: Int): Int

    fun include(recyclerView: RecyclerView)

    fun exclude(recyclerView: RecyclerView)

    fun positionAndOffsetForDisplacement(displacement: Int): Pair<Int, Int>

    companion object {
        const val UNKNOWN = -1
        const val DETACHED_SIZE = 80
    }
}

internal interface ViewModifier {
    val orientation: Int

    val isHorizontal get() = orientation == RecyclerView.HORIZONTAL

    val View.size get() = if (isHorizontal) width else height

    fun View.updateSize(updatedSize: Int) {
        val layoutParams = layoutParams
        val currentSize = if (isHorizontal) layoutParams.width else layoutParams.height

        if (currentSize == updatedSize) return
        val parentRecyclerView = parentRecyclerView ?: return

        invalidate()
        updateLayoutParams { if (isHorizontal) width = updatedSize else height = updatedSize }

        Handler().repeat(parentRecyclerView) { requestLayout() }
    }
}

internal val View.currentColumn: Int
    get() {
        val recyclerView = parent as? RecyclerView ?: return -1
        val viewHolder = recyclerView.getChildViewHolder(this) ?: return -1

        return viewHolder.layoutPosition
    }

internal val View.parentRecyclerView: RecyclerView?
    get() = parent as? RecyclerView

private fun Handler.repeat(view: RecyclerView, action: () -> Unit) {
    val runnable = object : Runnable {
        override fun run() {
            if (!view.isLaidOut || view.isLayoutRequested || view.isComputingLayout) {
                post(this)
            } else {
                action()
                cancel(this)
            }
        }
    }

    post(runnable)
    view.doOnDetach { cancel(runnable) }
}

private fun Handler.cancel(runnable: Runnable) {
    removeCallbacks(runnable)
    removeCallbacksAndMessages(null)
}

internal fun View.log(action: String) {
    (this as? ViewGroup)?.children?.filterIsInstance<TextView>()?.firstOrNull()?.let {
        Log.i("TEST", "$action ${it.text}")
    }
}