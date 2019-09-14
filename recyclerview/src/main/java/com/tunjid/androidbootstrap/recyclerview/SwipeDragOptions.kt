package com.tunjid.androidbootstrap.recyclerview

import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Configurable options for swiping and dragging the [RecyclerView] [RecyclerView.ViewHolder]
 *
 * @param[itemViewSwipeSupplier] Determines if the ItemView supports swipes
 *
 * @param[longPressDragSupplier] Determines if the long press and dragging is enabled
 *
 * @param[dragConsumer] Determines what happens when the ViewHolder is dragged from
 * start [RecyclerView.ViewHolder] to end [RecyclerView.ViewHolder]
 *
 * @param[swipeConsumer] A callback invoked when a ViewHolder is swiped in a certain direction.
 * arguments are the [RecyclerView.ViewHolder] and one of [ItemTouchHelper.UP],
 * [ItemTouchHelper.DOWN], [ItemTouchHelper.LEFT] or [ItemTouchHelper.RIGHT]
 * @see [ItemTouchHelper.Callback.onSwiped]
 *
 * @param[swipeDragStartConsumer] A callback for when a swipe or drag has started on a ViewHolder
 * Arguments include the [RecyclerView.ViewHolder] and the actionState which is one of
 * [ItemTouchHelper.ACTION_STATE_IDLE], [ItemTouchHelper.ACTION_STATE_SWIPE] or
 * [ItemTouchHelper.ACTION_STATE_DRAG]

 * @param[swipeDragEndConsumer] A callback for when a swipe or drag has ended on a ViewHolder.
 * Arguments include the [RecyclerView.ViewHolder] and the actionState
 *
 * @param[movementFlagFunction] Determines whether a [RecyclerView.ViewHolder] can be swiped
 * or dragged. Return value from this param is generated from
 * [ItemTouchHelper.Callback.makeMovementFlags]
 *
 * @param[dragHandleFunction] A function for determining what View in the
 * [RecyclerView.ViewHolder.itemView] starts the drag operation
 *
 * @see [ItemTouchHelper.Callback] for more reference
 */
class SwipeDragOptions<VH : RecyclerView.ViewHolder>(
        internal var itemViewSwipeSupplier: () -> Boolean = { false },
        internal var longPressDragSupplier: () -> Boolean = { false },
        internal var dragConsumer: (VH, VH) -> Unit = { _, _ -> },
        internal var swipeConsumer: (VH, Int) -> Unit = { _, _ -> },
        internal var swipeDragStartConsumer: (VH, Int) -> Unit = { _, _ -> },
        internal var swipeDragEndConsumer: (VH, Int) -> Unit = { _, _ -> },
        internal var movementFlagFunction: (VH) -> Int = { _ -> ListManager.SWIPE_DRAG_ALL_DIRECTIONS },
        internal var dragHandleFunction: (VH) -> View = { viewHolder -> viewHolder.itemView }
)
