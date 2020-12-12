package com.tunjid.androidx.core.delegates

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Similar to the kotlin Standard Library by [Map], this function delegates reading and writing
 * properties to a [Bundle].
 */
fun <T> bundle(default: T? = null): ReadWriteProperty<Bundle, T> =
    BundleDelegate(default)

/**
 * Similar to the kotlin Standard Library by [Map], this function delegates reading and writing
 * properties to a [Intent] via the bundle from [Intent.getExtras].
 */
fun <T> intentExtras(default: T? = null): ReadWriteProperty<Intent, T> = bundle(default).map(
    postWrite = Intent::replaceExtras,
    mapper = Intent::ensureExtras
)

/**
 * Similar to the kotlin Standard Library by [Map], this function delegates reading and writing
 * properties to an [Activity] via its [Intent] through the bundle from [Intent.getExtras].
 *
 * NOTE: if you use this delegate in conjunction with [Activity.onNewIntent],
 * remember to call [Activity.setIntent] after [Activity.onNewIntent]
 */
fun <T> activityIntent(default: T? = null): ReadWriteProperty<Activity, T> = intentExtras(default).map(
    postWrite = Activity::setIntent,
    mapper = Activity::getIntent
)

/**
 * Similar to the kotlin Standard Library by [Map],
 * this delegates a property to the [Fragment.getArguments] Bundle
 */
fun <T> fragmentArgs(): ReadWriteProperty<Fragment, T> = bundle<T>().map(
    mapper = Fragment::ensureArgs
)

/**
 * A delegate that reads from a [Bundle]
 */
private class BundleDelegate<T>(
    private val default: T? = null
) : ReadWriteProperty<Bundle, T> {
    @Suppress("UNCHECKED_CAST")
    override operator fun getValue(
        thisRef: Bundle,
        property: KProperty<*>
    ): T = when (val value = thisRef.get(property.name)) {
        null -> default
        else -> value
    } as T

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: T) =
        thisRef.putAll(bundleOf(property.name to value))
}

private val Intent.ensureExtras get() = extras ?: putExtras(Bundle()).let { extras!! }

private val Fragment.ensureArgs get() = arguments ?: Bundle().also(::setArguments)
