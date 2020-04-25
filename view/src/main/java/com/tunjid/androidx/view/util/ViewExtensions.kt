package com.tunjid.androidx.view.util

import android.graphics.Color
import android.graphics.Point
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.annotation.ColorInt
import androidx.core.graphics.component1
import androidx.core.graphics.component2
import androidx.core.view.doOnLayout
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.name
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
        property: FloatPropertyCompat<View>,
        stiffness: Float = SpringForce.STIFFNESS_MEDIUM,
        damping: Float = SpringForce.DAMPING_RATIO_NO_BOUNCY,
        startVelocity: Float? = null
): SpringAnimation {
    @Suppress("UNCHECKED_CAST") val propertyMap =
            getTag(R.id.spring_animation_property_map) as? MutableMap<String, SpringAnimation>
                    ?: mutableMapOf<String, SpringAnimation>().also { setTag(R.id.spring_animation_property_map, it) }

    val springAnim = propertyMap[property.name]
            ?: SpringAnimation(this, property).also { propertyMap[property.name] = it; Log.i("TEST","CREATING A ${property.name} SPRING") }

    springAnim.spring = (springAnim.spring ?: SpringForce()).apply {
        this.dampingRatio = damping
        this.stiffness = stiffness
    }

    startVelocity?.let { springAnim.setStartVelocity(it) }

    return springAnim
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
 * Returns the innermost focused child within this [View] hierarchy, or null if this is not a [ViewGroup]
 */
val View.innermostFocusedChild: View?
    get() {
        if (this !is ViewGroup) return null
        val focused = focusedChild
        return focused?.innermostFocusedChild ?: focused
    }

/**
 * Pops an orphaned [View] over the specified [anchor] using a [PopupWindow]
 */
fun View.popOver(
        @ColorInt backgroundColor: Int = Color.argb(60, 0, 0, 0),
        anchor: View,
        adjuster: () -> Point = { Point(0, 0) },
        options: PopupWindow.() -> Unit = {}
) {
    require(!this.isAttachedToWindow) { "The View being attached must be an orphan" }
    PopupWindow(this.wrapAtAnchor(backgroundColor, anchor, adjuster), MATCH_PARENT, MATCH_PARENT, true).run {
        isOutsideTouchable = true
        contentView.setOnTouchListener { _, _ -> dismiss(); true }
        options(this)
        showAtLocation(anchor, Gravity.START, 0, 0)
    }
}

private fun View.wrapAtAnchor(@ColorInt backgroundColor: Int,
                              anchor: View, adjuster: () -> Point
): View? = FrameLayout(anchor.context).apply {
    clipChildren = false
    clipToPadding = false
    this@wrapAtAnchor.alignToAnchor(anchor, adjuster)
    setBackgroundColor(backgroundColor)
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
