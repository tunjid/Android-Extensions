package com.tunjid.androidx.uidrivers

import android.graphics.Rect
import androidx.transition.ChangeBounds
import androidx.transition.TransitionValues
import com.tunjid.androidx.R

class UiStateAwareChangeBounds(
        private val initial: UiState?
) : ChangeBounds() {

    override fun captureEndValues(transitionValues: TransitionValues) {
        super.captureEndValues(transitionValues)

        val rect = transitionValues.values[BOUNDS_PROPERTY] as? Rect ?: return

        val current = transitionValues.uiState
        val noInsets = current.insetFlags == InsetFlags.NONE
        val toolBarChanged = changed(initial, current) { it.toolbarOverlaps }
        val statusBarChanged = changed(initial, current) { it.insetFlags.hasTopInset }
        val navBarChanged = changed(initial, current) { it.insetFlags.hasBottomInset }

        // Shared element transitions only seem to break when the content is truly full screen
        val statusBar = if (noInsets && statusBarChanged) current.systemUI.static.statusBarSize else 0
        val toolbar = if (noInsets && toolBarChanged) transitionValues.view.resources.getDimensionPixelSize(R.dimen.triple_and_half_margin) else 0
        val navBar = if (noInsets && navBarChanged && !toolBarChanged) current.systemUI.static.navBarSize else 0

        val altered = Rect(
                rect.left,
                rect.top + statusBar + toolbar + navBar,
                rect.right,
                rect.bottom + statusBar + toolbar + navBar
        )
        transitionValues.values[BOUNDS_PROPERTY] = altered
    }
}

private const val BOUNDS_PROPERTY = "android:changeBounds:bounds"

private fun <T> changed(
        before: UiState?,
        after: UiState?,
        property: (UiState) -> T
) = before != null
        && after != null
        && property(before) != property(after)