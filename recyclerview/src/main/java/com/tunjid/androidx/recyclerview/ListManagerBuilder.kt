package com.tunjid.androidx.recyclerview

import androidx.recyclerview.widget.RecyclerView

@Deprecated("Use extensions instead")
class ListManagerBuilder<VH : RecyclerView.ViewHolder, T> : AbstractListManagerBuilder<ListManagerBuilder<VH, T>, ListManager<VH, T>, VH, T>() {
    override fun build(): ListManager<VH, T> {
        val layoutManager = buildLayoutManager()
        val scroller = buildEndlessScroller(layoutManager)
        val scrollListeners = buildScrollListeners()

        return ListManager(
                scroller, placeholder, refreshLayout, swipeDragOptions, recycledViewPool,
                recyclerView!!, adapter!!, layoutManager, itemDecorations, scrollListeners, hasFixedSize)
    }

}
