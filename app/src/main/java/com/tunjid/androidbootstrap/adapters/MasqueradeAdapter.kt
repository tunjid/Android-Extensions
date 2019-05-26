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

    override fun onBindViewHolder(holder: T, position: Int) {
        val isFromProxy = position < proxyAdapter.itemCount

        holder.itemView.visibility = if (isFromProxy) VISIBLE else INVISIBLE
        holder.itemView.layoutParams.height = if (isFromProxy) RecyclerView.LayoutParams.WRAP_CONTENT else bottomInset
        (holder.itemView as? ViewGroup)?.forEach { it.visibility = if (isFromProxy) VISIBLE else GONE }

        if (isFromProxy) proxyAdapter.onBindViewHolder(holder, position)
    }

}

fun <
        B : AbstractListManagerBuilder<B, S, VH, T>,
        S : ListManager<VH, T>,
        VH : RecyclerView.ViewHolder, T>
        AbstractListManagerBuilder<B, S, VH, T>.withPaddedAdapter(
        adapter: RecyclerView.Adapter<VH>,
        extras: Int = 1): B = withAdapter(MasqueradeAdapter(adapter, extras))

