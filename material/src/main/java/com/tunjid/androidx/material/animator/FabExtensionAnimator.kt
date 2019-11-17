package com.tunjid.androidx.material.animator

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.transition.AutoTransition
import android.transition.Transition
import android.transition.Transition.TransitionListener
import android.transition.TransitionManager
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.android.material.button.MaterialButton
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.view.R
import java.util.*

open class FabExtensionAnimator(private val button: MaterialButton) {

    var transitionOptions: (Transition.() -> Unit)? = null

    @Suppress("MemberVisibilityCanBePrivate")
    protected var collapsedFabSize: Int = button.resources.getDimensionPixelSize(R.dimen.collapsed_fab_size)
    @Suppress("MemberVisibilityCanBePrivate")
    protected var expandedFabHeight: Int = button.resources.getDimensionPixelSize(R.dimen.extended_fab_height)
    private var isAnimating: Boolean = false

    private var glyphState: GlyphState? = null

    private val listener = object : TransitionListener {
        override fun onTransitionStart(transition: Transition) {
            isAnimating = true
        }

        override fun onTransitionEnd(transition: Transition) {
            isAnimating = false
        }

        override fun onTransitionCancel(transition: Transition) {
            isAnimating = false
        }

        override fun onTransitionPause(transition: Transition) {}

        override fun onTransitionResume(transition: Transition) {}
    }

    var isExtended: Boolean
        get() {
            val params = button.layoutParams
            return !(params.height == params.width && params.width == collapsedFabSize)
        }
        set(extended) = setExtended(extended, false)

    init {
        button.cornerRadius = collapsedFabSize
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
        if (isAnimating || extended && isExtended && !force) return

        val collapsedFabSize = collapsedFabSize
        val width = if (extended) ViewGroup.LayoutParams.WRAP_CONTENT else collapsedFabSize
        val height = if (extended) expandedFabHeight else collapsedFabSize

        val params = button.layoutParams
        val group = button.parent as ViewGroup

        params.width = width
        params.height = height

        TransitionManager.beginDelayedTransition(group, AutoTransition()
                .setDuration(EXTENSION_DURATION.toLong())
                .addListener(listener)
                .addTarget(button)
                .apply { transitionOptions?.invoke(this) }
        )

        if (extended) this.button.text = this.glyphState!!.text
        else this.button.text = ""

        button.requestLayout()
        button.invalidate()
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
        private const val EXTENSION_DURATION = 150
        private const val ROTATION_Y_PROPERTY = "rotationY"
    }
}
