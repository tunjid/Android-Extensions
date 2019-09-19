package com.tunjid.androidbootstrap.core.components

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Delegates a property to the arguments Bundle, keyed by the property name
 */
inline fun <reified T> Fragment.args(): ReadWriteProperty<Fragment, T> = bundleDelegate {
    if (arguments == null) arguments = Bundle()
    requireArguments()
}

inline fun <F, reified T> bundleDelegate(
        crossinline bundleProvider: (F) -> Bundle
): ReadWriteProperty<F, T> = object : ReadWriteProperty<F, T> {
    override operator fun getValue(thisRef: F, property: KProperty<*>): T =
            bundleProvider(thisRef).get(property.name) as T

    override fun setValue(thisRef: F, property: KProperty<*>, value: T) =
            bundleProvider(thisRef).putAll(bundleOf(property.name to value))
}