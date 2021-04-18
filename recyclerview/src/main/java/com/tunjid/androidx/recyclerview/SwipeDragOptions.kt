package com.tunjid.androidx.recyclerview

import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

internal class SwipeDragOptions<VH : RecyclerView.ViewHolder>(
    internal val itemViewSwipeSupplier: () -> Boolean,
    internal val longPressDragSupplier: () -> Boolean,
    internal val dragConsumer: (VH, VH) -> Unit,
    internal val swipeConsumer: (VH, Int) -> Unit,
    internal val swipeDragStartConsumer: (VH, Int) -> Unit,
    internal val swipeDragEndConsumer: (VH, Int) -> Unit,
    internal val movementFlagFunction: (VH) -> Int,
    internal val dragHandleFunction: (VH) -> View
)

val SWIPE_DRAG_ALL_DIRECTIONS = ItemTouchHelper.Callback.makeMovementFlags(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    ItemTouchHelper.START or ItemTouchHelper.END
)