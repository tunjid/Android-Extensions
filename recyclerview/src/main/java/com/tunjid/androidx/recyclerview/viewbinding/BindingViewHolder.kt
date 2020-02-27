package com.tunjid.androidx.recyclerview.viewbinding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

open class BindingViewHolder<T : ViewBinding> private constructor(
        val binding: T
) : RecyclerView.ViewHolder(binding.root) {
    constructor(
            parent: ViewGroup,
            creator: (inflater: LayoutInflater, root: ViewGroup, attachToRoot: Boolean) -> T
    ) : this(creator(
            LayoutInflater.from(parent.context),
            parent,
            false
    ))
}

inline fun <reified T : ViewBinding> BindingViewHolder<*>.binding() = (binding as T)

fun <T : ViewBinding> ViewGroup.viewHolderFrom(creator: (inflater: LayoutInflater, root: ViewGroup, attachToRoot: Boolean) -> T) =
        BindingViewHolder(this, creator)
