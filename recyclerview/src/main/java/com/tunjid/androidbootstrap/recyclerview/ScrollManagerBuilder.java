package com.tunjid.androidbootstrap.recyclerview;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class ScrollManagerBuilder<VH extends RecyclerView.ViewHolder, T>
        extends AbstractScrollManagerBuilder<ScrollManagerBuilder<VH, T>, ScrollManager<VH, T>, VH, T> {

    public ScrollManagerBuilder() {}

    @Override
    public ScrollManager<VH, T> build() {
        RecyclerView.LayoutManager layoutManager = buildLayoutManager();
        EndlessScroller scroller = buildEndlessScroller(layoutManager);
        List<RecyclerView.OnScrollListener> scrollListeners = buildScrollListeners();

        return new ScrollManager<>(
                scroller, placeholder, refreshLayout, swipeDragOptions, recycledViewPool,
                recyclerView, adapter, layoutManager, itemDecorations, scrollListeners, hasFixedSize);
    }

}
