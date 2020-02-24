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
import com.tunjid.androidx.recyclerview.R

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

    val View.size get() = if (isHorizontal) width else height

    fun View.updateSize(updatedSize: Int) {
        val layoutParams = layoutParams
        val currentSize = if (isHorizontal) layoutParams.width else layoutParams.height

        if (currentSize == updatedSize) return
        val parentRecyclerView = parentRecyclerView ?: return

        invalidate()
        updateLayoutParams { if (isHorizontal) width = updatedSize else height = updatedSize }

        sizingHandler.cancel()
        sizingHandler.repeat(parentRecyclerView) { requestLayout() }
    }

    fun View.log(action: String, filter: (String) -> Boolean = { true }) {
        if (this@ViewModifier is StaticSizer) return

        (this as? ViewGroup)?.children
                ?.filterIsInstance<TextView>()
                ?.filter { filter(it.text.toString()) }
                ?.firstOrNull()
                ?.let { Log.i("TEST", "$action ${it.text}") }
    }
}

internal inline val ViewModifier.isHorizontal get() = orientation == RecyclerView.HORIZONTAL

internal inline val View.currentColumn: Int
    get() {
        val recyclerView = parentRecyclerView ?: return Sizer.UNKNOWN
        val viewHolder = recyclerView.getChildViewHolder(this) ?: return Sizer.UNKNOWN

        return viewHolder.layoutPosition
    }

internal inline val View.parentRecyclerView: RecyclerView?
    get() = parent as? RecyclerView

private inline val View.sizingHandler: Handler
    get() = getTag(R.id.recyclerview_dynamic_sizing_handler) as? Handler
            ?: Handler().apply { setTag(R.id.recyclerview_dynamic_sizing_handler, this) }

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

private fun Handler.cancel(runnable: Runnable? = null) {
    if (runnable != null) removeCallbacks(runnable)
    removeCallbacksAndMessages(null)
}
