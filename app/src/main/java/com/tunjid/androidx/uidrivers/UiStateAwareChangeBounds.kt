package com.tunjid.androidx.uidrivers

import android.graphics.Rect
import android.transition.ChangeBounds
import android.transition.TransitionValues
import com.tunjid.androidx.R

class UiStateAwareChangeBounds(
        before: UiState?,
        after: UiState?
) : ChangeBounds() {

    private val statusBarChanged = changed(before, after) { it.insetFlags.hasTopInset }
    private val toolbarChanged = changed(before, after, UiState::toolbarOverlaps)

    override fun captureEndValues(transitionValues: TransitionValues?) {
        super.captureEndValues(transitionValues)
        transitionValues ?: return
        val context = transitionValues.view.context

        val rect = transitionValues.values[BOUNDS_PROPERTY] as? Rect ?: return

        val statusBar = if (statusBarChanged) GlobalUiDriver.statusBarSize else 0
        val toolbar = if (toolbarChanged) context.resources.getDimensionPixelSize(R.dimen.triple_and_half_margin) else 0

        val altered = Rect(
                rect.left,
                rect.top + toolbar + statusBar,
                rect.right,
                rect.bottom + toolbar + statusBar
        )
        transitionValues.values[BOUNDS_PROPERTY] = altered
    }
}

private const val BOUNDS_PROPERTY = "android:changeBounds:bounds"

private fun <T> changed(before: UiState?,
                        after: UiState?, property: (UiState) -> T) =
        before != null
                && after != null
                && property(before) != property(after)