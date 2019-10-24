package com.tunjid.androidx.core.graphics.drawable

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

fun Drawable?.updateTint(@ColorInt tint: Int) = this?.run {
    DrawableCompat.wrap(this).apply { DrawableCompat.setTint(this, tint) }
}