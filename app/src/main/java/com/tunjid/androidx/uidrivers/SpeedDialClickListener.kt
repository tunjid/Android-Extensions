package com.tunjid.androidx.uidrivers

import android.animation.ArgbEvaluator
import android.animation.IntEvaluator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import com.google.android.material.button.MaterialButton
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.material.animator.speedDial
import com.tunjid.androidx.view.util.spring
import com.tunjid.androidx.view.util.withOneShotEndListener
import com.tunjid.androidx.view.util.withOneShotUpdateListener

class SpeedDialClickListener(
        @ColorInt private val tint: Int = Color.BLUE,
        private val items: List<Pair<CharSequence?, Drawable>>,
        private val runGuard: (View) -> Boolean,
        private val dismissListener: (Int?) -> Unit
) : View.OnClickListener {

    override fun onClick(button: View?) {
        if (button !is MaterialButton || !runGuard(button)) return

        val rotationSpring = button.spring(DynamicAnimation.ROTATION)
        if (rotationSpring.isRunning) return

        val flipRange = 90F..180F
        val context = button.context
        val colorFrom = context.themeColorAt(R.attr.colorPrimary)
        val colorTo = context.themeColorAt(R.attr.colorAccent).run {
            Color.argb(20, Color.red(this), Color.green(this), Color.blue(this))
        }

        button.strokeColor = ColorStateList.valueOf(context.themeColorAt(R.attr.colorAccent))

        rotationSpring.apply {
            doInRange(flipRange) { button.icon = button.context.drawableAt(R.drawable.ic_unfold_less_24dp) }
            animateToFinalPosition(225F) // re-targeting will be idempotent
        }

        val animators = button.haloEffects(colorFrom, colorTo, context)

        speedDial(anchor = button, buttonTint = tint, items = items, dismissListener = dismiss@{ index ->
            animators.forEach(ValueAnimator::cancel)

            rotationSpring.apply {
                if (!isRunning) {
                    doInRange(flipRange) { button.icon = button.context.drawableAt(R.drawable.ic_unfold_more_24dp) }
                    withOneShotEndListener { dismissListener(index) }
                }

                animateToFinalPosition(0F)
            }

            if (index == null) button.haloEffects(colorFrom, colorTo, context)
        })
    }

    private fun MaterialButton.haloEffects(colorFrom: Int, colorTo: Int, context: Context) = listOf(
            roundAbout(colorFrom, colorTo, ArgbEvaluator(), { backgroundTintList!!.defaultColor }) { backgroundTintList = ColorStateList.valueOf(it as Int) },
            roundAbout(0, context.resources.getDimensionPixelSize(R.dimen.quarter_margin), IntEvaluator(), this::getStrokeWidth, this::setStrokeWidth)
    )

    private inline fun <reified T> roundAbout(
            originalPosition: T,
            nextPosition: T,
            evaluator: TypeEvaluator<T>,
            crossinline getter: () -> T,
            crossinline setter: (T) -> Unit
    ) = ValueAnimator.ofObject(evaluator, getter(), nextPosition).apply {
        duration = 200L
        addUpdateListener { setter(it.animatedValue as T) }
        doOnEnd {
            setObjectValues(getter(), originalPosition)
            removeAllListeners()
            start()
        }
        start()
    }

    private fun SpringAnimation.doInRange(range: ClosedRange<Float>, action: () -> Unit) = apply {
        var flipped = false
        withOneShotUpdateListener update@{ value, _ ->
            if (flipped || !range.contains(value)) return@update
            action()
            flipped = true
        }
    }
}