package com.tunjid.androidbootstrap.recyclerview;

import android.view.View;

import com.tunjid.androidbootstrap.functions.BiConsumer;
import com.tunjid.androidbootstrap.functions.Function;
import com.tunjid.androidbootstrap.functions.Supplier;

import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings("WeakerAccess")
public class SwipeDragOptions<VH extends RecyclerView.ViewHolder> {

    Supplier<Boolean> itemViewSwipeSupplier;
    Supplier<Boolean> longPressDragSupplier;

    BiConsumer<VH, VH> dragConsumer;
    BiConsumer<VH, Integer> swipeConsumer;
    BiConsumer<VH, Integer> swipeDragStartConsumer;
    BiConsumer<VH, Integer> swipeDragEndConsumer;

    Function<VH, Integer> movementFlagFunction;
    Function<VH, View> dragHandleFunction;

    public SwipeDragOptions(Supplier<Boolean> itemViewSwipeSupplier,
                     Supplier<Boolean> longPressDragSupplier,
                     BiConsumer<VH, VH> dragConsumer,
                     BiConsumer<VH, Integer> swipeConsumer,
                     BiConsumer<VH, Integer> swipeDragStartConsumer,
                     BiConsumer<VH, Integer> swipeDragEndConsumer,
                     Function<VH, Integer> movementFlagFunction,
                     Function<VH, View> dragHandleFunction) {
        this.itemViewSwipeSupplier = itemViewSwipeSupplier;
        this.longPressDragSupplier = longPressDragSupplier;
        this.dragConsumer = dragConsumer;
        this.swipeConsumer = swipeConsumer;
        this.swipeDragStartConsumer = swipeDragStartConsumer;
        this.swipeDragEndConsumer = swipeDragEndConsumer;
        this.movementFlagFunction = movementFlagFunction;
        this.dragHandleFunction = dragHandleFunction;
    }
}
