package com.tunjid.androidbootstrap.recyclerview;

import com.tunjid.androidbootstrap.functions.BiConsumer;
import com.tunjid.androidbootstrap.functions.Consumer;
import com.tunjid.androidbootstrap.functions.Function;
import com.tunjid.androidbootstrap.functions.Supplier;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class SwipeDragOptionsBuilder {

    private Supplier<List> listSupplier;
    private Supplier<Boolean> itemViewSwipeSupplier = () -> false;
    private Supplier<Boolean> longPressDragEnabledSupplier = () -> false;

    private Consumer<RecyclerView.ViewHolder> swipeDragEndConsumerConsumer = viewHolder -> {};
    private BiConsumer<RecyclerView.ViewHolder, Integer> swipeDragStartConsumerConsumer = (viewHolder, state) -> {};

    private Function<RecyclerView.ViewHolder, Integer> movementFlagsSupplier = viewHolder -> ScrollManager.defaultMovements();

    public SwipeDragOptionsBuilder setSwipeDragEndConsumer(Consumer<RecyclerView.ViewHolder> swipeDragEndConsumerConsumer) {
        this.swipeDragEndConsumerConsumer = swipeDragEndConsumerConsumer;
        return this;
    }

    public SwipeDragOptionsBuilder setSwipeDragStartConsumerConsumer(BiConsumer<RecyclerView.ViewHolder, Integer> swipeDragStartConsumerConsumer) {
        this.swipeDragStartConsumerConsumer = swipeDragStartConsumerConsumer;
        return this;
    }

    public SwipeDragOptionsBuilder setItemViewSwipeSupplier(Supplier<Boolean> itemViewSwipeSupplier) {
        this.itemViewSwipeSupplier = itemViewSwipeSupplier;
        return this;
    }

    public SwipeDragOptionsBuilder setLongPressDragEnabledSupplier(Supplier<Boolean> longPressDragEnabledSupplier) {
        this.longPressDragEnabledSupplier = longPressDragEnabledSupplier;
        return this;
    }

    public SwipeDragOptionsBuilder setMovementFlagsSupplier(Function<RecyclerView.ViewHolder, Integer> movementFlagsSupplier) {
        this.movementFlagsSupplier = movementFlagsSupplier;
        return this;
    }

    public SwipeDragOptionsBuilder setListSupplier(Supplier<List> listSupplier) {
        this.listSupplier = listSupplier;
        return this;
    }

    public SwipeDragOptions build() {
        return new SwipeDragOptions(swipeDragEndConsumerConsumer, swipeDragStartConsumerConsumer, itemViewSwipeSupplier, longPressDragEnabledSupplier, movementFlagsSupplier, listSupplier);
    }
}
