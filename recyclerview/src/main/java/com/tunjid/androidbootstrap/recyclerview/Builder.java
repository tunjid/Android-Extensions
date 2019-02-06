package com.tunjid.androidbootstrap.recyclerview;

import android.util.Log;

import com.tunjid.androidbootstrap.functions.BiConsumer;
import com.tunjid.androidbootstrap.functions.BiFunction;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.tunjid.androidbootstrap.recyclerview.ScrollManager.TAG;

@SuppressWarnings("WeakerAccess")
public class Builder<T> {

    private static final int LINEAR_LAYOUT_MANAGER = 0;
    private static final int GRID_LAYOUT_MANAGER = 1;
    private static final int STAGGERED_GRID_LAYOUT_MANAGER = 2;

    int spanCount;
    int layoutManagerType;
    boolean hasFixedSize;

    Runnable scrollCallback;
    ListPlaceholder<T> placeholder;
    SwipeRefreshLayout refreshLayout;

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;

    SwipeDragOptions swipeDragOptions;
    RecyclerView.RecycledViewPool recycledViewPool;

    Consumer<RecyclerView.LayoutManager> layoutManagerConsumer;
    Consumer<IndexOutOfBoundsException> handler;

    List<RecyclerView.ItemDecoration> itemDecorations = new ArrayList<>();
    List<Consumer<Integer>> stateConsumers = new ArrayList<>();
    List<BiConsumer<Integer, Integer>> displacementConsumers = new ArrayList<>();

    Builder() {
    }

    public Builder<T> setHasFixedSize() {
        this.hasFixedSize = true;
        return this;
    }

    public Builder<T> withAdapter(@NonNull RecyclerView.Adapter adapter) {
        this.adapter = adapter;
        return this;
    }

    public Builder<T> onLayoutManager(Consumer<RecyclerView.LayoutManager> layoutManagerConsumer) {
        this.layoutManagerConsumer = layoutManagerConsumer;
        return this;
    }

    public Builder<T> withLinearLayoutManager() {
        layoutManagerType = LINEAR_LAYOUT_MANAGER;
        return this;
    }

    public Builder<T> withGridLayoutManager(int spanCount) {
        layoutManagerType = GRID_LAYOUT_MANAGER;
        this.spanCount = spanCount;
        return this;
    }

    public Builder<T> withStaggeredGridLayoutManager(int spanCount) {
        layoutManagerType = STAGGERED_GRID_LAYOUT_MANAGER;
        this.spanCount = spanCount;
        return this;
    }

    public Builder<T> withRecycledViewPool(RecyclerView.RecycledViewPool recycledViewPool) {
        this.recycledViewPool = recycledViewPool;
        return this;
    }

    public Builder<T> withInconsistencyHandler(Consumer<IndexOutOfBoundsException> handler) {
        this.handler = handler;
        return this;
    }

    public Builder<T> withEndlessScrollCallback(@NonNull Runnable scrollCallback) {
        this.scrollCallback = scrollCallback;
        return this;
    }

    public Builder<T> addStateListener(@NonNull Consumer<Integer> stateListener) {
        this.stateConsumers.add(stateListener);
        return this;
    }

    public Builder<T> addScrollListener(@NonNull BiConsumer<Integer, Integer> scrollListener) {
        this.displacementConsumers.add(scrollListener);
        return this;
    }

    public Builder<T> addDecoration(@NonNull RecyclerView.ItemDecoration decoration) {
        this.itemDecorations.add(decoration);
        return this;
    }

    public Builder<T> withRefreshLayout(@NonNull SwipeRefreshLayout refreshLayout, Runnable refreshAction) {
        this.refreshLayout = refreshLayout;
        refreshLayout.setOnRefreshListener(refreshAction::run);
        return this;
    }

    public Builder<T> withPlaceholder(@NonNull ListPlaceholder placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public Builder<T> withSwipeDragOptions(@NonNull SwipeDragOptions swipeDragOptions) {
        this.swipeDragOptions = swipeDragOptions;
        return this;
    }

    public ScrollManager<T> build() {
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

        if (recyclerView == null)
            throw new IllegalArgumentException("RecyclerView must be provided");
        if (layoutManager == null)
            throw new IllegalArgumentException("RecyclerView LayoutManager must be provided");
        if (adapter == null)
            throw new IllegalArgumentException("RecyclerView Adapter must be provided");

        if (handler == null) Log.w(TAG, "InconsistencyHandler is not provided, " +
                "inconsistencies in the RecyclerView adapter will cause crashes at runtime");

        if (layoutManager instanceof LinearLayoutManager)
            ((LinearLayoutManager) layoutManager).setRecycleChildrenOnDetach(true);
        if (layoutManagerConsumer != null) layoutManagerConsumer.accept(layoutManager);

        EndlessScroller scroller = scrollCallback == null ? null : new EndlessScroller(layoutManager) {
            @Override
            public void onLoadMore(int currentItemCount) {
                scrollCallback.run();
            }
        };

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

        return new ScrollManager<>(
                scroller, placeholder, refreshLayout, swipeDragOptions, recycledViewPool,
                recyclerView, adapter, layoutManager, itemDecorations, scrollListeners, hasFixedSize);
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
