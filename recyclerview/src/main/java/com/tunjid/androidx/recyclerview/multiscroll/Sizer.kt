package com.tunjid.androidx.recyclerview.multiscroll

import android.view.View
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView

interface Sizer {
    val orientation : Int

    fun sizeAt(position: Int): Int

    fun include(recyclerView: RecyclerView)

    fun exclude(recyclerView: RecyclerView)

    fun positionAndOffsetForDisplacement(displacement: Int): Pair<Int, Int>
}

internal interface ViewModifier {
    val orientation : Int

    fun View.updateSize(updatedSize: Int) = updateLayoutParams {
        if (orientation == RecyclerView.HORIZONTAL) width = updatedSize else height = updatedSize
    }
}

internal val View.currentColumn: Int
    get() {
        val recyclerView = parent as? RecyclerView ?: return -1
        val viewHolder = recyclerView.getChildViewHolder(this) ?: return -1

        return viewHolder.layoutPosition
    }