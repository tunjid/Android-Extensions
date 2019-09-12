package com.tunjid.androidbootstrap

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import com.tunjid.androidbootstrap.activities.MainActivity
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.core.components.StackNavigator
import com.tunjid.androidbootstrap.view.util.ViewUtil
import kotlin.math.max

class InsetLifecycleCallbacks(
        private val stackNavigatorSource: () -> StackNavigator?,
        private val parentContainer: ViewGroup,
        private val contentContainer: FragmentContainerView,
        private val coordinatorLayout: CoordinatorLayout,
        private val toolbar: Toolbar,
        private val topInsetView: View,
        private val bottomInsetView: View,
        private val keyboardPadding: View
) : FragmentManager.FragmentLifecycleCallbacks() {

    private var insetsApplied: Boolean = false
    private var leftInset: Int = 0
    private var rightInset: Int = 0

    init {
        ViewCompat.setOnApplyWindowInsetsListener(this.parentContainer) { _, insets -> consumeSystemInsets(insets) }
    }

    fun showSnackBar(consumer: (Snackbar) -> Unit) {
        val snackbar = Snackbar.make(coordinatorLayout, "", Snackbar.LENGTH_SHORT)

        // Necessary to remove snackbar padding for keyboard on older versions of Android
        ViewCompat.setOnApplyWindowInsetsListener(snackbar.view) { _, insets -> insets }
        consumer.invoke(snackbar)
        snackbar.show()
    }

    override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) = adjustInsetForFragment(f)

    override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) =
            onFragmentViewCreated(v, f)

    private fun isNotInCurrentFragmentContainer(fragment: Fragment): Boolean =
            stackNavigatorSource()?.run { fragment.id != containerId } ?: true

    private fun consumeSystemInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        if (this.insetsApplied) return insets

        MainActivity.topInset = insets.systemWindowInsetTop
        leftInset = insets.systemWindowInsetLeft
        rightInset = insets.systemWindowInsetRight
        MainActivity.bottomInset = insets.systemWindowInsetBottom

        topInsetView.layoutParams.height = MainActivity.topInset
        bottomInsetView.layoutParams.height = MainActivity.bottomInset

        adjustInsetForFragment(stackNavigatorSource()?.currentFragment)

        this.insetsApplied = true
        return insets
    }

    private fun consumeFragmentInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        var padding = insets.systemWindowInsetBottom - MainActivity.bottomInset
        if (padding != MainActivity.bottomInset) padding -= parentContainer.resources.getDimensionPixelSize(R.dimen.triple_and_half_margin)

        padding = max(padding, 1)

        contentContainer.updatePadding(bottom = padding)
        keyboardPadding.layoutParams.height = padding

        return insets
    }

    @SuppressLint("InlinedApi")
    private fun adjustInsetForFragment(fragment: Fragment?) {
        if (fragment !is AppBaseFragment || isNotInCurrentFragmentContainer(fragment)) return

        val insetFlags = fragment.insetFlags
        ViewUtil.getLayoutParams(toolbar).topMargin = if (insetFlags.hasTopInset) 0 else MainActivity.topInset
        ViewUtil.getLayoutParams(coordinatorLayout).bottomMargin = if (insetFlags.hasBottomInset) 0 else MainActivity.bottomInset

        TransitionManager.beginDelayedTransition(parentContainer, AutoTransition()
                .setDuration(MainActivity.ANIMATION_DURATION.toLong())
                .addTarget(R.id.content_container)
        )

        topInsetView.visibility = if (insetFlags.hasTopInset) View.VISIBLE else View.GONE
        bottomInsetView.visibility = if (insetFlags.hasBottomInset) View.VISIBLE else View.GONE

        parentContainer.setPadding(
                if (insetFlags.hasLeftInset) this.leftInset else 0,
                0,
                if (insetFlags.hasRightInset) this.rightInset else 0,
                0)
    }

    private fun onFragmentViewCreated(v: View, fragment: Fragment) {
        if (fragment !is AppBaseFragment || isNotInCurrentFragmentContainer(fragment)) return
        if (fragment.restoredFromBackStack()) adjustInsetForFragment(fragment)

        ViewCompat.setOnApplyWindowInsetsListener(v) { _, insets -> consumeFragmentInsets(insets) }
    }
}