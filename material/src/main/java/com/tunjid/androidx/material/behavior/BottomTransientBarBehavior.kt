package com.tunjid.androidx.material.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.dynamicanimation.animation.SpringAnimation
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.tunjid.androidx.view.util.spring
import kotlin.math.min

/**
 * Animates a [View] when a [Snackbar] appears.
 *
 *
 * Mostly identical to the old [FloatingActionButton.Behavior] that would animate the FAB
 * upwards when it appears, but with spring animations.
 *Note that this is no longer the Material Design Spec.
 *
 * Created by tj.dahunsi on 4/15/17.
 */
// Constructed via xml
@Suppress("unused")
class BottomTransientBarBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<View>(context, attrs) {

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean =
            dependency is Snackbar.SnackbarLayout

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        if (child.visibility == View.VISIBLE)
            child.spring(SpringAnimation.TRANSLATION_Y)
                    .animateToFinalPosition(getViewTranslationYForSnackbar(parent, child))
        return true
    }

    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: View, dependency: View) {
        if (dependency is Snackbar.SnackbarLayout && child.translationY != 0.0f) child.apply {
            spring(SpringAnimation.TRANSLATION_Y).animateToFinalPosition(0F)
            spring(SpringAnimation.SCALE_X).animateToFinalPosition(1F)
            spring(SpringAnimation.SCALE_Y).animateToFinalPosition(1F)
            spring(SpringAnimation.ALPHA).animateToFinalPosition(1F)
        }
    }

    private fun getViewTranslationYForSnackbar(parent: CoordinatorLayout, child: View): Float {
        var minOffset = 0.0f
        val dependencies = parent.getDependencies(child)
        var i = 0

        val z = dependencies.size
        while (i < z) {
            val view = dependencies[i] as View
            if (view is Snackbar.SnackbarLayout && parent.doViewsOverlap(child, view)) {
                minOffset = min(minOffset, view.getTranslationY() - view.getHeight().toFloat())
            }
            ++i
        }

        return minOffset
    }
}
