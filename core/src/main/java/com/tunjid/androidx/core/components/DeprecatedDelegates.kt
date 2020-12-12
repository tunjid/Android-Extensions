package com.tunjid.androidx.core.components

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.tunjid.androidx.core.delegates.fragmentArgs
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Similar to the kotlin Standard Library by [Map],
 * this delegates a property to the [Fragment.getArguments] Bundle
 */
@Deprecated(
    message = "Use the statically defined fragmentArgs() delegate instead",
    replaceWith = ReplaceWith(
        expression = "fragmentArgs()",
        imports = arrayOf("com.tunjid.androidx.core.delegates.fragmentArgs")
    )
)
@Suppress("unused")
inline fun <reified T> Fragment.args(): ReadWriteProperty<Fragment, T> = fragmentArgs()

@Deprecated(
    message = "Use the composed bundleDelegate() with default arguments instead",
    replaceWith = ReplaceWith(
        expression = "bundle()",
        imports = arrayOf("com.tunjid.androidx.core.delegates.bundleDelegate")
    )
)
@Suppress("unused")
inline fun <F, reified T> bundleDelegate(
    crossinline bundleProvider: (F) -> Bundle
): ReadWriteProperty<F, T> = object : ReadWriteProperty<F, T> {
    override operator fun getValue(thisRef: F, property: KProperty<*>): T =
        bundleProvider(thisRef).get(property.name) as T

    override fun setValue(thisRef: F, property: KProperty<*>, value: T) =
        bundleProvider(thisRef).putAll(bundleOf(property.name to value))
}