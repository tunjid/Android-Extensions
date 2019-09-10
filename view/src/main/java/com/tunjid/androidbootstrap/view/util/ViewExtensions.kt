package com.tunjid.androidbootstrap.view.util

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.tunjid.androidbootstrap.view.R

var TOOLBAR_ANIM_DELAY = 200L

var Toolbar.animDuration: Long
    get() = TOOLBAR_ANIM_DELAY
    set(value) {
        TOOLBAR_ANIM_DELAY = value
    }

fun Toolbar.update(title: CharSequence, @MenuRes menu: Int = 0) {
    if (visibility != View.VISIBLE || this.title == null) {
        setTitle(title)
        replaceMenu(menu)
    } else for (i in 0 until childCount) {
        val child = getChildAt(i)
        if (child is ImageView) continue

        child.animate().alpha(0F).setDuration(animDuration).withEndAction {
            if (child is TextView) setTitle(title)
            else if (child is ActionMenuView) replaceMenu(menu)

            child.animate()
                    .setDuration(animDuration)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .alpha(1F)
                    .start()
        }.start()
    }
}

fun Toolbar.replaceMenu(menu: Int) {
    this.menu.clear()
    if (menu != 0) inflateMenu(menu)
}

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