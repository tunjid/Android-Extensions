package com.tunjid.androidx.view.util

import android.view.View
import androidx.viewbinding.ViewBinding
import com.tunjid.androidx.core.delegates.map
import com.tunjid.androidx.view.R
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Similar to the kotlin Standard Library by [Map], this function delegates reading and writing
 * properties to a [Map] held in a tag held by the [View] instance.
 */
fun <T> viewDelegate(default: T? = null): ReadWriteProperty<View, T> =
    ViewDelegate(default)

/**
 * Similar to the kotlin Standard Library by [Map], this function delegates reading and writing
 * properties to a [Map] held in a tag held by the [View] instance provided by [ViewBinding.getRoot].
 */
fun <T> viewBindingDelegate(default: T? = null): ReadWriteProperty<ViewBinding, T> =
    viewDelegate(default).map(mapper = ViewBinding::getRoot)

private class ViewDelegate<T>(
    private val default: T? = null,
) : ReadWriteProperty<View, T> {
    @Suppress("UNCHECKED_CAST")
    override operator fun getValue(thisRef: View, property: KProperty<*>): T {
        val map = thisRef
            .getOrPutTag<MutableMap<String, Any?>>(R.id.view_delegate_property_map, ::mutableMapOf)
        return (map[property.name] ?: default) as T
    }

    override fun setValue(thisRef: View, property: KProperty<*>, value: T) {
        val map = thisRef
            .getOrPutTag<MutableMap<String, Any?>>(R.id.view_delegate_property_map, ::mutableMapOf)
        map[property.name] = value
    }
}
