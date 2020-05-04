package com.tunjid.androidx.uidrivers

import android.graphics.Rect
import android.transition.ChangeBounds
import android.transition.TransitionValues
import com.tunjid.androidx.view.util.InsetFlags

class UiStateAwareChangeBounds(
        before: UiState?,
        after: UiState?
) : ChangeBounds() {

    private val statusBarChanged = changed(before, after, InsetFlags::hasTopInset)
    private val navBarChanged = changed(before, after, InsetFlags::hasBottomInset)

    override fun captureEndValues(transitionValues: TransitionValues?) {
        super.captureEndValues(transitionValues)

        transitionValues ?: return
        val rect = transitionValues.values[BOUNDS_PROPERTY] as? Rect ?: return

        val statusBar = if (statusBarChanged) GlobalUiDriver.statusBarSize else 0
        val navBar = if (navBarChanged) GlobalUiDriver.navBarSize else 0

        val altered = Rect(
                rect.left,
                rect.top + navBar + statusBar,
                rect.right,
                rect.bottom + navBar + statusBar
        )
        transitionValues.values[BOUNDS_PROPERTY] = altered
    }
}

private const val BOUNDS_PROPERTY = "android:changeBounds:bounds"

private fun <T> changed(
        before: UiState?,
        after: UiState?,
        property: (InsetFlags) -> T
) = before != null
        && after != null
        && property(before.insetFlags) != property(after.insetFlags)