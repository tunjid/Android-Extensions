package com.tunjid.androidx.view.animator


import android.content.Context.WINDOW_SERVICE
import android.graphics.Point
import android.view.View
import android.view.WindowManager
import androidx.annotation.IntDef
import androidx.core.view.doOnPreDraw
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.tunjid.androidx.view.util.spring
import com.tunjid.androidx.view.util.withOneShotEndListener

/**
 * Translates a view offscreen, useful for adding the quick return pattern to a view.
 *
 *
 * Created by tj.dahunsi on 2/19/16.
 */
class ViewHider<T : View> private constructor(
        val view: T,
        private val listener: Listener,
        @param:HideDirection @field:HideDirection
        private val direction: Int,
        private val options: SpringAnimation.() -> Unit) {

    private var isVisible = true

    private val startAction: () -> Unit = { if (isVisible) view.visibility = View.VISIBLE }
    private val endAction: () -> Unit = { if (!isVisible) view.visibility = View.GONE }

    private val spring: SpringAnimation get() = view.spring(
            if (direction == LEFT || direction == RIGHT) SpringAnimation.TRANSLATION_X
            else SpringAnimation.TRANSLATION_Y
    )

    // These calculations don't take the status bar into account, unlikely to matter however
    private val displacement: Float
        get() {
            if (isVisible) return 0F

            val location = IntArray(2).apply { view.getLocationInWindow(this) }
            val displaySize = Point().apply {
                (view.context.getSystemService(WINDOW_SERVICE) as? WindowManager)
                        ?.defaultDisplay
                        ?.getSize(this)
                        ?: return 0F
            }

            return when (direction) {
                LEFT -> -(location[0] + view.width)
                TOP -> -(location[1] + view.height)
                RIGHT -> displaySize.x - location[0]
                BOTTOM -> displaySize.y - location[1]
                else -> throw IllegalArgumentException("Invalid direction")
            }.toFloat()
        }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(LEFT, TOP, RIGHT, BOTTOM)
    annotation class HideDirection

    fun show() = toggle(true)

    fun hide() = toggle(false)

    fun configure(options:  SpringForce.() -> Unit) = options.invoke(spring.spring)

    private fun toggle(visible: Boolean) {
        if (this.isVisible == visible) return

        // View hasn't been laid out yet and has it's observer attached
        if (!view.isLaidOut) return view.doOnPreDraw { toggle(visible) }.let { Unit }

        this.isVisible = visible

        spring
                .apply {
                    options.invoke(this)
                    for (runnable in listener.startActions) runnable.invoke()
                }
                .withOneShotEndListener { for (runnable in listener.endActions) runnable.invoke() }
                .animateToFinalPosition(displacement)
    }

    class Builder<T : View> internal constructor(private val view: T) {

        @HideDirection
        private var direction = BOTTOM
        private var options: SpringAnimation.() -> Unit = {}
        private val listener = Listener()

        fun setDirection(@HideDirection direction: Int): Builder<T> = apply { this.direction = direction }

        fun addOptions(options: SpringAnimation.() -> Unit): Builder<T> = apply { this.options = options }

        fun addStartAction(action: () -> Unit): Builder<T> = apply { listener.startActions.add(action) }

        fun addEndAction(action: () -> Unit): Builder<T> = apply { listener.endActions.add(action) }

        fun build(): ViewHider<T> = ViewHider(view, listener, direction, options).apply {
            listener.startActions.add(0, startAction)
            listener.endActions.add(0, endAction)
        }
    }

    private class Listener internal constructor() {
        val startActions = mutableListOf<() -> Unit>()
        val endActions = mutableListOf<() -> Unit>()
    }

    companion object {

        const val LEFT = 0
        const val TOP = 1
        const val RIGHT = 2
        const val BOTTOM = 3

        fun <T : View> of(view: T): Builder<T> = Builder(view)
    }
}