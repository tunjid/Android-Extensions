package com.tunjid.androidx.recyclerview.viewbinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.tunjid.androidx.core.components.map
import com.tunjid.androidx.recyclerview.R
import com.tunjid.androidx.view.util.getOrPutTag
import com.tunjid.androidx.view.util.viewDelegate
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Similar to the kotlin Standard Library by [Map], this function delegates reading and writing
 * properties to a [Map] held in a tag held by the [View] instance in the [RecyclerView.ViewHolder.itemView].
 */
fun <T> viewHolderDelegate(default: T? = null): ReadWriteProperty<RecyclerView.ViewHolder, T> =
    viewDelegate(default).map(mapper = RecyclerView.ViewHolder::itemView)

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

    @Deprecated(
        message = "Use the more direct viewHolderDelegate() function instead",
        replaceWith = ReplaceWith(
            expression = "viewHolderDelegate()",
            imports = arrayOf("com.tunjid.androidx.recyclerview.viewbinding.viewHolderDelegate")
        )
    )
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

inline fun <reified T : ViewBinding> BindingViewHolder<*>.binding() = (binding as T)

@Suppress("UNCHECKED_CAST")
inline fun <reified T : ViewBinding> BindingViewHolder<*>.typed() = this as BindingViewHolder<T>

fun <T : ViewBinding> ViewGroup.viewHolderFrom(
    creator: (inflater: LayoutInflater, root: ViewGroup, attachToRoot: Boolean) -> T
): BindingViewHolder<T> = BindingViewHolder(this, creator)

private inline val BindingViewHolder<*>.propertyMap
    get() = itemView.getOrPutTag<MutableMap<String, Any?>>(R.id.recyclerview_view_binding_map, ::mutableMapOf)
