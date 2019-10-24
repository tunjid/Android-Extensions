package com.tunjid.androidx.core.content

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

fun Context.resolveColor(@ColorRes color: Int) = ContextCompat.getColor(this, color)

fun Context.resolveDrawable(@DrawableRes drawable: Int) = ContextCompat.getDrawable(this, drawable)

@ColorInt
fun Context.resolveThemeColor(@AttrRes colorAttr: Int): Int = TypedValue().run typedValue@{
    this@resolveThemeColor.theme.resolveAttribute(colorAttr, this@typedValue, true).run { if (this) data else Color.BLACK }
}

val Context.activity: Activity?
    get() {
        var wrapped = this
        while (wrapped is ContextWrapper)
            if (wrapped is Activity) return wrapped
            else wrapped = wrapped.baseContext

        return null
    }