package com.tunjid.androidx.uidrivers

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.springAnimationOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import com.tunjid.androidx.R
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.marginLayoutParams

class InsetLifecycleCallbacks(
        globalUiController: GlobalUiController,
        private val stackNavigatorSource: () -> Navigator?,
        private val parentContainer: ViewGroup,
        private val contentContainer: FragmentContainerView,
        private val coordinatorLayout: CoordinatorLayout,
        private val toolbar: Toolbar,
        private val bottomInsetView: View,
        private val bottomNavHeightGetter: () -> Int
) : FragmentManager.FragmentLifecycleCallbacks(), GlobalUiController by globalUiController {

    private var leftInset: Int = 0
    private var rightInset: Int = 0
    private var insetsApplied: Boolean = false
    private var lastInsetDispatch: InsetDispatch? = InsetDispatch()

    init {
        ViewCompat.setOnApplyWindowInsetsListener(parentContainer) { _, insets -> onInsetsApplied(insets) }
    }

    override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) =
            onFragmentViewCreated(v, f)

    private fun isNotInCurrentFragmentContainer(fragment: Fragment): Boolean =
            stackNavigatorSource()?.run { fragment.id != containerId } ?: true

    private fun onInsetsApplied(insets: WindowInsetsCompat): WindowInsetsCompat {
        if (this.insetsApplied) return insets

        topInset = insets.systemWindowInsetTop
        leftInset = insets.systemWindowInsetLeft
        rightInset = insets.systemWindowInsetRight
        bottomInset = insets.systemWindowInsetBottom

        toolbar.marginLayoutParams.topMargin = topInset
        bottomInsetView.layoutParams.height = bottomInset

        adjustInsetForFragment(stackNavigatorSource()?.current)

        this.insetsApplied = true
        return insets
    }

    private fun onFragmentViewCreated(v: View, fragment: Fragment) {
        if (fragment !is InsetProvider || isNotInCurrentFragmentContainer(fragment)) return
        adjustInsetForFragment(fragment)

        ViewCompat.setOnApplyWindowInsetsListener(v) { _, insets -> consumeFragmentInsets(insets) }
    }

    private fun consumeFragmentInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        withSpring(coordinatorInsetReducer(insets.systemWindowInsetBottom), coordinatorLayout)
        withSpring(contentInsetReducer(insets.systemWindowInsetBottom), contentContainer) {
            addEndListener { _, _, _, _ ->
                val input = contentContainer.innermostFocusedChild as? EditText
                        ?: return@addEndListener
                input.text = input.text // Scroll to text that has focus
            }
        }

        return insets
    }

    @SuppressLint("InlinedApi")
    fun adjustInsetForFragment(fragment: Fragment?) {
        if (fragment !is InsetProvider || isNotInCurrentFragmentContainer(fragment)) return

        fragment.insetFlags.dispatch(fragment.tag) {
            if (insetFlags == null || lastInsetDispatch == this) return

            bottomInsetView.visibility = if (insetFlags.hasBottomInset) View.VISIBLE else View.GONE

            parentContainer.updatePadding(
                    left = if (insetFlags.hasLeftInset) this.leftInset else 0,
                    right = if (insetFlags.hasRightInset) this.rightInset else 0
            )

            val topPadding = if (insetFlags.hasTopInset) topInset else 0
            val bottomPadding = bottomNavHeightGetter() + if (insetFlags.hasBottomInset) bottomInset else 0

            fragment.view?.updatePadding(top = topPadding, bottom = bottomPadding)

            lastInsetDispatch = this
        }
    }

    private inline fun InsetFlags.dispatch(tag: String?, receiver: InsetDispatch.() -> Unit) =
            receiver.invoke(InsetDispatch(tag, leftInset, topInset, rightInset, bottomInset, this))

    private fun contentInsetReducer(systemBottomInset: Int) =
            systemBottomInset - bottomInset - bottomNavHeightGetter()

    private fun coordinatorInsetReducer(systemBottomInset: Int) =
            if (systemBottomInset > bottomInset) systemBottomInset
            else bottomInset + if (uiState.showsBottomNav) bottomNavHeightGetter() else 0

    private fun withSpring(bottomPadding: Int, view: View, modifier: SpringAnimation.() -> Unit = {}) {
        val spring = view.getTag(R.id.bottom_padding) as? SpringAnimation
                ?: view.bottomPaddingSpring().apply(modifier).apply { view.setTag(R.id.bottom_padding, this) }
        if (view.paddingBottom != bottomPadding) spring.animateToFinalPosition(bottomPadding.toFloat())
    }

    companion object {
        const val ANIMATION_DURATION = 300

        var topInset: Int = 0
        var bottomInset: Int = 0
    }

    private data class InsetDispatch(
            val tag: String? = null,
            val leftInset: Int = 0,
            val topInset: Int = 0,
            val rightInset: Int = 0,
            val bottomInset: Int = 0,
            val insetFlags: InsetFlags? = null
    )
}

private fun View.bottomPaddingSpring(): SpringAnimation = springAnimationOf(
        {
            updatePadding(bottom = it.toInt())
            invalidate()
        },
        { paddingBottom.toFloat() },
        0F
).apply {
    spring.stiffness = SpringForce.STIFFNESS_MEDIUM
    spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
}

private val View.innermostFocusedChild: View?
    get() {
        if (this !is ViewGroup) return null
        val focused = focusedChild
        return focused?.innermostFocusedChild ?: focused
    }