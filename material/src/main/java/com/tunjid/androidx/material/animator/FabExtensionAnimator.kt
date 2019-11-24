package com.tunjid.androidx.material.animator

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.google.android.material.button.MaterialButton
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.view.R
import java.util.*

open class FabExtensionAnimator(private val button: MaterialButton) {

    @Suppress("MemberVisibilityCanBePrivate")
    protected var collapsedFabSize: Int = button.resources.getDimensionPixelSize(R.dimen.collapsed_fab_size)
    @Suppress("MemberVisibilityCanBePrivate")
    protected var expandedFabHeight: Int = button.resources.getDimensionPixelSize(R.dimen.extended_fab_height)

    private var glyphState: GlyphState? = null
    private val sizeInterpolator = SpringSizeInterpolator(button, collapsedFabSize, expandedFabHeight)

    val isAnimating: Boolean
        get() = sizeInterpolator.isRunning

    var isExtended: Boolean
        get() {
            val params = button.layoutParams
            return !(params.height == params.width && params.width == collapsedFabSize)
        }
        set(extended) = setExtended(extended, false)

    init {
        button.cornerRadius = collapsedFabSize
        button.setSingleLine()
    }

    fun configureSpring(options: SpringAnimation.() -> Unit) {
        sizeInterpolator.attachToSpring(options)
    }

    @Suppress("unused")
    fun updateGlyphs(@StringRes stringRes: Int, @DrawableRes drawableRes: Int) = button.context.run {
        updateGlyphs(SimpleGlyphState(getText(stringRes), this.drawableAt(drawableRes)!!))
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun updateGlyphs(text: CharSequence, @DrawableRes drawableRes: Int) = button.context.run {
        updateGlyphs(SimpleGlyphState(text, this.drawableAt(drawableRes)!!))
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun updateGlyphs(glyphState: GlyphState) {
        val isSame = glyphState == this.glyphState
        this.glyphState = glyphState
        animateChange(glyphState, isSame)
    }

    private fun animateChange(glyphState: GlyphState, isSame: Boolean) {
        val extended = isExtended
        this.button.text = glyphState.text
        this.button.icon = glyphState.icon
        setExtended(extended, !isSame)
        if (!extended) onPreExtend()
    }

    private fun setExtended(extended: Boolean, force: Boolean) {
        if ( extended && isExtended && !force) return

        if (extended) this.button.text = this.glyphState!!.text
        else this.button.text = ""

        sizeInterpolator.run(extended)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun onPreExtend() {
        val set = AnimatorSet()
        set.play(animateProperty(TWITCH_END, TWITCH_START)).after(animateProperty(TWITCH_START, TWITCH_END))
        set.start()
    }

    private fun animateProperty(start: Float, end: Float): ObjectAnimator {
        return ObjectAnimator.ofFloat(button, ROTATION_Y_PROPERTY, start, end).setDuration(TWITCH_DURATION.toLong())
    }

    abstract class GlyphState {

        abstract val icon: Drawable

        abstract val text: CharSequence
    }

    class SimpleGlyphState constructor(
            override val text: CharSequence,
            override val icon: Drawable
    ) : GlyphState() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val that = other as SimpleGlyphState?
            return icon == that!!.icon && text == that.text
        }

        override fun hashCode(): Int {
            return Objects.hash(icon, text)
        }
    }

    companion object {

        private const val TWITCH_END = 20.0f
        private const val TWITCH_START = 0.0f
        private const val TWITCH_DURATION = 200
        private const val ROTATION_Y_PROPERTY = "rotationY"
    }
}

private class SpringSizeInterpolator(
        val button: MaterialButton,
        val collapsedFabSize: Int,
        val expandedFabHeight: Int
) {

    private var endWidth = 0
    private var endHeight = 0
    private var startWidth = 0
    private var startHeight = 0

    val isRunning get() = spring.isRunning

    private val spring = SpringAnimation(button, sizeProperty, collapsedFabSize.toFloat()).apply {
        spring.stiffness = SpringForce.STIFFNESS_MEDIUM
        spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
    }

    fun run(extended: Boolean) {
        val widthMeasureSpec =
                if (extended) View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                else View.MeasureSpec.makeMeasureSpec(collapsedFabSize, View.MeasureSpec.EXACTLY)

        val heightMeasureSpec =
                if (extended) View.MeasureSpec.makeMeasureSpec(expandedFabHeight, View.MeasureSpec.EXACTLY)
                else View.MeasureSpec.makeMeasureSpec(collapsedFabSize, View.MeasureSpec.EXACTLY)

        button.measure(widthMeasureSpec, heightMeasureSpec)

        startWidth = button.width
        startHeight = button.height
        endWidth = button.measuredWidth
        endHeight = button.measuredHeight

        spring.animateToFinalPosition(endWidth.toFloat())
    }

    fun attachToSpring(options: (SpringAnimation.() -> Unit)?) {
        if (!isRunning) options?.invoke(spring)
    }

    private val slope get() = if (endWidth != startWidth) (endHeight - startHeight) / (endWidth - startWidth) else 0

    private val intercept get() = endHeight - (slope * startHeight)

    private val Int.y: Int get() = intercept + (slope * this)

    private val sizeProperty: FloatPropertyCompat<View>
        get() = object : FloatPropertyCompat<View>("button") {
            override fun setValue(button: View, width: Float) {
                val x = width.toInt()
                button.layoutParams.width = x
                button.layoutParams.height = x.y
                button.requestLayout()
                button.invalidate()
            }

            override fun getValue(button: View): Float = button.run {
                startWidth = width
                startHeight = height
                return width.toFloat()
            }
        }
}