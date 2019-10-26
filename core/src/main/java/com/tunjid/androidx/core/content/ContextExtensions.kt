package com.tunjid.androidx.core.content

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

/**
 * Convenience method for [ContextCompat.getColor]
 */
@ColorInt
fun Context.resolveColor(@ColorRes color: Int) = ContextCompat.getColor(this, color)

/**
 * Convenience method for [ContextCompat.getDrawable]
 */
fun Context.resolveDrawable(@DrawableRes drawable: Int) = ContextCompat.getDrawable(this, drawable)

@ColorInt
fun Context.resolveThemeColor(@AttrRes colorAttr: Int): Int = TypedValue().run typedValue@{
    this@resolveThemeColor.theme.resolveAttribute(colorAttr, this@typedValue, true).run { if (this) data else Color.BLACK }
}

/**
 * Unwraps the [Activity] backing this [Context] if available. This is typically useful for
 * [View] instances, as they typically return an instance of [ContextWrapper] from [View.getContext]
 *
 * This property will return null for [Service] and [Application] backed [ContextWrapper]
 * instances as you would expect.
 */
val Context.unwrapActivity: Activity?
    get() {
        var wrapped = this
        while (wrapped is ContextWrapper)
            if (wrapped is Activity) return wrapped
            else wrapped = wrapped.baseContext

        return null
    }