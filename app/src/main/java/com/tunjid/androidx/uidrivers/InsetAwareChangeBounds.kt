package com.tunjid.androidx.uidrivers

import android.graphics.Rect
import android.transition.ChangeBounds
import android.transition.TransitionValues
import android.util.Log
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
        Log.i("TEST", "Insets changed: $insetsChanged")
        transitionValues ?: return
        val context = transitionValues.view.context

        val rect = transitionValues.values[PROPNAME_BOUNDS] as? Rect ?: return
        val altered = Rect(
                rect.left,
                if (insetsChanged) rect.top + context.resources.getDimensionPixelSize(R.dimen.triple_and_half_margin) else rect.top,
                rect.right,
                if (insetsChanged) rect.bottom + context.resources.getDimensionPixelSize(R.dimen.triple_and_half_margin) else rect.bottom
        )
        transitionValues.values[PROPNAME_BOUNDS] = altered
    }
}

private const val PROPNAME_BOUNDS = "android:changeBounds:bounds"
