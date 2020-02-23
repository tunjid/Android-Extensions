package com.tunjid.androidx.recyclerview.multiscroll

import android.view.View
import androidx.recyclerview.widget.RecyclerView

interface Sizer {
    fun sizeAt(position: Int): Int

    fun include(recyclerView: RecyclerView)

    fun exclude(recyclerView: RecyclerView)

    fun positionAndOffsetForDisplacement(displacement: Int): Pair<Int, Int>
}

internal val View.currentColumn: Int
    get() {
        val recyclerView = parent as? RecyclerView ?: return -1
        val viewHolder = recyclerView.getChildViewHolder(this) ?: return -1

        return viewHolder.layoutPosition
    }