package com.tunjid.androidbootstrap.adapters

import android.view.View.*
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidbootstrap.activities.MainActivity.Companion.bottomInset
import com.tunjid.androidbootstrap.recyclerview.AbstractListManagerBuilder
import com.tunjid.androidbootstrap.recyclerview.ListManager

/**
 * A Proxy Adapter that adds extra items to the bottom of the actual adapter for over scrolling
 * to easily compensate for going edge to edge
 */
class MasqueradeAdapter<T : RecyclerView.ViewHolder>(
        private val proxyAdapter: RecyclerView.Adapter<T>,
        private val extras: Int)
    : RecyclerView.Adapter<T>() {

    init {
        setHasStableIds(proxyAdapter.hasStableIds())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): T =
            proxyAdapter.onCreateViewHolder(parent, viewType)

    override fun getItemCount(): Int = proxyAdapter.itemCount + extras

    override fun getItemId(position: Int): Long =
            if (position < proxyAdapter.itemCount) proxyAdapter.getItemId(position)
            else Long.MAX_VALUE - (position - proxyAdapter.itemCount)

    override fun getItemViewType(position: Int): Int =
        if (position < proxyAdapter.itemCount) proxyAdapter.getItemViewType(position)
        else super.getItemViewType(position)

    override fun onBindViewHolder(holder: T, position: Int) {
        val isFromProxy = position < proxyAdapter.itemCount
        adjustSpacers(holder, isFromProxy)

        if (isFromProxy) proxyAdapter.onBindViewHolder(holder, position)
    }

    override fun onBindViewHolder(holder: T, position: Int, payloads: MutableList<Any>) {
        val isFromProxy = position < proxyAdapter.itemCount
        adjustSpacers(holder, isFromProxy)

        if (isFromProxy) proxyAdapter.onBindViewHolder(holder, position, payloads)
    }

    override fun unregisterAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) =
            proxyAdapter.unregisterAdapterDataObserver(observer)

    override fun onViewDetachedFromWindow(holder: T) = proxyAdapter.onViewDetachedFromWindow(holder)

    override fun setHasStableIds(hasStableIds: Boolean) = proxyAdapter.setHasStableIds(hasStableIds)

    override fun onFailedToRecycleView(holder: T): Boolean =
            proxyAdapter.onFailedToRecycleView(holder)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) =
            proxyAdapter.onAttachedToRecyclerView(recyclerView)

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) =
            proxyAdapter.onDetachedFromRecyclerView(recyclerView)

    override fun onViewRecycled(holder: T) = proxyAdapter.onViewRecycled(holder)

    override fun registerAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) =
            proxyAdapter.registerAdapterDataObserver(observer)

    override fun onViewAttachedToWindow(holder: T) = proxyAdapter.onViewAttachedToWindow(holder)

    private fun adjustSpacers(holder: T, isFromProxy: Boolean) {
        holder.itemView.visibility = if (isFromProxy) VISIBLE else INVISIBLE
        holder.itemView.layoutParams.height = if (isFromProxy) RecyclerView.LayoutParams.WRAP_CONTENT else bottomInset
        (holder.itemView as? ViewGroup)?.forEach { it.visibility = if (isFromProxy) VISIBLE else GONE }
    }

}

fun <
        B : AbstractListManagerBuilder<B, S, VH, T>,
        S : ListManager<VH, T>,
        VH : RecyclerView.ViewHolder, T>
        AbstractListManagerBuilder<B, S, VH, T>.withPaddedAdapter(
        adapter: RecyclerView.Adapter<VH>,
        extras: Int = 1): B = withAdapter(MasqueradeAdapter(adapter, extras))

