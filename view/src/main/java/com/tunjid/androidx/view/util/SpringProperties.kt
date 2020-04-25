package com.tunjid.androidx.view.util

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.dynamicanimation.animation.FloatPropertyCompat

@SuppressLint("RtlHardcoded")
object PaddingProperty {
    val LEFT: FloatPropertyCompat<View> = PaddingSpring(Gravity.LEFT)
    val TOP: FloatPropertyCompat<View> = PaddingSpring(Gravity.TOP)
    val RIGHT: FloatPropertyCompat<View> = PaddingSpring(Gravity.RIGHT)
    val BOTTOM: FloatPropertyCompat<View> = PaddingSpring(Gravity.BOTTOM)
}

@SuppressLint("RtlHardcoded")
object MarginProperty {
    val LEFT: FloatPropertyCompat<View> = MarginSpring(Gravity.LEFT)
    val TOP: FloatPropertyCompat<View> = MarginSpring(Gravity.TOP)
    val RIGHT: FloatPropertyCompat<View> = MarginSpring(Gravity.RIGHT)
    val BOTTOM: FloatPropertyCompat<View> = MarginSpring(Gravity.BOTTOM)
}

private class PaddingSpring(private val direction: Int) : FloatPropertyCompat<View>("Padding-$direction") {
    override fun setValue(view: View, value: Float) = when (direction) {
        Gravity.LEFT -> view.updatePadding(left = value.toInt())
        Gravity.TOP -> view.updatePadding(top = value.toInt())
        Gravity.RIGHT -> view.updatePadding(right = value.toInt())
        Gravity.BOTTOM -> view.updatePadding(bottom = value.toInt())
        else -> throw IllegalArgumentException("Invalid direction")
    }

    override fun getValue(view: View): Float = when (direction) {
        Gravity.LEFT -> view.paddingLeft
        Gravity.TOP -> view.paddingTop
        Gravity.RIGHT -> view.paddingRight
        Gravity.BOTTOM -> view.paddingBottom
        else -> throw IllegalArgumentException("Invalid direction")
    }.toFloat()
}

@SuppressLint("RtlHardcoded")
private class MarginSpring(private val direction: Int) : FloatPropertyCompat<View>("Margin-$direction") {
    override fun setValue(view: View, value: Float) = view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        when (direction) {
            Gravity.LEFT -> updateMargins(left = value.toInt())
            Gravity.TOP -> updateMargins(top = value.toInt())
            Gravity.RIGHT -> updateMargins(right = value.toInt())
            Gravity.BOTTOM -> updateMargins(bottom = value.toInt())
            else -> throw IllegalArgumentException("Invalid direction")
        }
    }

    override fun getValue(view: View): Float = when (direction) {
        Gravity.LEFT -> view.marginLeft
        Gravity.TOP -> view.marginTop
        Gravity.RIGHT -> view.marginRight
        Gravity.BOTTOM -> view.marginBottom
        else -> throw IllegalArgumentException("Invalid direction")
    }.toFloat()
}