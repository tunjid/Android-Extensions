@file:Suppress("unused")

package com.tunjid.androidx.recyclerview


import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.Callback.makeMovementFlags
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

@Deprecated("Use extensions instead")
@Suppress("MemberVisibilityCanBePrivate")
open class ListManager<VH : ViewHolder, T>(
        protected var scroller: EndlessScroller? = null,
        protected var placeholder: ListPlaceholder<T>? = null,
        protected var refreshLayout: SwipeRefreshLayout? = null,
        options: SwipeDragOptions<VH>? = null,
        recycledViewPool: RecycledViewPool? = null,
        recyclerView: RecyclerView,
        adapter: Adapter<out VH>,
        layoutManager: LayoutManager,
        decorations: List<ItemDecoration> = listOf(),
        listeners: List<OnScrollListener> = listOf(),
        hasFixedSize: Boolean = false
) {
    protected var touchHelper: ItemTouchHelper? = null

    var recyclerView: RecyclerView? = recyclerView

    val firstVisiblePosition: Int
        get() = when (val layoutManager = recyclerView!!.layoutManager) {
            is LinearLayoutManager -> layoutManager.findFirstVisibleItemPosition()
            is StaggeredGridLayoutManager -> layoutManager.findFirstVisibleItemPositions(IntArray(layoutManager.spanCount)).min()
                    ?: -1
            else -> -1
        }

    val lastVisiblePosition: Int
        get() = when (val layoutManager = recyclerView?.layoutManager) {
            is LinearLayoutManager -> layoutManager.findLastVisibleItemPosition()
            is StaggeredGridLayoutManager -> layoutManager.findLastVisibleItemPositions(IntArray(layoutManager.spanCount)).max()
                    ?: -1
            else -> -1
        }

    init {
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        if (hasFixedSize) recyclerView.setHasFixedSize(true)
        scroller?.let { recyclerView.addOnScrollListener(it) }
        if (recycledViewPool != null) recyclerView.setRecycledViewPool(recycledViewPool)

        if (options != null) touchHelper = fromSwipeDragOptions(this, options)
        touchHelper?.attachToRecyclerView(recyclerView)

        for (decoration in decorations) recyclerView.addItemDecoration(decoration)
        for (listener in listeners) recyclerView.addOnScrollListener(listener)
    }

    fun updateForEmptyList(payload: T) {
        placeholder?.bind(payload)
    }

    fun onDiff(result: DiffUtil.DiffResult) {
        refreshLayout?.isRefreshing = false
        withAdapter {
            result.dispatchUpdatesTo(this)
        }
    }

    fun notifyDataSetChanged() {
        refreshLayout?.isRefreshing = false
        withAdapter { notifyDataSetChanged() }
    }

    fun notifyItemChanged(position: Int) = withAdapter { notifyItemChanged(position) }

    fun notifyItemInserted(position: Int) = withAdapter { notifyItemInserted(position) }

    fun notifyItemRemoved(position: Int) = withAdapter { notifyItemRemoved(position) }

    fun notifyItemMoved(from: Int, to: Int) = withAdapter { notifyItemMoved(from, to) }

    fun notifyItemRangeChanged(start: Int, count: Int) = withAdapter { notifyItemRangeChanged(start, count) }

    fun startDrag(viewHolder: ViewHolder) {
        if (touchHelper != null) touchHelper?.startDrag(viewHolder)
    }

    fun setRefreshing() {
        refreshLayout?.isRefreshing = true
    }

    fun reset() {
        scroller?.reset()
        refreshLayout?.isRefreshing = false
    }

    fun clear() {
        recyclerView?.clearOnScrollListeners()

        scroller = null
        refreshLayout = null
        placeholder = null

        recyclerView = null
    }

    private fun withAdapter(function: Adapter<*>.() -> Unit) {
        recyclerView?.adapter.apply {
            function(this as Adapter<*>)
            placeholder?.toggle(itemCount == 0)
        }
    }

    fun withRecyclerView(consumer: (RecyclerView) -> Unit) {
        recyclerView?.apply(consumer)
                ?: Log.w(TAG, "ListManager RecyclerView is null. Did you clear it?")
    }

    fun findViewHolderForItemId(id: Long): VH? = recyclerView?.findViewHolderForItemId(id) as? VH

    fun post(runnable: () -> Unit) {
        recyclerView?.post(runnable)
    }

    fun postDelayed(delay: Long, runnable: () -> Unit) {
        recyclerView?.postDelayed(runnable, delay)
    }

    companion object {

        internal const val TAG = "ListManager"

        const val NO_SWIPE_OR_DRAG = 0

        val SWIPE_DRAG_ALL_DIRECTIONS = makeMovementFlags(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                ItemTouchHelper.START or ItemTouchHelper.END
        )

        private fun <VH : ViewHolder, T> fromSwipeDragOptions(listManager: ListManager<VH, T>, options: SwipeDragOptions<VH>): ItemTouchHelper =
                ItemTouchHelper(SwipeDragTouchHelper(listManager.recyclerView!!, options))
    }

}