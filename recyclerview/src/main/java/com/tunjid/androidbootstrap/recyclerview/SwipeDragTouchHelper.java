package com.tunjid.androidbootstrap.recyclerview;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import static android.util.Log.w;
import static com.tunjid.androidbootstrap.recyclerview.ScrollManager.TAG;

class SwipeDragTouchHelper<VH extends RecyclerView.ViewHolder, T> extends ItemTouchHelper.Callback
        implements RecyclerView.OnChildAttachStateChangeListener {

    private int actionState;
    private final ScrollManager<VH, T> scrollManager;
    private final SwipeDragOptions<VH> options;

    SwipeDragTouchHelper(ScrollManager<VH, T> scrollManager, SwipeDragOptions<VH> options) {
        this.scrollManager = scrollManager;
        this.options = options;
        scrollManager.getRecyclerView().addOnChildAttachStateChangeListener(this);
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return options.itemViewSwipeSupplier.get();
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return options.longPressDragSupplier.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return options.movementFlagFunction.apply((VH) viewHolder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        options.dragConsumer.accept((VH) viewHolder, (VH) target);
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        options.swipeConsumer.accept((VH) viewHolder, direction);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if (viewHolder == null) return;
        this.actionState = actionState;
        options.swipeDragStartConsumer.accept((VH) viewHolder, actionState);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        options.swipeDragEndConsumer.accept((VH) viewHolder, actionState);
    }

    @Override
    @SuppressWarnings("unchecked")
    @SuppressLint("ClickableViewAccessibility")
    public void onChildViewAttachedToWindow(@NonNull View view) {
        RecyclerView recyclerView = getRecyclerView();
        if (recyclerView == null) return;

        VH holder = (VH) recyclerView.findContainingViewHolder(view);
        if (holder == null) return;

        options.dragHandleFunction.apply(holder).setOnTouchListener((touched, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN)
                scrollManager.startDrag(holder);
            return false;
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onChildViewDetachedFromWindow(@NonNull View view) {
        RecyclerView recyclerView = getRecyclerView();
        if (recyclerView == null) return;

        VH viewHolder = (VH) recyclerView.findContainingViewHolder(view);
        if (viewHolder != null)
            options.dragHandleFunction.apply(viewHolder).setOnTouchListener(null);
    }

    @Nullable
    private RecyclerView getRecyclerView() {
        RecyclerView recyclerView = scrollManager.getRecyclerView();
        if (recyclerView == null) w(TAG, "Null RecyclerView. Did you clear it?");
        return recyclerView;
    }
}
