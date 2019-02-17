package com.tunjid.androidbootstrap.recyclerview;

import android.util.Log;

import com.tunjid.androidbootstrap.functions.BiConsumer;
import com.tunjid.androidbootstrap.functions.Consumer;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.tunjid.androidbootstrap.recyclerview.ScrollManager.TAG;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class AbstractScrollManagerBuilder<S extends ScrollManager<T, VH>, T, VH extends RecyclerView.ViewHolder> {
    private static final int LINEAR_LAYOUT_MANAGER = 0;
    private static final int GRID_LAYOUT_MANAGER = 1;
    private static final int STAGGERED_GRID_LAYOUT_MANAGER = 2;

    protected int spanCount;
    protected int layoutManagerType;
    protected int endlessScrollVisibleThreshold;
    protected boolean hasFixedSize;

    protected ListPlaceholder<T> placeholder;
    protected SwipeRefreshLayout refreshLayout;

    protected RecyclerView recyclerView;
    protected RecyclerView.Adapter<VH> adapter;

    protected SwipeDragOptions<VH> swipeDragOptions;
    protected RecyclerView.RecycledViewPool recycledViewPool;

    protected Consumer<Integer> endlessScrollConsumer;
    protected Consumer<RecyclerView.LayoutManager> layoutManagerConsumer;
    protected Consumer<IndexOutOfBoundsException> handler;

    protected List<RecyclerView.ItemDecoration> itemDecorations = new ArrayList<>();
    protected List<Consumer<Integer>> stateConsumers = new ArrayList<>();
    protected List<BiConsumer<Integer, Integer>> displacementConsumers = new ArrayList<>();

    public AbstractScrollManagerBuilder() {}

    public AbstractScrollManagerBuilder<S, T, VH> setHasFixedSize() {
        this.hasFixedSize = true;
        return this;
    }

    public AbstractScrollManagerBuilder<S, T, VH> withAdapter(@NonNull RecyclerView.Adapter<VH> adapter) {
        this.adapter = adapter;
        return this;
    }

    public AbstractScrollManagerBuilder<S, T, VH> onLayoutManager(Consumer<RecyclerView.LayoutManager> layoutManagerConsumer) {
        this.layoutManagerConsumer = layoutManagerConsumer;
        return this;
    }

    public AbstractScrollManagerBuilder<S, T, VH> withLinearLayoutManager() {
        layoutManagerType = LINEAR_LAYOUT_MANAGER;
        return this;
    }

    public AbstractScrollManagerBuilder<S, T, VH> withGridLayoutManager(int spanCount) {
        layoutManagerType = GRID_LAYOUT_MANAGER;
        this.spanCount = spanCount;
        return this;
    }

    public AbstractScrollManagerBuilder<S, T, VH> withStaggeredGridLayoutManager(int spanCount) {
        layoutManagerType = STAGGERED_GRID_LAYOUT_MANAGER;
        this.spanCount = spanCount;
        return this;
    }

    public AbstractScrollManagerBuilder<S, T, VH> withRecycledViewPool(RecyclerView.RecycledViewPool recycledViewPool) {
        this.recycledViewPool = recycledViewPool;
        return this;
    }

    public AbstractScrollManagerBuilder<S, T, VH> withInconsistencyHandler(Consumer<IndexOutOfBoundsException> handler) {
        this.handler = handler;
        return this;
    }

    public AbstractScrollManagerBuilder<S, T, VH> withEndlessScrollCallback(int threshold, @NonNull Consumer<Integer> endlessScrollConsumer) {
        this.endlessScrollVisibleThreshold = threshold;
        this.endlessScrollConsumer = endlessScrollConsumer;
        return this;
    }

    public AbstractScrollManagerBuilder<S, T, VH> addStateListener(@NonNull Consumer<Integer> stateListener) {
        this.stateConsumers.add(stateListener);
        return this;
    }

    public AbstractScrollManagerBuilder<S, T, VH> addScrollListener(@NonNull BiConsumer<Integer, Integer> scrollListener) {
        this.displacementConsumers.add(scrollListener);
        return this;
    }

    public AbstractScrollManagerBuilder<S, T, VH> addDecoration(@NonNull RecyclerView.ItemDecoration decoration) {
        this.itemDecorations.add(decoration);
        return this;
    }

    public AbstractScrollManagerBuilder<S, T, VH> withRefreshLayout(@NonNull SwipeRefreshLayout refreshLayout, Runnable refreshAction) {
        this.refreshLayout = refreshLayout;
        refreshLayout.setOnRefreshListener(refreshAction::run);
        return this;
    }

    public AbstractScrollManagerBuilder<S, T, VH> withPlaceholder(@NonNull ListPlaceholder<T> placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public AbstractScrollManagerBuilder<S, T, VH> withSwipeDragOptions(@NonNull SwipeDragOptions<VH> swipeDragOptions) {
        this.swipeDragOptions = swipeDragOptions;
        return this;
    }

    public abstract S build();

    protected EndlessScroller buildEndlessScroller(RecyclerView.LayoutManager layoutManager) {
        return endlessScrollConsumer == null ? null : new EndlessScroller(endlessScrollVisibleThreshold, layoutManager) {
            @Override
            public void onLoadMore(int currentItemCount) {
                endlessScrollConsumer.accept(currentItemCount);
            }
        };
    }

    @CallSuper
    protected List<RecyclerView.OnScrollListener> buildScrollListeners() {
        int stateConsumersSize = stateConsumers.size();
        int scrollConsumersSize = displacementConsumers.size();
        int max = Math.max(stateConsumersSize, scrollConsumersSize);

        List<RecyclerView.OnScrollListener> scrollListeners = new ArrayList<>(max);

        for (int i = 0; i < max; i++) {
            final Consumer<Integer> consumer;
            final BiConsumer<Integer, Integer> biConsumer;

            if (i < stateConsumersSize) consumer = stateConsumers.get(i);
            else consumer = null;
            if (i < scrollConsumersSize) biConsumer = displacementConsumers.get(i);
            else biConsumer = null;

            scrollListeners.add(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    if (biConsumer != null) biConsumer.accept(dx, dy);
                }

                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    if (consumer != null) consumer.accept(newState);
                }
            });
        }

        stateConsumers.clear();
        displacementConsumers.clear();
        return scrollListeners;
    }

    protected RecyclerView.LayoutManager buildLayoutManager() {
        RecyclerView.LayoutManager layoutManager = null;
        switch (layoutManagerType) {
            case STAGGERED_GRID_LAYOUT_MANAGER:
                layoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL) {
                    @Override
                    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                        handleLayout(recycler, state, super::onLayoutChildren);
                    }
                };
                break;
            case GRID_LAYOUT_MANAGER:
                layoutManager = new GridLayoutManager(recyclerView.getContext(), spanCount) {
                    @Override
                    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                        handleLayout(recycler, state, super::onLayoutChildren);
                    }
                };
                break;
            case LINEAR_LAYOUT_MANAGER:
                layoutManager = new LinearLayoutManager(recyclerView.getContext()) {
                    @Override
                    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                        handleLayout(recycler, state, super::onLayoutChildren);
                    }
                };
                break;
        }

        if (handler == null) Log.w(TAG, "InconsistencyHandler is not provided, " +
                "inconsistencies in the RecyclerView adapter will cause crashes at runtime");

        if (layoutManager instanceof LinearLayoutManager)
            ((LinearLayoutManager) layoutManager).setRecycleChildrenOnDetach(true);
        if (layoutManagerConsumer != null) layoutManagerConsumer.accept(layoutManager);

        return layoutManager;
    }

    private void handleLayout(RecyclerView.Recycler recycler, RecyclerView.State state,
                              BiConsumer<RecyclerView.Recycler, RecyclerView.State> function) {
        try {
            function.accept(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            if (handler != null) handler.accept(e);
            else throw e;
        }
    }
}
