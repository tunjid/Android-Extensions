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
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.tunjid.androidbootstrap.recyclerview.ListManager.TAG;

/**
 * Abstract thisInstance class for creating {@link ListManager scrollmanagers}
 * <p></p>
 * The breakdown for the generic types are as follows:
 * <p></p>
 * B: The implicit type of the Builder. This is only necessary to make the return type for each
 * thisInstance method be the type of any inheriting class, and not this base class. Otherwise inheritors of
 * this subclass will need to override each method here to return their custom thisInstance type to
 * maintain the fluency of the API which is fairly tedious.
 * <p></p>
 * S: The type of the {@link ListManager} to be built.
 * <p></p>
 * VH: The {@link ViewHolder} type in the {@link RecyclerView}
 * <p></p>
 * T: The type bound in the {@link ListPlaceholder}
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class AbstractListManagerBuilder<
        B extends AbstractListManagerBuilder<B, S, VH, T>,
        S extends ListManager<VH, T>,
        VH extends ViewHolder,
        T> {

    private static final int LINEAR_LAYOUT_MANAGER = 1;
    private static final int GRID_LAYOUT_MANAGER = 2;
    private static final int STAGGERED_GRID_LAYOUT_MANAGER = 3;

    protected int spanCount;
    protected int layoutManagerType;
    protected int endlessScrollVisibleThreshold;
    protected boolean hasFixedSize;

    protected ListPlaceholder<T> placeholder;
    protected SwipeRefreshLayout refreshLayout;

    protected RecyclerView recyclerView;
    protected RecyclerView.Adapter<? extends VH> adapter;
    protected RecyclerView.LayoutManager customLayoutManager;

    protected SwipeDragOptions<VH> swipeDragOptions;
    protected RecyclerView.RecycledViewPool recycledViewPool;

    protected Consumer<Integer> endlessScrollConsumer;
    protected Consumer<RecyclerView.LayoutManager> layoutManagerConsumer;
    protected Consumer<IndexOutOfBoundsException> handler;

    protected List<RecyclerView.ItemDecoration> itemDecorations = new ArrayList<>();
    protected List<Consumer<Integer>> stateConsumers = new ArrayList<>();
    protected List<BiConsumer<Integer, Integer>> displacementConsumers = new ArrayList<>();

    protected final B thisInstance;

    @SuppressWarnings("unchecked")
    public AbstractListManagerBuilder() { thisInstance = (B) this;}

    public final B setHasFixedSize() {
        this.hasFixedSize = true;
        return thisInstance;
    }

    public final B withRecyclerView(@NonNull RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        return thisInstance;
    }

    public final B withAdapter(@NonNull RecyclerView.Adapter<? extends VH> adapter) {
        this.adapter = adapter;
        return thisInstance;
    }

    public final B onLayoutManager(Consumer<RecyclerView.LayoutManager> layoutManagerConsumer) {
        this.layoutManagerConsumer = layoutManagerConsumer;
        return thisInstance;
    }

    public final B withLinearLayoutManager() {
        layoutManagerType = LINEAR_LAYOUT_MANAGER;
        return thisInstance;
    }

    public final B withGridLayoutManager(int spanCount) {
        layoutManagerType = GRID_LAYOUT_MANAGER;
        this.spanCount = spanCount;
        return thisInstance;
    }

    public final B withStaggeredGridLayoutManager(int spanCount) {
        layoutManagerType = STAGGERED_GRID_LAYOUT_MANAGER;
        this.spanCount = spanCount;
        return thisInstance;
    }

    public final B withCustomLayoutManager(RecyclerView.LayoutManager layoutManager) {
        this.customLayoutManager = layoutManager;
        return thisInstance;
    }

    public final B withRecycledViewPool(RecyclerView.RecycledViewPool recycledViewPool) {
        this.recycledViewPool = recycledViewPool;
        return thisInstance;
    }

    public final B withInconsistencyHandler(Consumer<IndexOutOfBoundsException> handler) {
        this.handler = handler;
        return thisInstance;
    }

    public final B withEndlessScrollCallback(int threshold, @NonNull Consumer<Integer> endlessScrollConsumer) {
        this.endlessScrollVisibleThreshold = threshold;
        this.endlessScrollConsumer = endlessScrollConsumer;
        return thisInstance;
    }

    public final B addStateListener(@NonNull Consumer<Integer> stateListener) {
        this.stateConsumers.add(stateListener);
        return thisInstance;
    }

    public final B addScrollListener(@NonNull BiConsumer<Integer, Integer> scrollListener) {
        this.displacementConsumers.add(scrollListener);
        return thisInstance;
    }

    public final B addDecoration(@NonNull RecyclerView.ItemDecoration decoration) {
        this.itemDecorations.add(decoration);
        return thisInstance;
    }

    public final B withRefreshLayout(@NonNull SwipeRefreshLayout refreshLayout, Runnable refreshAction) {
        this.refreshLayout = refreshLayout;
        refreshLayout.setOnRefreshListener(refreshAction::run);
        return thisInstance;
    }

    public final B withPlaceholder(@NonNull ListPlaceholder<T> placeholder) {
        this.placeholder = placeholder;
        return thisInstance;
    }

    public final B withSwipeDragOptions(@NonNull SwipeDragOptions<VH> swipeDragOptions) {
        this.swipeDragOptions = swipeDragOptions;
        return thisInstance;
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
        RecyclerView.LayoutManager layoutManager;
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
            default:
                if (customLayoutManager != null) layoutManager = customLayoutManager;
                else throw new IllegalArgumentException("LayoutManager must be provided");
                break;
        }

        if (handler == null) Log.w(TAG, "InconsistencyHandler is not provided, " +
                "inconsistencies in the RecyclerView adapter will cause crashes at runtime");

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
