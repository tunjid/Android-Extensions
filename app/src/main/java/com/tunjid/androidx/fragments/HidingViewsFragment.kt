package com.tunjid.androidx.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.view.children
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.tunjid.androidx.MutedColors
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.content.colorAt
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.graphics.drawable.withTint
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
                toolBarMenu = 0,
                fabShows = false,
                showsBottomNav = false,
                insetFlags = InsetFlags.ALL,
                lightStatusBar = !requireContext().isDarkTheme,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color),
                backgroundColor = MutedColors.colorAt(requireContext().isDarkTheme, 1)
        )

        val binding = FragmentHidingViewBinding.bind(view)
        val context = view.context

        binding.cage.background = MaterialShapeDrawable.createWithElevationOverlay(context).apply {
            shapeAppearanceModel = ShapeAppearanceModel().withCornerSize(context.resources.getDimensionPixelSize(R.dimen.half_margin).toFloat())
            strokeWidth = context.resources.getDimensionPixelSize(R.dimen.eigth_margin).toFloat()
            strokeColor = ColorStateList.valueOf(context.colorAt(R.color.white))
            withTint(MutedColors.colorAt(view.context.isDarkTheme, 0))
        }

        val leftHider = ViewHider.of(binding.leftButton).setDirection(ViewHider.LEFT).build()
        val topHider = ViewHider.of(binding.topButton).setDirection(ViewHider.TOP).build()
        val rightHider = ViewHider.of(binding.rightButton).setDirection(ViewHider.RIGHT).build()
        val bottomHider = ViewHider.of(binding.bottomButton).setDirection(ViewHider.BOTTOM).build()

        binding.shrinkPadding.setOnCheckedChangeListener { _, isChecked ->
            listOf(
                    binding.cage.spring(PaddingProperty.LEFT),
                    binding.cage.spring(PaddingProperty.TOP),
                    binding.cage.spring(PaddingProperty.RIGHT),
                    binding.cage.spring(PaddingProperty.BOTTOM)
            ).forEach { it.animateToFinalPosition(if (isChecked) 60f else 0f) }
        }

        binding.shrinkMargin.setOnCheckedChangeListener { _, isChecked ->
            listOf(
                    binding.root.spring(MarginProperty.LEFT),
                    binding.root.spring(MarginProperty.TOP),
                    binding.root.spring(MarginProperty.RIGHT),
                    binding.root.spring(MarginProperty.BOTTOM)
            ).forEach { it.animateToFinalPosition(if (isChecked) 60f else 0f) }
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
