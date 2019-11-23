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
import com.tunjid.androidx.navigation.Navigator
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
        private val bottomNavHeightGetter: () -> Int
) : FragmentManager.FragmentLifecycleCallbacks() {

    private var leftInset: Int = 0
    private var rightInset: Int = 0
    private var insetsApplied: Boolean = false
    private var lastInsetDispatch: InsetDispatch? = InsetDispatch()

    private val coordinatorSpring = bottomPaddingSpring(coordinatorLayout)
    private val contentSpring = bottomPaddingSpring(contentContainer).addEndListener { _, _, _, _ ->
        val input = contentContainer.recursiveFocusedChild as? EditText ?: return@addEndListener
        input.text = input.text // Scroll to text that has focus
    }

    init {
        ViewCompat.setOnApplyWindowInsetsListener(parentContainer) { _, insets -> consumeSystemInsets(insets) }
    }

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
        val old = contentContainer.paddingBottom
        val new = max(insets.systemWindowInsetBottom - bottomInset - bottomNavHeightGetter(), 0)

        if (old == new) return insets

        contentSpring.animateToFinalPosition(new.toFloat())
        coordinatorSpring.animateToFinalPosition((if (new != 0) new else 1).toFloat())

        return insets
    }

    @SuppressLint("InlinedApi")
    fun adjustInsetForFragment(fragment: Fragment?) {
        if (fragment !is InsetProvider || isNotInCurrentFragmentContainer(fragment)) return

        fragment.insetFlags.dispatch(fragment.tag) {
            if (insetFlags == null || lastInsetDispatch == this) return

            toolbar.marginLayoutParams.topMargin = if (insetFlags.hasTopInset) 0 else topInset
            coordinatorLayout.marginLayoutParams.bottomMargin = if (insetFlags.hasBottomInset) 0 else bottomInset

            topInsetView.visibility = if (insetFlags.hasTopInset) View.VISIBLE else View.GONE
            bottomInsetView.visibility = if (insetFlags.hasBottomInset) View.VISIBLE else View.GONE

            parentContainer.setPadding(
                    if (insetFlags.hasLeftInset) this.leftInset else 0,
                    0,
                    if (insetFlags.hasRightInset) this.rightInset else 0,
                    0)

            val topPadding = if (insetFlags.hasTopInset) topInset else 0
            val bottomPadding = bottomNavHeightGetter() + if (insetFlags.hasBottomInset) bottomInset else 0

            fragment.view?.updatePadding(top = topPadding, bottom = bottomPadding)

            lastInsetDispatch = this
        }
    }

    private inline fun InsetFlags.dispatch(tag: String?, receiver: InsetDispatch.() -> Unit) =
            receiver.invoke(InsetDispatch(tag, leftInset, topInset, rightInset, bottomInset, this))

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

private fun bottomPaddingSpring(view: View): SpringAnimation = springAnimationOf(
        {
            view.updatePadding(bottom = it.toInt())
            view.invalidate()
        },
        { view.paddingBottom.toFloat() },
        0F
).apply {
    spring.stiffness = SpringForce.STIFFNESS_MEDIUM
    spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
}

private val View.recursiveFocusedChild : View?
    get() {
        if (this !is ViewGroup) return null
        val focused = focusedChild
        return focused.recursiveFocusedChild ?: focused
    }