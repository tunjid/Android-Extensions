package com.tunjid.androidbootstrap.recyclerview;

import com.tunjid.androidbootstrap.functions.BiConsumer;
import com.tunjid.androidbootstrap.functions.Consumer;
import com.tunjid.androidbootstrap.functions.Function;
import com.tunjid.androidbootstrap.functions.Supplier;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

class SwipeDragOptions {

    Supplier<List> listSupplier;
    Supplier<Boolean> itemViewSwipeSupplier;
    Supplier<Boolean> longPressDragSupplier;

    Consumer<RecyclerView.ViewHolder> swipeDragEndConsumerConsumer;
    BiConsumer<RecyclerView.ViewHolder, Integer> swipeDragStartConsumerConsumer;

    Function<RecyclerView.ViewHolder, Integer> movementFlagsSupplier;

    SwipeDragOptions(Consumer<RecyclerView.ViewHolder> swipeDragEndConsumerConsumer,
                     BiConsumer<RecyclerView.ViewHolder, Integer> swipeDragStartConsumerConsumer,
                     Supplier<Boolean> itemViewSwipeSupplier,
                     Supplier<Boolean> longPressDragSupplier,
                     Function<RecyclerView.ViewHolder, Integer> movementFlagsSupplier,
                     Supplier<List> listSupplier) {
        this.swipeDragEndConsumerConsumer = swipeDragEndConsumerConsumer;
        this.swipeDragStartConsumerConsumer = swipeDragStartConsumerConsumer;
        this.itemViewSwipeSupplier = itemViewSwipeSupplier;
        this.longPressDragSupplier = longPressDragSupplier;
        this.movementFlagsSupplier = movementFlagsSupplier;
        this.listSupplier = listSupplier;
    }
}
