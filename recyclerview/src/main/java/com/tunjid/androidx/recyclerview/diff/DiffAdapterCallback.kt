package com.tunjid.androidx.recyclerview.diff

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * Diffs items allowing for nice optimal list changes and nice transitions if items are [Differentiable],
 * otherwise [ListAdapter.submitList] will perform just as well as a regular [RecyclerView.Adapter.notifyDataSetChanged]
 *
 * It is highly recommended that type [T] is a Data class,
 * or at the very least overrides [Any.equals] and [Any.hashCode]
 *
 */
class DiffAdapterCallback<T> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean =
            if (oldItem is Diffable && newItem is Diffable) oldItem.diffId == newItem.diffId
            else false

    @SuppressLint("DiffUtilEquals") // Fallback to Object.equals is necessary
    override fun areContentsTheSame(oldItem: T, newItem: T) =
            if (oldItem is Diffable && newItem is Diffable) oldItem.areContentsTheSame(newItem)
            else oldItem == newItem

    override fun getChangePayload(oldItem: T, newItem: T): Any? =
            if (oldItem is Diffable && newItem is Diffable) oldItem.getChangePayload(newItem)
            else null
}