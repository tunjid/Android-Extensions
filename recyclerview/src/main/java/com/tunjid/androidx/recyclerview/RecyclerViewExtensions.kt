package com.tunjid.androidx.recyclerview

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.verticalLayoutManager(reverseLayout: Boolean = false) =
        LinearLayoutManager(context, RecyclerView.VERTICAL, reverseLayout)

fun RecyclerView.horizontalLayoutManager(reverseLayout: Boolean = false) =
        LinearLayoutManager(context, RecyclerView.HORIZONTAL, reverseLayout)

fun RecyclerView.gridLayoutManager(spanCount: Int = 1) =
        GridLayoutManager(context, spanCount)

fun RecyclerView.notifyDataSetChanged() = adapter?.notifyDataSetChanged() ?: Unit

fun RecyclerView.notifyItemChanged(position: Int) = adapter?.notifyItemChanged(position) ?: Unit

fun RecyclerView.notifyItemInserted(position: Int) = adapter?.notifyItemInserted(position) ?: Unit

fun RecyclerView.notifyItemRemoved(position: Int) = adapter?.notifyItemRemoved(position) ?: Unit

fun RecyclerView.notifyItemMoved(from: Int, to: Int) = adapter?.notifyItemMoved(from, to) ?: Unit

fun RecyclerView.notifyItemRangeChanged(start: Int, count: Int) = adapter?.notifyItemRangeChanged(start, count)
        ?: Unit

fun RecyclerView.addEndlessScrollListener(
        visibleThreshold: Int, // The minimum amount of items to have below your current scroll position before loading more.
        isLayoutManagerReverse: (RecyclerView.LayoutManager) -> Boolean = ::isReverse,
        firstVisibleItemFunction: (RecyclerView.LayoutManager) -> Int = ::finder,
        loadMore: (Int) -> Unit
) = addOnScrollListener(ComposedEndlessScroller(visibleThreshold, isLayoutManagerReverse, firstVisibleItemFunction, loadMore))

fun RecyclerView.addScrollListener(scrollListener: (Int, Int) -> Unit): Unit = addOnScrollListener(object : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) = scrollListener(dx, dy)
})

inline fun <reified VH : RecyclerView.ViewHolder> RecyclerView.viewHolderForItemId(id: Long) =
        findViewHolderForItemId(id) as? VH

inline fun <reified VH : RecyclerView.ViewHolder> RecyclerView.viewHolderForAdapterPosition(position: Int) =
        findViewHolderForAdapterPosition(id) as? VH

inline fun <reified VH : RecyclerView.ViewHolder> RecyclerView.viewHolderForLayoutPosition(position: Int) =
        findViewHolderForLayoutPosition(id) as? VH

inline fun <reified VH : RecyclerView.ViewHolder> RecyclerView.containingViewHolder(view: View) =
        findContainingViewHolder(view) as? VH

fun <VH : RecyclerView.ViewHolder> RecyclerView.setSwipeDragOptions(swipeDragOptions: SwipeDragOptions<VH>) {
    val previous = getTag(R.id.recyclerview_swipe_drag) as? SwipeDragTouchHelper<*>
    previous?.destroy()
    setTag(R.id.recyclerview_swipe_drag, SwipeDragTouchHelper(this, swipeDragOptions))
}

fun RecyclerView.Adapter<*>.acceptDiff(diffResult: DiffUtil.DiffResult) = diffResult.dispatchUpdatesTo(this)

fun RecyclerView.acceptDiff(diffResult: DiffUtil.DiffResult) = adapter?.acceptDiff(diffResult)
        ?: Unit