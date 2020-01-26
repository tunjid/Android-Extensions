package com.tunjid.androidx.recyclerview.diff

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * Diffs items allowing for nice optimal list changes and nice transitions if items are [Differentiable],
 * otherwise [ListAdapter.submitList] will perform just as well as a regular
 * [RecyclerView.Adapter.notifyDataSetChanged]
 */
class DiffAdapterCallback<T> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean =
            if (oldItem is Differentiable && newItem is Differentiable) oldItem.diffId == newItem.diffId
            else false

    override fun areContentsTheSame(oldItem: T, newItem: T) =
            if (oldItem is Differentiable && newItem is Differentiable) oldItem.areContentsTheSame(newItem)
            else false

    override fun getChangePayload(oldItem: T, newItem: T): Any? =
            if (oldItem is Differentiable && newItem is Differentiable) oldItem.getChangePayload(newItem)
            else null
}