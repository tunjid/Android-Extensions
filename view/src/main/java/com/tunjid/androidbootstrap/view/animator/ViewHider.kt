package com.tunjid.androidbootstrap.view.animator


import android.content.Context.WINDOW_SERVICE
import android.graphics.Point
import android.view.View
import android.view.WindowManager
import androidx.annotation.IntDef
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener
import androidx.core.view.doOnPreDraw
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.springAnimationOf
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

/**
 * Translates a view offscreen, useful for adding the quick return pattern to a view.
 *
 *
 * Created by tj.dahunsi on 2/19/16.
 */
class ViewHider private constructor(
        private val view: View,
        private val listener: Listener,
        @param:HideDirection @field:HideDirection
        private val direction: Int,
        private val duration: Long) {

    private var isVisible = true

    private val startRunnable: () -> Unit = { if (isVisible) view.visibility = View.VISIBLE }
    private val endRunnable: () -> Unit = { if (!isVisible) view.visibility = View.GONE }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(LEFT, TOP, RIGHT, BOTTOM)
    annotation class HideDirection

    fun show() = toggle(true)

    fun hide() = toggle(false)

    private fun toggle(visible: Boolean) {
        if (this.isVisible == visible) return

//        springAnimationOf(::setDropperX, ::getDropperX, point.x).apply {
//            spring.stiffness = SpringForce.STIFFNESS_MEDIUM
//            spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
//            start()
//        }

        // View hasn't been laid out yet and has it's observer attached
        if (!view.isLaidOut) return view.doOnPreDraw { toggle(visible) }

        this.isVisible = visible
        val displacement = (if (visible) 0 else getDistanceOffscreen()).toFloat()

        val animator = ViewCompat.animate(view)
                .setDuration(duration)
                .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                .setListener(listener)

        if (direction == LEFT || direction == RIGHT) animator.translationX(displacement)
        else animator.translationY(displacement)

        animator.start()
    }

    // These calculations don't take the status bar into account, unlikely to matter however
    private fun getDistanceOffscreen(): Int {
        val location = IntArray(2)
        val displaySize = Point()

        val manager = view.context.getSystemService(WINDOW_SERVICE) as? WindowManager
                ?: return 0

        view.getLocationInWindow(location)
        manager.defaultDisplay.getSize(displaySize)

        return when (direction) {
            LEFT -> -(location[0] + view.width)
            TOP -> -(location[1] + view.height)
            RIGHT -> displaySize.x - location[0]
            BOTTOM -> displaySize.y - location[1]
            else -> throw IllegalArgumentException("Invalid direction")
        }
    }

    class Builder internal constructor(private val view: View) {

        @HideDirection
        private var direction = BOTTOM
        private var duration = 200L
        private val listener = Listener()

        fun setDirection(@HideDirection direction: Int): Builder = apply { this.direction = direction }

        fun setDuration(duration: Long): Builder = apply { this.duration = duration }

        fun addStartRunnable(runnable: () -> Unit): Builder = apply { listener.startRunnables.add(runnable) }

        fun addEndRunnable(runnable: () -> Unit): Builder = apply { listener.endRunnables.add(runnable) }

        fun build(): ViewHider = ViewHider(view, listener, direction, duration).apply {
            listener.startRunnables.add(0, startRunnable)
            listener.endRunnables.add(0, endRunnable)
        }
    }

    private class Listener internal constructor() : ViewPropertyAnimatorListener {

        val startRunnables = mutableListOf<() -> Unit>()
        val endRunnables = mutableListOf<() -> Unit>()

        override fun onAnimationStart(view: View) {
            if (startRunnables.isNotEmpty()) for (runnable in startRunnables) runnable.invoke()
        }

        override fun onAnimationEnd(view: View) {
            if (endRunnables.isNotEmpty()) for (runnable in endRunnables) runnable.invoke()
        }

        override fun onAnimationCancel(view: View) {}
    }

    companion object {

        private val FAST_OUT_SLOW_IN_INTERPOLATOR = FastOutSlowInInterpolator()

        const val LEFT = 0
        const val TOP = 1
        const val RIGHT = 2
        const val BOTTOM = 3

        fun of(view: View): Builder = Builder(view)
    }
}