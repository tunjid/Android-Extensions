package com.tunjid.androidx.material.animator

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.doOnLayout
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.google.android.material.button.MaterialButton
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.view.R
import java.util.*

open class FabExtensionAnimator(
        private val button: MaterialButton,
        private val collapsedFabSize: Int = button.resources.getDimensionPixelSize(R.dimen.collapsed_fab_size),
        expandedFabHeight: Int = button.resources.getDimensionPixelSize(R.dimen.extended_fab_height)
) {

    private var glyphState: GlyphState = SimpleGlyphState(button.text, button.icon)
    private val sizeInterpolator = SpringSizeInterpolator(button, collapsedFabSize, expandedFabHeight)

    @Suppress("unused")
    val isRunning: Boolean
        get() = sizeInterpolator.isRunning

    var isExtended: Boolean
        get() = button.layoutParams.run { height != width || width != collapsedFabSize }
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
        updateGlyphs(SimpleGlyphState(getText(stringRes), this.drawableAt(drawableRes)))
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun updateGlyphs(text: CharSequence, @DrawableRes drawableRes: Int) = button.context.run {
        updateGlyphs(SimpleGlyphState(text, this.drawableAt(drawableRes)))
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun updateGlyphs(text: CharSequence, drawable: Drawable?) = button.context.run {
        updateGlyphs(SimpleGlyphState(text, drawable))
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
        if (extended && isExtended && !force) return
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
        abstract val icon: Drawable?

        abstract val text: CharSequence
    }

    class SimpleGlyphState constructor(
            override val text: CharSequence,
            override val icon: Drawable?
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
) : FloatPropertyCompat<View>("FabExtensionSpring") {

    val isRunning get() = spring.isRunning

    private val x1 = collapsedFabSize
    private val y1 = collapsedFabSize
    private var y2 = expandedFabHeight
    private var x2 = button.height

    private val slope get() = if (x2 != x1) (y2 - y1) / (x2 - x1) else 0

    private val intercept get() = y2 - (slope * y1)

    private val spring = SpringAnimation(button, this, x1.toFloat()).apply {
        spring.stiffness = SpringForce.STIFFNESS_MEDIUM
        spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
    }

    fun run(extended: Boolean) = button.doOnLayout {
        val widthMeasureSpec =
                if (extended) View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                else View.MeasureSpec.makeMeasureSpec(collapsedFabSize, View.MeasureSpec.EXACTLY)

        val heightMeasureSpec =
                if (extended) View.MeasureSpec.makeMeasureSpec(expandedFabHeight, View.MeasureSpec.EXACTLY)
                else View.MeasureSpec.makeMeasureSpec(collapsedFabSize, View.MeasureSpec.EXACTLY)

        button.measure(widthMeasureSpec, heightMeasureSpec)

        x2 = button.measuredWidth
        y2 = button.measuredHeight

        spring.animateToFinalPosition(x2.toFloat())
    }

    fun attachToSpring(options: (SpringAnimation.() -> Unit)?) {
        if (!isRunning) options?.invoke(spring)
    }

    private fun f(x: Float): Float = intercept + (slope * x)

    override fun getValue(button: View): Float = button.width.toFloat()

    override fun setValue(button: View, x: Float) = button.run {
        layoutParams.width = x.toInt()
        layoutParams.height = f(x).toInt()
        requestLayout()
        invalidate()
    }

}