package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.core.view.children
import androidx.core.view.postDelayed
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.databinding.FragmentSpringAnimationBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.uidrivers.activityGlobalUiController
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

private typealias SpringModifier = SpringForce.() -> Unit

private typealias SpringModifierConsumer = (SpringForce.() -> Unit) -> Unit

class SpringAnimationFragment : Fragment(R.layout.fragment_spring_animation) {

    private var uiState by activityGlobalUiController()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentSpringAnimationBinding.bind(view)
        val viewHiders = binding.viewHiders

        @Suppress("MoveSuspiciousCallableReferenceIntoParentheses")
        val springModifiers: List<SpringModifierConsumer> =
                viewHiders.map { it::configure }
                        .plus(marginProperties.toModifiers(view))
                        .plus(paddingProperties.toModifiers(binding.cage))
                        .plus(viewHiders.map(ViewHider<FloatingActionButton>::view).map(scaleProperties::toModifiers).flatten())

        springModifiers.forEach {
            it.invoke {
                stiffness = stiffnessValues.first()
                dampingRatio = dampingValues.last()
            }
        }

        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName,
                toolBarMenu = R.menu.menu_spring_animations,
                fabShows = true,
                toolbarOverlaps = false,
                toolbarShows = true,
                toolbarMenuClickListener = {
                    springModifiers.springOptions()
                },
                fabIcon = R.drawable.ic_dance_24dp,
                fabText = getString(R.string.party_hard),
                fabClickListener = {
                    marginProperties.partyHard(view)
                    paddingProperties.partyHard(binding.cage)
                    viewHiders.map(ViewHider<FloatingActionButton>::view).forEach(scaleProperties::partyHard)
                },
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

        binding.shrinkMargin.run {
            view.spring(MarginProperty.LEFT).addEndListener { _, _, value, _ -> isChecked = value != 0f }
        }
        binding.shrinkPadding.run {
            binding.cage.spring(PaddingProperty.LEFT).addEndListener { _, _, value, _ -> isChecked = value != 0f }
        }
    }

    private fun List<SpringModifierConsumer>.springOptions() =
            dialogOf(getString(R.string.stiffness), stiffnessNames) { stiffnessIndex ->
                forEach { it.invoke { stiffness = stiffnessValues[stiffnessIndex] } }
                view?.postDelayed(160) {
                    dialogOf(getString(R.string.bounciness), dampingNames) { dampingIndex ->
                        forEach { it.invoke { dampingRatio = dampingValues[dampingIndex] } }
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

private fun List<FloatPropertyCompat<View>>.toModifiers(view: View) =
        map { view.spring(it).spring }
                .map { { modifier: SpringModifier -> modifier.invoke(it) } }

private fun List<FloatPropertyCompat<View>>.partyHard(view: View) = forEach {
    val resources = view.context.resources
    val current = it.getValue(view)
    val squeeze = resources.getDimensionPixelSize(R.dimen.double_margin).toFloat()

    view.spring(it).animateToFinalPosition(when (it) {
        SpringAnimation.SCALE_X -> if (current == 1f) 0.8f else 1f
        SpringAnimation.SCALE_Y -> if (current == 1f) 0.8f else 1f
        else -> if (current == 0f) squeeze else 0f
    })
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

private val scaleProperties
    get() = listOf(
            SpringAnimation.SCALE_X,
            SpringAnimation.SCALE_Y
    )

private val Fragment.stiffnessNames
    get() = arrayOf(
            getString(R.string.bruh),
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
            getString(R.string.high),
            getString(R.string.bruh)
    )

private val stiffnessValues = listOf(
        10f,
        SpringForce.STIFFNESS_VERY_LOW,
        SpringForce.STIFFNESS_LOW,
        SpringForce.STIFFNESS_MEDIUM,
        SpringForce.STIFFNESS_HIGH
)

private val dampingValues = listOf(
        SpringForce.DAMPING_RATIO_NO_BOUNCY,
        SpringForce.DAMPING_RATIO_LOW_BOUNCY,
        SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY,
        SpringForce.DAMPING_RATIO_HIGH_BOUNCY,
        0.08f
)