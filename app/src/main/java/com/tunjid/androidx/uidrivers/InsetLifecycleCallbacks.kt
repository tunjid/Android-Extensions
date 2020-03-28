package com.tunjid.androidx.uidrivers

import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.EditText
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.springAnimationOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.tunjid.androidx.databinding.ActivityMainBinding
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.innermostFocusedChild
import com.tunjid.androidx.view.util.marginLayoutParams

class InsetLifecycleCallbacks(
        globalUiController: GlobalUiController,
        private val binding: ActivityMainBinding,
        private val stackNavigatorSource: () -> Navigator?
) : FragmentManager.FragmentLifecycleCallbacks(), GlobalUiController by globalUiController {

    private var leftInset: Int = 0
    private var rightInset: Int = 0
    private var insetsApplied: Boolean = false
    private var lastWindowInsets: WindowInsets? = null

    private val bottomNavHeight get() = binding.bottomNavigation.height

    private val topContentSpring =
            binding.contentContainer.paddingSpringAnimation(View::getPaddingTop) { updatePadding(top = it)}

    private val bottomContentSpring =
            binding.contentContainer.paddingSpringAnimation(View::getPaddingBottom) { updatePadding(bottom = it)}

    private val bottomCoordinatorSpring =
            binding.coordinatorLayout.paddingSpringAnimation(View::getPaddingBottom) { updatePadding(bottom = it)}

    init {
        binding.constraintLayout.setOnApplyWindowInsetsListener { _, insets -> onInsetsApplied(insets) }
        binding.bottomNavigation.doOnLayout { lastWindowInsets?.let(this::consumeFragmentInsets) }
        bottomContentSpring.apply {
            addEndListener { _, _, _, _ ->
                val input = binding.contentContainer.innermostFocusedChild as? EditText
                        ?: return@addEndListener
                input.text = input.text // Scroll to text that has focus
            }
        }
    }

    override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) =
            onFragmentViewCreated(v, f)

    private fun isNotInCurrentFragmentContainer(fragment: Fragment): Boolean =
            stackNavigatorSource()?.run { fragment.id != containerId } ?: true

    private fun onInsetsApplied(insets: WindowInsets): WindowInsets {
        if (this.insetsApplied) return insets

        statusBarSize = insets.systemWindowInsetTop
        leftInset = insets.systemWindowInsetLeft
        rightInset = insets.systemWindowInsetRight
        navBarSize = insets.systemWindowInsetBottom

        binding.toolbar.marginLayoutParams.topMargin = statusBarSize
        binding.bottomNavigation.marginLayoutParams.bottomMargin = navBarSize

        lastWindowInsets?.let(::consumeFragmentInsets)

        this.insetsApplied = true
        return insets
    }

    private fun onFragmentViewCreated(v: View, fragment: Fragment) {
        if (isNotInCurrentFragmentContainer(fragment)) return
        lastWindowInsets?.let(::consumeFragmentInsets)

        v.setOnApplyWindowInsetsListener { _, insets -> consumeFragmentInsets(insets) }
    }

    private fun consumeFragmentInsets(insets: WindowInsets): WindowInsets = insets.apply {
        lastWindowInsets = this

        val current = stackNavigatorSource()?.current ?: return@apply
        if (isNotInCurrentFragmentContainer(current)) return@apply

        val large = systemWindowInsetBottom > navBarSize + bottomNavHeight.given(uiState.showsBottomNav)
        val bottom = if (large) navBarSize else fragmentInsetReducer(uiState.insetFlags)

        val contentTop = (statusBarSize given uiState.insetFlags.hasTopInset).toFloat()
        val contentBottom = bottom + contentInsetReducer(systemWindowInsetBottom).toFloat()

        topContentSpring.animateToFinalPosition(contentTop)
        bottomContentSpring.animateToFinalPosition(contentBottom)
        bottomCoordinatorSpring.animateToFinalPosition(coordinatorInsetReducer(systemWindowInsetBottom).toFloat())

        return insets
    }

    private fun contentInsetReducer(systemBottomInset: Int) =
            systemBottomInset - navBarSize

    private fun coordinatorInsetReducer(systemBottomInset: Int) =
            if (systemBottomInset > navBarSize) systemBottomInset
            else navBarSize + (binding.bottomNavigation.height given uiState.showsBottomNav)

    private fun fragmentInsetReducer(insetFlags: InsetFlags): Int =
            bottomNavHeight.given(uiState.showsBottomNav) + navBarSize.given(insetFlags.hasBottomInset)

    companion object {
        const val ANIMATION_DURATION = 300

        var statusBarSize: Int = 0
        var navBarSize: Int = 0
    }
}

private infix fun Int.given(flag: Boolean) = if (flag) this else 0

private fun View.paddingSpringAnimation(getter: (View) -> Int, setter: View.(Int) -> Unit) =
    springAnimationOf(
            getter = { getter(this).toFloat() },
            setter = { setter(it.toInt()) },
            finalPosition = 0f
    ).apply {
        spring.stiffness = SpringForce.STIFFNESS_LOW
        spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
    }