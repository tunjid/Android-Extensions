package com.tunjid.androidx.uidrivers

import android.animation.ArgbEvaluator
import android.animation.IntEvaluator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.transition.Transition
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import androidx.core.transition.doOnEnd
import androidx.dynamicanimation.animation.DynamicAnimation
import com.google.android.material.button.MaterialButton
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.material.animator.speedDial
import com.tunjid.androidx.view.util.spring
import com.tunjid.androidx.view.util.withUpdateListener

class StateAwareSpeedDial(
        private val uiController: GlobalUiController,
        @ColorInt private val tint: Int = Color.BLUE,
        private val items: List<Pair<CharSequence?, Drawable>>,
        private val dismissListener: (Int?) -> Unit
) : View.OnClickListener {

    private var button: MaterialButton? = null

    override fun onClick(button: View?) {
        if (button !is MaterialButton) return

        this.button = button

        if (uiController.uiState.fabExtended) {
            uiController.uiState = uiController.uiState.copy(
                    fabExtended = false,
                    fabTransitionOptions = { onEnd() }
            )
            return
        }

        val context = button.context
        val colorFrom = context.themeColorAt(R.attr.colorPrimary)
        val colorTo = context.themeColorAt(R.attr.colorAccent).run {
            Color.argb(20, Color.red(this), Color.green(this), Color.blue(this))
        }

        button.strokeColor = ColorStateList.valueOf(context.themeColorAt(R.attr.colorAccent))

        button.spring(DynamicAnimation.ROTATION)
                .withUpdateListener(range = 90F.rangeTo(180F)) { button.icon = button.context.drawableAt(R.drawable.ic_unfold_less_24dp) }
                .animateToFinalPosition(225F)

        val animators = listOf(
                roundAbout(colorFrom, colorTo, ArgbEvaluator(), { button.backgroundTintList!!.defaultColor }) { button.backgroundTintList = ColorStateList.valueOf(it as Int) },
                roundAbout(0, context.resources.getDimensionPixelSize(R.dimen.quarter_margin), IntEvaluator(), button::getStrokeWidth, button::setStrokeWidth)
        )

        speedDial(anchor = button, tint = tint, items = items, dismissListener = { index ->
            animators.forEach(ValueAnimator::cancel)
            button.spring(DynamicAnimation.ROTATION)
                    .withUpdateListener(range = 90F.rangeTo(180F)) { button.icon = button.context.drawableAt(R.drawable.ic_unfold_more_24dp) }
                    .animateToFinalPosition(0F)
            if (index == null) {
                roundAbout(colorFrom, colorTo, ArgbEvaluator(), { button.backgroundTintList!!.defaultColor }) { button.backgroundTintList = ColorStateList.valueOf(it as Int) }
                roundAbout(0, context.resources.getDimensionPixelSize(R.dimen.quarter_margin), IntEvaluator(), button::getStrokeWidth, button::setStrokeWidth)
            }
            dismissListener(index)
        })
    }

    private fun Transition.onEnd() = doOnEnd {
        if (!uiController.uiState.fabExtended && button != null) {
            onClick(button)
            uiController.uiState = uiController.uiState.copy(fabTransitionOptions = null)
        }
    }.let { Unit }

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
}