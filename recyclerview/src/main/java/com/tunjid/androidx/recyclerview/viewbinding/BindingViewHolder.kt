package com.tunjid.androidx.recyclerview.viewbinding

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.tunjid.androidx.recyclerview.R
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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

    class Prop<T> : ReadWriteProperty<BindingViewHolder<*>, T> {
        override fun getValue(thisRef: BindingViewHolder<*>, property: KProperty<*>): T {
            @Suppress("UNCHECKED_CAST")
            return thisRef.propertyMap[property.name] as T
        }

        override fun setValue(thisRef: BindingViewHolder<*>, property: KProperty<*>, value: T) {
            thisRef.propertyMap[property.name] = value
        }
    }
}

fun <T : ViewBinding> ViewGroup.viewHolderFrom(creator: (inflater: LayoutInflater, root: ViewGroup, attachToRoot: Boolean) -> T) =
        BindingViewHolder(this, creator)

@Suppress("UNCHECKED_CAST")
private inline val BindingViewHolder<*>.propertyMap
    get() = itemView.getTag(R.id.recyclerview_view_binding_map) as? MutableMap<String, Any?>
            ?: mutableMapOf<String, Any?>().also { itemView.setTag(R.id.recyclerview_view_binding_map, it) }
