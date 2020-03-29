package com.tunjid.androidx.uidrivers

import android.graphics.Rect
import android.transition.ChangeBounds
import android.transition.TransitionValues
import com.tunjid.androidx.R

class InsetAwareChangeBounds(
        before: UiState?,
        after: UiState?
) : ChangeBounds() {

    private val insetsChanged = before != null
            && after != null
            && before.showsBottomNav != after.showsBottomNav

    override fun captureEndValues(transitionValues: TransitionValues?) {
        super.captureEndValues(transitionValues)
        transitionValues ?: return
        val context = transitionValues.view.context

        val rect = transitionValues.values[BOUNDS_PROPERTY] as? Rect ?: return
        val altered = Rect(
                rect.left,
                if (insetsChanged) rect.top + context.resources.getDimensionPixelSize(R.dimen.triple_and_half_margin) else rect.top,
                rect.right,
                if (insetsChanged) rect.bottom + context.resources.getDimensionPixelSize(R.dimen.triple_and_half_margin) else rect.bottom
        )
        transitionValues.values[BOUNDS_PROPERTY] = altered
    }
}

private const val BOUNDS_PROPERTY = "android:changeBounds:bounds"
