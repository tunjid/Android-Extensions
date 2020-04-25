package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.core.view.children
import androidx.core.view.postDelayed
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.databinding.FragmentSpringAnimationBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.view.animator.ViewHider
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.MarginProperty
import com.tunjid.androidx.view.util.PaddingProperty
import com.tunjid.androidx.view.util.spring
import com.tunjid.androidx.viewmodels.routeName

/**
 * Fragment demonstrating hiding views
 *
 *
 * Created by tj.dahunsi on 5/6/17.
 */

class SpringAnimationFragment : AppBaseFragment(R.layout.fragment_spring_animation) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentSpringAnimationBinding.bind(view)
        val viewHiders = binding.viewHiders

        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName,
                toolbarShows = true,
                toolBarMenu = R.menu.menu_options,
                toolbarMenuClickListener = { viewHiders.springOptions() },
                fabShows = false,
                showsBottomNav = false,
                insetFlags = InsetFlags.ALL,
                lightStatusBar = !requireContext().isDarkTheme,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color)
        )

        val context = view.context
        val elevation = context.resources.getDimensionPixelSize(R.dimen.sixteenth_margin).toFloat()

        binding.cage.elevation = elevation
        binding.cage.background = MaterialShapeDrawable.createWithElevationOverlay(context, elevation).apply {
            shapeAppearanceModel = ShapeAppearanceModel().withCornerSize(context.resources.getDimensionPixelSize(R.dimen.half_margin).toFloat())
        }

        binding.shrinkMargin.toggleProperty(marginProperties, view)
        binding.shrinkPadding.toggleProperty(paddingProperties, binding.cage)
        binding.cage.children.forEach { it.setOnClickListener(viewHiders::onButtonClicked) }
    }

    private fun List<ViewHider<FloatingActionButton>>.springOptions() =
            dialogOf(getString(R.string.stiffness), stiffnessNames) { stiffnessIndex ->
                forEach { it.configure { stiffness = stiffnessValues[stiffnessIndex] } }
                view?.postDelayed(160){
                    dialogOf(getString(R.string.bounciness), dampingNames) { dampingIndex ->
                        forEach { it.configure { dampingRatio = dampingValues[dampingIndex] } }
                    }
                }
            }

    private fun dialogOf(title: String, names: Array<String>, onSelected: (Int) -> Unit) =
            MaterialAlertDialogBuilder(requireContext())
                    .setTitle(title)
                    .setItems(names) { _, index -> onSelected(index) }
                    .show()

    companion object {
        fun newInstance(): SpringAnimationFragment = SpringAnimationFragment().apply { arguments = Bundle() }
    }
}

private val FragmentSpringAnimationBinding.viewHiders: List<ViewHider<FloatingActionButton>>
    get() = listOf(
            ViewHider.of(leftButton).setDirection(ViewHider.LEFT).build(),
            ViewHider.of(topButton).setDirection(ViewHider.TOP).build(),
            ViewHider.of(rightButton).setDirection(ViewHider.RIGHT).build(),
            ViewHider.of(bottomButton).setDirection(ViewHider.BOTTOM).build()
    )

private fun List<ViewHider<FloatingActionButton>>.onButtonClicked(view: View) = when (view.id) {
    R.id.left_button -> this[0].hide()
    R.id.top_button -> this[1].hide()
    R.id.right_button -> this[2].hide()
    R.id.bottom_button -> this[3].hide()
    R.id.reset -> forEach(ViewHider<FloatingActionButton>::show)
    else -> Unit
}

private fun CheckBox.toggleProperty(properties: List<FloatPropertyCompat<View>>, cage: View) {
    val squeeze = context.resources.getDimensionPixelSize(R.dimen.double_margin).toFloat()
    setOnCheckedChangeListener { _, isChecked ->
        properties
                .map { cage.spring(it) }
                .forEach { it.animateToFinalPosition(if (isChecked) squeeze else 0f) }
    }
}

private val paddingProperties
    get() = listOf(
            PaddingProperty.LEFT,
            PaddingProperty.TOP,
            PaddingProperty.RIGHT,
            PaddingProperty.BOTTOM
    )

private val marginProperties
    get() = listOf(
            MarginProperty.LEFT,
            MarginProperty.TOP,
            MarginProperty.RIGHT,
            MarginProperty.BOTTOM
    )

private val Fragment.stiffnessNames
    get() = arrayOf(
            getString(R.string.very_low),
            getString(R.string.low),
            getString(R.string.medium),
            getString(R.string.high)
    )

private val Fragment.dampingNames
    get() = arrayOf(
            getString(R.string.none),
            getString(R.string.low),
            getString(R.string.medium),
            getString(R.string.high)
    )

private val stiffnessValues = listOf(
        SpringForce.STIFFNESS_VERY_LOW,
        SpringForce.STIFFNESS_LOW,
        SpringForce.STIFFNESS_MEDIUM,
        SpringForce.STIFFNESS_HIGH
)

private val dampingValues = listOf(
        SpringForce.DAMPING_RATIO_NO_BOUNCY,
        SpringForce.DAMPING_RATIO_LOW_BOUNCY,
        SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY,
        SpringForce.DAMPING_RATIO_HIGH_BOUNCY
)