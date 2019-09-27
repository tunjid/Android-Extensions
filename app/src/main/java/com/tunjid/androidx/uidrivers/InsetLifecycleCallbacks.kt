package com.tunjid.androidx.uidrivers

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEach
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.tunjid.androidx.R
import com.tunjid.androidx.core.components.Navigator
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.marginLayoutParams
import kotlin.math.max

class InsetLifecycleCallbacks(
        private val stackNavigatorSource: () -> Navigator?,
        private val parentContainer: ViewGroup,
        private val contentContainer: FragmentContainerView,
        private val coordinatorLayout: CoordinatorLayout,
        private val toolbar: Toolbar,
        private val topInsetView: View,
        private val bottomInsetView: View,
        private val keyboardPadding: View
) : FragmentManager.FragmentLifecycleCallbacks() {

    private var leftInset: Int = 0
    private var rightInset: Int = 0
    private var insetsApplied: Boolean = false
    private var lastInsetDispatch: InsetDispatch? = InsetDispatch()

    init {
        ViewCompat.setOnApplyWindowInsetsListener(parentContainer) { _, insets -> consumeSystemInsets(insets) }
    }

    override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) = adjustInsetForFragment(f)

    override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) =
            onFragmentViewCreated(v, f)

    private fun isNotInCurrentFragmentContainer(fragment: Fragment): Boolean =
            stackNavigatorSource()?.run { fragment.id != containerId } ?: true

    private fun consumeSystemInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        if (this.insetsApplied) return insets

        topInset = insets.systemWindowInsetTop
        leftInset = insets.systemWindowInsetLeft
        rightInset = insets.systemWindowInsetRight
        bottomInset = insets.systemWindowInsetBottom

        topInsetView.layoutParams.height = topInset
        bottomInsetView.layoutParams.height = bottomInset

        adjustInsetForFragment(stackNavigatorSource()?.currentFragment)

        this.insetsApplied = true
        return insets
    }

    private fun onFragmentViewCreated(v: View, fragment: Fragment) {
        if (fragment !is InsetProvider || isNotInCurrentFragmentContainer(fragment)) return
        adjustInsetForFragment(fragment)

        ViewCompat.setOnApplyWindowInsetsListener(v) { _, insets -> consumeFragmentInsets(insets) }
    }

    private fun consumeFragmentInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        val old = contentContainer.paddingBottom
        var new = insets.systemWindowInsetBottom - bottomInset
        if (new != bottomInset) new -= parentContainer.resources.getDimensionPixelSize(R.dimen.triple_and_half_margin)

        new = max(new, 0)

        if (old != new) TransitionManager.beginDelayedTransition(parentContainer, AutoTransition().apply {
            duration = ANIMATION_DURATION.toLong()
            coordinatorLayout.forEach { addTarget(it) }
            addTarget(coordinatorLayout) // Animate coordinator and its children, mainly the FAB
        })

        contentContainer.updatePadding(bottom = new)
        keyboardPadding.layoutParams.height = if (new != 0) new else 1 // 0 breaks animations

        return insets
    }

    @SuppressLint("InlinedApi")
    private fun adjustInsetForFragment(fragment: Fragment?) {
        if (fragment !is InsetProvider || isNotInCurrentFragmentContainer(fragment)) return

        fragment.insetFlags.dispatch {
            if (insetFlags == null || lastInsetDispatch == this) return

            toolbar.marginLayoutParams.topMargin = if (insetFlags.hasTopInset) 0 else topInset
            coordinatorLayout.marginLayoutParams.bottomMargin = if (insetFlags.hasBottomInset) 0 else bottomInset

            TransitionManager.beginDelayedTransition(parentContainer, AutoTransition()
                    .setDuration(ANIMATION_DURATION.toLong())
                    .addTarget(contentContainer) // Animate inset change
            )

            topInsetView.visibility = if (insetFlags.hasTopInset) View.VISIBLE else View.GONE
            bottomInsetView.visibility = if (insetFlags.hasBottomInset) View.VISIBLE else View.GONE

            parentContainer.setPadding(
                    if (insetFlags.hasLeftInset) this.leftInset else 0,
                    0,
                    if (insetFlags.hasRightInset) this.rightInset else 0,
                    0)

            lastInsetDispatch = this
        }
    }

    private inline fun InsetFlags.dispatch(receiver: InsetDispatch.() -> Unit) =
            receiver.invoke(InsetDispatch(leftInset, topInset, rightInset, bottomInset, this))

    companion object {
        const val ANIMATION_DURATION = 300

        var topInset: Int = 0
        var bottomInset: Int = 0
    }

    private data class InsetDispatch(
            val leftInset: Int = 0,
            val topInset: Int = 0,
            val rightInset: Int = 0,
            val bottomInset: Int = 0,
            val insetFlags: InsetFlags? = null
    )
}