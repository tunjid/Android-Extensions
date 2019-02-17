package com.tunjid.androidbootstrap.recyclerview;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class ListManagerBuilder<VH extends RecyclerView.ViewHolder, T>
        extends AbstractListManagerBuilder<ListManagerBuilder<VH, T>, ListManager<VH, T>, VH, T> {

    public ListManagerBuilder() {}

    @Override
    public ListManager<VH, T> build() {
        RecyclerView.LayoutManager layoutManager = buildLayoutManager();
        EndlessScroller scroller = buildEndlessScroller(layoutManager);
        List<RecyclerView.OnScrollListener> scrollListeners = buildScrollListeners();

        return new ListManager<>(
                scroller, placeholder, refreshLayout, swipeDragOptions, recycledViewPool,
                recyclerView, adapter, layoutManager, itemDecorations, scrollListeners, hasFixedSize);
    }

}
