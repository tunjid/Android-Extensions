package com.tunjid.androidbootstrap.view.animator


import android.content.Context.WINDOW_SERVICE
import android.graphics.Point
import android.view.View
import android.view.WindowManager
import androidx.annotation.IntDef
import androidx.core.view.doOnPreDraw
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import com.tunjid.androidbootstrap.view.util.spring

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
        private val options: SpringAnimation.() -> Unit) {

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

        // View hasn't been laid out yet and has it's observer attached
        if (!view.isLaidOut) return view.doOnPreDraw { toggle(visible) }

        this.isVisible = visible
        val displacement = (if (visible) 0 else getDistanceOffscreen()).toFloat()

        view.spring(
                if (direction == LEFT || direction == RIGHT) SpringAnimation.TRANSLATION_X
                else SpringAnimation.TRANSLATION_Y
        )
                .apply {
                    options.invoke(this)
                    for (runnable in listener.startRunnables) runnable.invoke()
                }
                .withOneShotEndListener { for (runnable in listener.endRunnables) runnable.invoke() }
                .animateToFinalPosition(displacement)
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
        private var options: SpringAnimation.() -> Unit = {}
        private val listener = Listener()

        fun setDirection(@HideDirection direction: Int): Builder = apply { this.direction = direction }

        fun addOptions(options: SpringAnimation.() -> Unit): Builder = apply { this.options = options }

        fun addStartRunnable(runnable: () -> Unit): Builder = apply { listener.startRunnables.add(runnable) }

        fun addEndRunnable(runnable: () -> Unit): Builder = apply { listener.endRunnables.add(runnable) }

        fun build(): ViewHider = ViewHider(view, listener, direction, options).apply {
            listener.startRunnables.add(0, startRunnable)
            listener.endRunnables.add(0, endRunnable)
        }
    }

    private class Listener internal constructor() {
        val startRunnables = mutableListOf<() -> Unit>()
        val endRunnables = mutableListOf<() -> Unit>()
    }

    private fun SpringAnimation.withOneShotEndListener(onEnd: (canceled: Boolean) -> Unit) = apply {
        addEndListener(object : DynamicAnimation.OnAnimationEndListener {
            override fun onAnimationEnd(animation: DynamicAnimation<out DynamicAnimation<*>>?, canceled: Boolean, value: Float, velocity: Float) {
                removeEndListener(this)
                onEnd(canceled)
            }
        })
    }

    companion object {

        const val LEFT = 0
        const val TOP = 1
        const val RIGHT = 2
        const val BOTTOM = 3

        fun of(view: View): Builder = Builder(view)
    }
}