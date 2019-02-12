package com.tunjid.androidbootstrap.recyclerview;

import android.view.View;

import com.tunjid.androidbootstrap.functions.BiConsumer;
import com.tunjid.androidbootstrap.functions.Function;
import com.tunjid.androidbootstrap.functions.Supplier;

import androidx.recyclerview.widget.RecyclerView;

import static com.tunjid.androidbootstrap.recyclerview.ScrollManager.SWIPE_DRAG_ALL_DIRECTIONS;

public class SwipeDragOptionsBuilder<VH extends RecyclerView.ViewHolder> {

    private Supplier<Boolean> itemViewSwipeSupplier = () -> false;
    private Supplier<Boolean> longPressDragEnabledSupplier = () -> false;

    private BiConsumer<VH, VH> dragConsumer = (start, end) -> {};
    private BiConsumer<VH, Integer> swipeConsumer = (viewHolder, direction) -> {};
    private BiConsumer<VH, Integer> swipeDragStartConsumer = (viewHolder, state) -> {};
    private BiConsumer<VH, Integer> swipeDragEndConsumer = (viewHolder, state) -> {};

    private Function<VH, Integer> movementFlagsFunction = viewHolder -> SWIPE_DRAG_ALL_DIRECTIONS;
    private Function<VH, View> dragHandleFunction = viewHolder -> viewHolder.itemView;

    @SuppressWarnings("WeakerAccess")
    public SwipeDragOptionsBuilder() {}

    public SwipeDragOptionsBuilder<VH> setItemViewSwipeSupplier(Supplier<Boolean> itemViewSwipeSupplier) {
        this.itemViewSwipeSupplier = itemViewSwipeSupplier;
        return this;
    }

    public SwipeDragOptionsBuilder<VH> setLongPressDragEnabledSupplier(Supplier<Boolean> longPressDragEnabledSupplier) {
        this.longPressDragEnabledSupplier = longPressDragEnabledSupplier;
        return this;
    }

    public SwipeDragOptionsBuilder<VH> setDragConsumer(BiConsumer<VH, VH> dragConsumer) {
        this.dragConsumer = dragConsumer;
        return this;
    }

    public SwipeDragOptionsBuilder<VH> setSwipeConsumer(BiConsumer<VH, Integer> swipeConsumer) {
        this.swipeConsumer = swipeConsumer;
        return this;
    }

    public SwipeDragOptionsBuilder<VH> setSwipeDragStartConsumer(BiConsumer<VH, Integer> swipeDragStartConsumer) {
        this.swipeDragStartConsumer = swipeDragStartConsumer;
        return this;
    }

    public SwipeDragOptionsBuilder<VH> setSwipeDragEndConsumer(BiConsumer<VH, Integer> swipeDragEndConsumerConsumer) {
        this.swipeDragEndConsumer = swipeDragEndConsumerConsumer;
        return this;
    }

    public SwipeDragOptionsBuilder<VH> setMovementFlagsFunction(Function<VH, Integer> movementFlagsFunction) {
        this.movementFlagsFunction = movementFlagsFunction;
        return this;
    }

    public SwipeDragOptionsBuilder<VH> setDragHandleFunction(Function<VH, View> dragHandleFunction) {
        this.dragHandleFunction = dragHandleFunction;
        return this;
    }

    public SwipeDragOptions<VH> build() {
        return new SwipeDragOptions<>(
                itemViewSwipeSupplier,
                longPressDragEnabledSupplier,
                dragConsumer,
                swipeConsumer,
                swipeDragStartConsumer,
                swipeDragEndConsumer,
                movementFlagsFunction,
                dragHandleFunction
        );
    }
}
