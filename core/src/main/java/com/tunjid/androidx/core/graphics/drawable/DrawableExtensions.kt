package com.tunjid.androidx.core.graphics.drawable

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

/**
 * Convenience extension for [DrawableCompat.wrap]  and [DrawableCompat.setTint]
 */
fun Drawable?.withTint(@ColorInt tint: Int) = this?.run {
    wrapped.apply { DrawableCompat.setTint(this, tint) }
}

@Suppress("unused")
fun Drawable?.withTintList(tint: ColorStateList) = this?.run {
    wrapped.apply { DrawableCompat.setTintList(this, tint) }
}

@Suppress("unused")
fun Drawable?.withTintMode(tintMode: PorterDuff.Mode) = this?.run {
    wrapped.apply { DrawableCompat.setTintMode(this, tintMode) }
}

val Drawable.wrapped get() = DrawableCompat.wrap(this)