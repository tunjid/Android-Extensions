package com.tunjid.androidx.recyclerview

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

private class ComposedAdapter<ItemT : Any?, VH : RecyclerView.ViewHolder>(
        private val itemsSource: () -> List<ItemT>,
        private val viewHolderCreator: (ViewGroup, Int) -> VH,
        private val viewHolderBinder: (holder: VH, item: ItemT, position: Int) -> Unit,
        private val viewHolderPartialBinder: ((holder: VH, item: ItemT, position: Int, updates: List<Any>) -> Unit)? = null,
        private val viewTypeFunction: ((ItemT) -> Int)? = null,
        private val itemIdFunction: ((ItemT) -> Long)? = null,
        private val onViewHolderAttached: ((VH) -> Unit)? = null,
        private val onViewHolderDetached: ((VH) -> Unit)? = null,
        private val onViewHolderRecycled: ((VH) -> Unit)? = null,
        private val onViewHolderRecycleFailed: ((VH) -> Boolean)? = null,
        private val onAttachedToRecyclerView: ((RecyclerView) -> Unit)? = null,
        private val onDetachedFromRecyclerView: ((RecyclerView) -> Unit)? = null
) : RecyclerView.Adapter<VH>() {

    init {
        setHasStableIds(itemIdFunction != null)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = viewHolderCreator(parent, viewType)

    override fun onBindViewHolder(holder: VH, position: Int) = viewHolderBinder(holder, itemsSource()[position], position)

    override fun getItemCount(): Int = itemsSource().size

    override fun getItemViewType(position: Int): Int =
            viewTypeFunction?.invoke(itemsSource()[position]) ?: super.getItemViewType(position)

    override fun getItemId(position: Int): Long =
            itemIdFunction?.invoke(itemsSource()[position]) ?: super.getItemId(position)

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) =
            viewHolderPartialBinder?.invoke(holder, itemsSource()[position], position, payloads)
                    ?: super.onBindViewHolder(holder, position, payloads)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) =
            onAttachedToRecyclerView?.invoke(recyclerView)
                    ?: super.onAttachedToRecyclerView(recyclerView)

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) =
            onDetachedFromRecyclerView?.invoke(recyclerView)
                    ?: super.onDetachedFromRecyclerView(recyclerView)

    override fun onViewAttachedToWindow(holder: VH) =
            onViewHolderAttached?.invoke(holder) ?: super.onViewAttachedToWindow(holder)

    override fun onViewRecycled(holder: VH) =
            onViewHolderRecycled?.invoke(holder) ?: super.onViewRecycled(holder)

    override fun onViewDetachedFromWindow(holder: VH) =
            onViewHolderDetached?.invoke(holder) ?: super.onViewDetachedFromWindow(holder)

    override fun onFailedToRecycleView(holder: VH): Boolean =
            onViewHolderRecycleFailed?.invoke(holder) ?: super.onFailedToRecycleView(holder)

}

fun <ItemT, VH : RecyclerView.ViewHolder> adapterOf(
        itemsSource: () -> List<ItemT>,
        viewHolderCreator: (ViewGroup, Int) -> VH,
        viewHolderBinder: (holder: VH, item: ItemT, position: Int) -> Unit,
        viewHolderPartialBinder: ((holder: VH, item: ItemT, position: Int, updates: List<Any>) -> Unit)? = null,
        viewTypeFunction: ((ItemT) -> Int)? = null,
        itemIdFunction: ((ItemT) -> Long)? = null,
        onViewHolderAttached: ((VH) -> Unit)? = null,
        onViewHolderDetached: ((VH) -> Unit)? = null,
        onViewHolderRecycled: ((VH) -> Unit)? = null,
        onViewHolderRecycleFailed: ((VH) -> Boolean)? = null,
        onAttachedToRecyclerView: ((RecyclerView) -> Unit)? = null,
        onDetachedFromRecyclerView: ((RecyclerView) -> Unit)? = null
): RecyclerView.Adapter<VH> =
        ComposedAdapter(
                itemsSource,
                viewHolderCreator,
                viewHolderBinder,
                viewHolderPartialBinder,
                viewTypeFunction,
                itemIdFunction,
                onViewHolderAttached,
                onViewHolderDetached,
                onViewHolderRecycled,
                onViewHolderRecycleFailed,
                onAttachedToRecyclerView,
                onDetachedFromRecyclerView
        )