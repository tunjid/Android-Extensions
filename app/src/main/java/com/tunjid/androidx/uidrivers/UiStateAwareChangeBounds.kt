package com.tunjid.androidx.uidrivers

import android.graphics.Rect
import android.transition.ChangeBounds
import android.transition.TransitionValues
import com.tunjid.androidx.core.content.unwrapActivity
import com.tunjid.androidx.view.util.InsetFlags

class UiStateAwareChangeBounds(
        private val initial: UiState?
) : ChangeBounds() {

    override fun captureEndValues(transitionValues: TransitionValues?) {
        super.captureEndValues(transitionValues)

        transitionValues ?: return
        val rect = transitionValues.values[BOUNDS_PROPERTY] as? Rect ?: return

        val current = transitionValues.uiState
        val noInsets = current.insetFlags == InsetFlags.NONE
        val statusBarChanged = changed(initial, current, InsetFlags::hasTopInset)
        val navBarChanged = changed(initial, current, InsetFlags::hasBottomInset)

        // Shared element transitions only seem to break when the content is truly full screen
        val statusBar = if (noInsets && statusBarChanged) GlobalUiDriver.statusBarSize else 0
        val navBar = if (noInsets && navBarChanged) GlobalUiDriver.navBarSize else 0

        val altered = Rect(
                rect.left,
                rect.top + navBar + statusBar,
                rect.right,
                rect.bottom + navBar + statusBar
        )
        transitionValues.values[BOUNDS_PROPERTY] = altered
    }
}

private var TransitionValues.uiState by UiStateDelegate { it.view.context.unwrapActivity }

private const val BOUNDS_PROPERTY = "android:changeBounds:bounds"

private fun <T> changed(
        before: UiState?,
        after: UiState?,
        property: (InsetFlags) -> T
) = before != null
        && after != null
        && property(before.insetFlags) != property(after.insetFlags)