package com.tunjid.androidx.recyclerview

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Returns a [LinearLayoutManager] in the [RecyclerView.VERTICAL] orientation
 */
fun RecyclerView.verticalLayoutManager(reverseLayout: Boolean = false) =
        LinearLayoutManager(context, RecyclerView.VERTICAL, reverseLayout)

/**
 * Returns a [LinearLayoutManager] in the [RecyclerView.HORIZONTAL] orientation
 */
fun RecyclerView.horizontalLayoutManager(reverseLayout: Boolean = false) =
        LinearLayoutManager(context, RecyclerView.HORIZONTAL, reverseLayout)

/**
 * Returns a [GridLayoutManager] with a span count of [spanCount], and an optional span size lookup
 */
fun RecyclerView.gridLayoutManager(
        spanCount: Int = 1,
        spanSizeLookup: ((position: Int) -> Int)? = null
): GridLayoutManager = GridLayoutManager(context, spanCount).apply {
    setSpanSizeLookup(object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int = spanSizeLookup?.invoke(position) ?: 1
    })
}

/**
 * @see RecyclerView.Adapter.notifyDataSetChanged
 */
fun RecyclerView.notifyDataSetChanged() = adapter?.notifyDataSetChanged() ?: Unit

/**
 * @see RecyclerView.Adapter.notifyItemChanged
 */
fun RecyclerView.notifyItemChanged(position: Int) = adapter?.notifyItemChanged(position) ?: Unit

/**
 * @see RecyclerView.Adapter.notifyItemInserted
 */
fun RecyclerView.notifyItemInserted(position: Int) = adapter?.notifyItemInserted(position) ?: Unit

/**
 * @see RecyclerView.Adapter.notifyItemRemoved
 */
fun RecyclerView.notifyItemRemoved(position: Int) = adapter?.notifyItemRemoved(position) ?: Unit

/**
 * @see RecyclerView.Adapter.notifyItemMoved
 */
fun RecyclerView.notifyItemMoved(from: Int, to: Int) = adapter?.notifyItemMoved(from, to) ?: Unit

/**
 * @see RecyclerView.Adapter.notifyItemRangeChanged
 */
fun RecyclerView.notifyItemRangeChanged(start: Int, count: Int) = adapter?.notifyItemRangeChanged(start, count)
        ?: Unit

/**
 * Convenience method for adding an [RecyclerView.OnScrollListener]
 */
fun RecyclerView.addScrollListener(scrollListener: (Int, Int) -> Unit): Unit = addOnScrollListener(object : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) = scrollListener(dx, dy)
})

/**
 * Typed convenience method
 * @see RecyclerView.findViewHolderForItemId
 */
inline fun <reified VH : RecyclerView.ViewHolder> RecyclerView.viewHolderForItemId(id: Long) =
        findViewHolderForItemId(id) as? VH

/**
 * Typed convenience method
 * @see RecyclerView.findViewHolderForAdapterPosition
 */
inline fun <reified VH : RecyclerView.ViewHolder> RecyclerView.viewHolderForAdapterPosition(position: Int) =
        findViewHolderForAdapterPosition(id) as? VH

/**
 * Typed convenience method
 * @see RecyclerView.findViewHolderForLayoutPosition
 */
inline fun <reified VH : RecyclerView.ViewHolder> RecyclerView.viewHolderForLayoutPosition(position: Int) =
        findViewHolderForLayoutPosition(id) as? VH

/**
 * Typed convenience method
 * @see RecyclerView.findContainingViewHolder
 */
inline fun <reified VH : RecyclerView.ViewHolder> RecyclerView.containingViewHolder(view: View) =
        findContainingViewHolder(view) as? VH

fun <VH : RecyclerView.ViewHolder> RecyclerView.setSwipeDragOptions(swipeDragOptions: SwipeDragOptions<VH>) {
    val previous = getTag(R.id.recyclerview_swipe_drag) as? SwipeDragTouchHelper<*>
    previous?.destroy()
    setTag(R.id.recyclerview_swipe_drag, SwipeDragTouchHelper(this, swipeDragOptions))
}

/**
 * Resets the endless scroll. This is useful if an endless scroll was called, and nothing new
 * was fetched, or the call failed
 */
fun RecyclerView.resetEndlessScrollListener() =
        (getTag(R.id.recyclerview_endless_scroller) as? ComposedEndlessScroller)?.reset() ?: Unit

/**
 * Adds an endless scroll listener to the [RecyclerView]
 */
fun RecyclerView.setEndlessScrollListener(
        visibleThreshold: Int, // The minimum amount of items to have below your current scroll position before loading more.
        @RecyclerView.Orientation
        orientation: Int = RecyclerView.VERTICAL,
        isLayoutManagerReverse: (RecyclerView.LayoutManager) -> Boolean = ::isReverse,
        firstVisibleItemFunction: (RecyclerView.LayoutManager) -> Int = ::firstVisiblePosition,
        loadMore: (Int) -> Unit
) {
    val previous = getTag(R.id.recyclerview_endless_scroller) as? ComposedEndlessScroller
    if (previous != null) removeOnScrollListener(previous)

    val current = ComposedEndlessScroller(visibleThreshold, orientation, isLayoutManagerReverse, firstVisibleItemFunction, loadMore)
    setTag(R.id.recyclerview_endless_scroller, current)
    addOnScrollListener(current)
}

fun RecyclerView.Adapter<*>.acceptDiff(diffResult: DiffUtil.DiffResult) = diffResult.dispatchUpdatesTo(this)

fun RecyclerView.acceptDiff(diffResult: DiffUtil.DiffResult) = adapter?.acceptDiff(diffResult)
        ?: Unit