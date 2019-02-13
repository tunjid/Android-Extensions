package com.tunjid.androidbootstrap.recyclerview;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class ScrollManagerBuilder<T, VH extends RecyclerView.ViewHolder>
        extends AbstractScrollManagerBuilder<ScrollManager<T, VH>, T, VH> {

    ScrollManagerBuilder() {}

    @Override
    public ScrollManager<T, VH> build() {
        RecyclerView.LayoutManager layoutManager = buildLayoutManager();
        EndlessScroller scroller = buildEndlessScroller(layoutManager);
        List<RecyclerView.OnScrollListener> scrollListeners = buildScrollListeners();

        return new ScrollManager<>(
                scroller, placeholder, refreshLayout, swipeDragOptions, recycledViewPool,
                recyclerView, adapter, layoutManager, itemDecorations, scrollListeners, hasFixedSize);
    }

}
