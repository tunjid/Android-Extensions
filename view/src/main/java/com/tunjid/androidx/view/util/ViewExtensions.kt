package com.tunjid.androidx.view.util

import android.graphics.Point
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.annotation.IdRes
import androidx.core.graphics.component1
import androidx.core.graphics.component2
import androidx.core.view.doOnLayout
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.tunjid.androidx.view.R


/**
 * An extension function which creates/retrieves a [SpringAnimation] and stores it in the [View]s
 * tag.
 * This was lifted from a github repo referenced in a Medium post by Google Dev advocate Nick Butcher.
 * It is likely it will be bundled in a KTX library in the future, and this should be removed then,
 * along with its the corresponding entries in ids.xml
 *
 * [Github](https://github.com/android/plaid/pull/751/files#diff-02877e05f7cadd07c732fe9755337c3bR31-R49)
 * [Medium](https://medium.com/androiddevelopers/motional-intelligence-build-smarter-animations-821af4d5f8c0)
 */
fun View.spring(
        property: DynamicAnimation.ViewProperty,
        stiffness: Float = SpringForce.STIFFNESS_MEDIUM,
        damping: Float = SpringForce.DAMPING_RATIO_NO_BOUNCY,
        startVelocity: Float? = null
): SpringAnimation {
    val key = getKey(property)

    val springAnim = getTag(key) as? SpringAnimation
            ?: SpringAnimation(this, property).apply { setTag(key, this) }

    springAnim.spring = (springAnim.spring ?: SpringForce()).apply {
        this.dampingRatio = damping
        this.stiffness = stiffness
    }

    startVelocity?.let { springAnim.setStartVelocity(it) }

    return springAnim
}

/**
 * Map from a [DynamicAnimation.ViewProperty] to an `id` suitable to use as a [View] tag.
 */
@IdRes
private fun getKey(property: DynamicAnimation.ViewProperty): Int = when (property) {
    SpringAnimation.TRANSLATION_X -> R.id.translation_x
    SpringAnimation.TRANSLATION_Y -> R.id.translation_y
    SpringAnimation.TRANSLATION_Z -> R.id.translation_z
    SpringAnimation.SCALE_X -> R.id.scale_x
    SpringAnimation.SCALE_Y -> R.id.scale_y
    SpringAnimation.ROTATION -> R.id.rotation
    SpringAnimation.ROTATION_X -> R.id.rotation_x
    SpringAnimation.ROTATION_Y -> R.id.rotation_y
    SpringAnimation.X -> R.id.x
    SpringAnimation.Y -> R.id.y
    SpringAnimation.Z -> R.id.z
    SpringAnimation.ALPHA -> R.id.alpha
    SpringAnimation.SCROLL_X -> R.id.scroll_x
    SpringAnimation.SCROLL_Y -> R.id.scroll_y
    else -> throw IllegalAccessException("Unknown ViewProperty: $property")
}

/**
 * An end listener that removes itself when the animation ends
 */
fun SpringAnimation.withOneShotEndListener(onEnd: () -> Unit): SpringAnimation = addEndListener(object : DynamicAnimation.OnAnimationEndListener {
    override fun onAnimationEnd(animation: DynamicAnimation<out DynamicAnimation<*>>?, canceled: Boolean, value: Float, velocity: Float) {
        removeEndListener(this)
        onEnd()
    }
})

/**
 * An update listener that removes itself when the animation ends
 */
fun SpringAnimation.withOneShotUpdateListener(
        onUpdate: (value: Float, velocity: Float) -> Unit
): SpringAnimation = apply {
    val listener = DynamicAnimation.OnAnimationUpdateListener { _, value, velocity -> onUpdate(value, velocity) }
    addUpdateListener(listener).withOneShotEndListener { removeUpdateListener(listener) }
}

/**
 * Pops an orphaned [View] over the specified [anchor] using a [PopupWindow]
 */
fun View.popOver(
        anchor: View,
        adjuster: () -> Point = { Point(0, 0) },
        options: PopupWindow.() -> Unit = {}
) {
    require(!this.isAttachedToWindow) { "The View being attached must be an orphan" }
    PopupWindow(this.wrapAtAnchor(anchor, adjuster), MATCH_PARENT, MATCH_PARENT, true).run {
        isOutsideTouchable = true
        contentView.setOnTouchListener { _, _ -> dismiss(); true }
        options(this)
        showAtLocation(anchor, Gravity.START, 0, 0)
    }
}

private fun View.wrapAtAnchor(anchor: View, adjuster: () -> Point): View? = FrameLayout(anchor.context).apply {
    clipChildren = false
    clipToPadding = false
    this@wrapAtAnchor.alignToAnchor(anchor, adjuster)
    addView(this@wrapAtAnchor, ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
}

private fun View.alignToAnchor(anchor: View, adjuster: () -> Point) = intArrayOf(0, 0).run {
    anchor.getLocationInWindow(this)
    doOnLayout {
        val (offsetX, offsetY) = adjuster()
        val x = this[0].toFloat() + offsetX
        val y = this[1].toFloat() + offsetY
        translationX = x; translationY = y
    }
}
