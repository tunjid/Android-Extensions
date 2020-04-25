package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.children
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.databinding.FragmentHidingViewBinding
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

class HidingViewsFragment : AppBaseFragment(R.layout.fragment_hiding_view) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName,
                toolbarShows = true,
                toolBarMenu = R.menu.menu_options,
                fabShows = false,
                showsBottomNav = false,
                insetFlags = InsetFlags.ALL,
                lightStatusBar = !requireContext().isDarkTheme,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color)
        )

        val binding = FragmentHidingViewBinding.bind(view)
        val context = view.context
        val elevation = context.resources.getDimensionPixelSize(R.dimen.sixteenth_margin).toFloat()

        binding.cage.elevation = elevation
        binding.cage.background = MaterialShapeDrawable.createWithElevationOverlay(context, elevation).apply {
            shapeAppearanceModel = ShapeAppearanceModel().withCornerSize(context.resources.getDimensionPixelSize(R.dimen.half_margin).toFloat())
        }

        val leftHider = ViewHider.of(binding.leftButton).setDirection(ViewHider.LEFT).build()
        val topHider = ViewHider.of(binding.topButton).setDirection(ViewHider.TOP).build()
        val rightHider = ViewHider.of(binding.rightButton).setDirection(ViewHider.RIGHT).build()
        val bottomHider = ViewHider.of(binding.bottomButton).setDirection(ViewHider.BOTTOM).build()

        val squeeze = context.resources.getDimensionPixelSize(R.dimen.double_margin).toFloat()

        binding.shrinkPadding.setOnCheckedChangeListener { _, isChecked ->
            listOf(
                    binding.cage.spring(PaddingProperty.LEFT),
                    binding.cage.spring(PaddingProperty.TOP),
                    binding.cage.spring(PaddingProperty.RIGHT),
                    binding.cage.spring(PaddingProperty.BOTTOM)
            ).forEach { it.animateToFinalPosition(if (isChecked) squeeze else 0f) }
        }

        binding.shrinkMargin.setOnCheckedChangeListener { _, isChecked ->
            listOf(
                    binding.root.spring(MarginProperty.LEFT),
                    binding.root.spring(MarginProperty.TOP),
                    binding.root.spring(MarginProperty.RIGHT),
                    binding.root.spring(MarginProperty.BOTTOM)
            ).forEach { it.animateToFinalPosition(if (isChecked) squeeze else 0f) }
        }

        fun onClick(v: View) = when (v.id) {
            R.id.left_button -> leftHider.hide()
            R.id.top_button -> topHider.hide()
            R.id.right_button -> rightHider.hide()
            R.id.bottom_button -> bottomHider.hide()
            R.id.reset -> {
                leftHider.show()
                topHider.show()
                rightHider.show()
                bottomHider.show()
            }
            else -> Unit
        }

        binding.cage.children.forEach { it.setOnClickListener(::onClick) }
    }

    companion object {
        fun newInstance(): HidingViewsFragment = HidingViewsFragment().apply { arguments = Bundle() }
    }
}
