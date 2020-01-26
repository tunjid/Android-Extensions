package com.tunjid.androidx.recyclerview

import android.annotation.SuppressLint
import android.view.MotionEvent.ACTION_DOWN
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

@Suppress("UNCHECKED_CAST")
internal class SwipeDragTouchHelper<VH : RecyclerView.ViewHolder>(
        private val recyclerView: RecyclerView,
        private val options: SwipeDragOptions<VH>
) : ItemTouchHelper.Callback(), RecyclerView.OnChildAttachStateChangeListener {

    private var actionState: Int = 0
    private val itemTouchHelper = ItemTouchHelper(this)
    private val dragHandles = mutableSetOf<View>()

    init {
        recyclerView.addOnChildAttachStateChangeListener(this)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun isItemViewSwipeEnabled(): Boolean = options.itemViewSwipeSupplier()

    override fun isLongPressDragEnabled(): Boolean = options.longPressDragSupplier()

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int =
            options.movementFlagFunction(viewHolder as VH)

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        options.dragConsumer(viewHolder as VH, target as VH)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) =
            options.swipeConsumer(viewHolder as VH, direction)

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (viewHolder == null) return
        this.actionState = actionState
        options.swipeDragStartConsumer(viewHolder as VH, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        options.swipeDragEndConsumer(viewHolder as VH, actionState)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onChildViewAttachedToWindow(view: View) {
        val holder = recyclerView.findContainingViewHolder(view) as? VH
                ?: return

        options.dragHandleFunction(holder).apply {
            dragHandles.add(this)
            setOnTouchListener { _, motionEvent ->
                if (motionEvent.actionMasked == ACTION_DOWN) itemTouchHelper.startDrag(holder)
                false
            }
        }
    }

    override fun onChildViewDetachedFromWindow(view: View) {
        val holder = recyclerView.findContainingViewHolder(view) as? VH ?: return
        options.dragHandleFunction(holder).apply {
            dragHandles.remove(this)
            setOnTouchListener(null)
        }
    }

    fun destroy() {
        val iterator = dragHandles.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            next.setOnTouchListener(null)
            iterator.remove()
        }
        itemTouchHelper.attachToRecyclerView(null)
    }
}
