package com.tunjid.androidx.core.graphics.drawable

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

/**
 * Convenience extension for [DrawableCompat.wrap]  and [DrawableCompat.setTint]
 */
fun Drawable.withTint(@ColorInt tint: Int): Drawable = this.run {
    wrapped.apply { DrawableCompat.setTint(this, tint) }
}

@Suppress("unused")
fun Drawable.withTintList(tint: ColorStateList): Drawable = this.run {
    wrapped.apply { DrawableCompat.setTintList(this, tint) }
}

@Suppress("unused")
fun Drawable.withTintMode(tintMode: PorterDuff.Mode): Drawable = this.run {
    wrapped.apply { DrawableCompat.setTintMode(this, tintMode) }
}

val Drawable.wrapped: Drawable get() = DrawableCompat.wrap(this)