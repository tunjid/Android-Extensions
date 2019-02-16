package com.tunjid.androidbootstrap.recyclerview;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static androidx.recyclerview.widget.ItemTouchHelper.Callback.makeMovementFlags;

@SuppressWarnings({"unused, WeakerAccess"})
public class ScrollManager<VH extends RecyclerView.ViewHolder, T> {

    static final String TAG = "ScrollManager";

    public static final int NO_SWIPE_OR_DRAG = 0;
    public static final int SWIPE_DRAG_ALL_DIRECTIONS = makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START | ItemTouchHelper.END);

    @Nullable
    protected EndlessScroller scroller;
    @Nullable
    protected ListPlaceholder<T> placeholder;
    @Nullable
    protected ItemTouchHelper touchHelper;
    @Nullable
    protected SwipeRefreshLayout refreshLayout;

    protected RecyclerView recyclerView;
    protected Adapter<VH> adapter;

    protected ScrollManager(@Nullable EndlessScroller scroller,
                            @Nullable ListPlaceholder<T> placeholder,
                            @Nullable SwipeRefreshLayout refreshLayout,
                            @Nullable SwipeDragOptions<VH> options,
                            @Nullable RecyclerView.RecycledViewPool recycledViewPool,
                            RecyclerView recyclerView,
                            Adapter<VH> adapter,
                            LayoutManager layoutManager,
                            List<ItemDecoration> decorations,
                            List<OnScrollListener> listeners,
                            boolean hasFixedSize) {

        if (recyclerView == null)
            throw new IllegalArgumentException("RecyclerView must be provided");
        if (layoutManager == null)
            throw new IllegalArgumentException("RecyclerView LayoutManager must be provided");
        if (adapter == null)
            throw new IllegalArgumentException("RecyclerView Adapter must be provided");

        this.scroller = scroller;
        this.placeholder = placeholder;
        this.refreshLayout = refreshLayout;

        this.recyclerView = recyclerView;
        this.adapter = adapter;

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        if (hasFixedSize) recyclerView.setHasFixedSize(true);
        if (scroller != null) recyclerView.addOnScrollListener(scroller);
        if (recycledViewPool != null) recyclerView.setRecycledViewPool(recycledViewPool);
        if (options != null) touchHelper = fromSwipeDragOptions(this, options);
        if (touchHelper != null) touchHelper.attachToRecyclerView(recyclerView);
        for (ItemDecoration decoration : decorations) recyclerView.addItemDecoration(decoration);
        for (OnScrollListener listener : listeners) recyclerView.addOnScrollListener(listener);
    }

    public static <VH extends RecyclerView.ViewHolder> SwipeDragOptionsBuilder<VH> swipeDragOptionsBuilder() {
        return new SwipeDragOptionsBuilder<>();
    }

    public void updateForEmptyList(T payload) {
        if (placeholder != null) placeholder.bind(payload);
    }

    public void onDiff(DiffUtil.DiffResult result) {
        boolean hasAdapter = adapter != null;
        if (hasAdapter) result.dispatchUpdatesTo(adapter);
        if (refreshLayout != null) refreshLayout.setRefreshing(false);
        if (placeholder != null && hasAdapter) placeholder.toggle(adapter.getItemCount() == 0);
    }

    public void notifyDataSetChanged() {
        boolean hasAdapter = adapter != null;
        if (hasAdapter) adapter.notifyDataSetChanged();
        if (refreshLayout != null) refreshLayout.setRefreshing(false);
        if (placeholder != null && hasAdapter) placeholder.toggle(adapter.getItemCount() == 0);
    }

    public void notifyItemChanged(int position) {
        if (adapter != null) adapter.notifyItemChanged(position);
        if (placeholder != null && adapter != null) placeholder.toggle(adapter.getItemCount() == 0);
    }

    public void notifyItemInserted(int position) {
        if (adapter != null) adapter.notifyItemInserted(position);
        if (placeholder != null && adapter != null) placeholder.toggle(adapter.getItemCount() == 0);
    }

    public void notifyItemRemoved(int position) {
        if (adapter != null) adapter.notifyItemRemoved(position);
        if (placeholder != null && adapter != null) placeholder.toggle(adapter.getItemCount() == 0);
    }

    @SuppressWarnings("WeakerAccess")
    public void notifyItemMoved(int from, int to) {
        if (adapter != null) adapter.notifyItemMoved(from, to);
        if (placeholder != null && adapter != null) placeholder.toggle(adapter.getItemCount() == 0);
    }

    public void notifyItemRangeChanged(int start, int count) {
        if (adapter != null) adapter.notifyItemRangeChanged(start, count);
        if (placeholder != null && adapter != null) placeholder.toggle(adapter.getItemCount() == 0);
    }

    public void startDrag(RecyclerView.ViewHolder viewHolder) {
        if (touchHelper != null) touchHelper.startDrag(viewHolder);
    }

    public void setRefreshing() {
        if (refreshLayout != null) refreshLayout.setRefreshing(true);
    }

    public void reset() {
        if (scroller != null) scroller.reset();
        if (refreshLayout != null) refreshLayout.setRefreshing(false);
    }

    public void clear() {
        if (recyclerView != null) recyclerView.clearOnScrollListeners();

        scroller = null;
        refreshLayout = null;
        placeholder = null;

        recyclerView = null;
        adapter = null;
    }

    @SuppressWarnings("unused")
    public int getFirstVisiblePosition() {
        LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager castedManager = (LinearLayoutManager) layoutManager;
            return castedManager.findFirstVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager castedManager = (StaggeredGridLayoutManager) layoutManager;

            int[] positions = new int[castedManager.getSpanCount()];
            castedManager.findFirstVisibleItemPositions(positions);

            List<Integer> indexes = new ArrayList<>(positions.length);
            for (int i : positions) indexes.add(i);

            return Collections.min(indexes);
        }
        return -1;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public RecyclerView.ViewHolder findViewHolderForItemId(long id) {
        return recyclerView.findViewHolderForItemId(id);
    }

    public void post(Runnable runnable) {
        recyclerView.post(runnable);
    }

    public void postDelayed(long delay, Runnable runnable) {
        recyclerView.postDelayed(runnable, delay);
    }

    private static <VH extends RecyclerView.ViewHolder, T> ItemTouchHelper fromSwipeDragOptions(ScrollManager<VH, T> scrollManager, SwipeDragOptions<VH> options) {
        return new ItemTouchHelper(new SwipeDragTouchHelper<>(scrollManager, options));
    }

}
