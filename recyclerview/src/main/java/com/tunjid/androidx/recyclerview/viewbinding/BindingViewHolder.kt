package com.tunjid.androidx.recyclerview.viewbinding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.tunjid.androidx.recyclerview.R

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

fun <T : ViewBinding> ViewGroup.viewHolderFrom(creator: (inflater: LayoutInflater, root: ViewGroup, attachToRoot: Boolean) -> T) =
        BindingViewHolder(this, creator)

inline val BindingViewHolder<*>.propertyMap
    get() = itemView.getTag(R.id.recyclerview_view_binding_map) as? MutableMap<String, Any?>
            ?: mutableMapOf<String, Any?>().also { itemView.setTag(R.id.recyclerview_view_binding_map, it) }
