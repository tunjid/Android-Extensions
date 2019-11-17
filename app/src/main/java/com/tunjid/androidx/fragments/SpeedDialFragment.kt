package com.tunjid.androidx.fragments

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.view.postDelayed
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.graphics.drawable.withTint
import com.tunjid.androidx.core.text.color
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.uidrivers.StateAwareSpeedDial
import com.tunjid.androidx.viewmodels.routeName

/**
 * Fragment demonstrating hiding views
 *
 *
 * Created by tj.dahunsi on 5/6/17.
 */

class SpeedDialFragment : AppBaseFragment(R.layout.fragment_speed_dial) {

    private val color
        get() = if (requireContext().isDarkTheme) Color.BLACK else Color.WHITE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = view.context

        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName,
                toolbarShows = true,
                toolBarMenu = 0,
                fabShows = true,
                fabExtended = true,
                fabText = getString(R.string.speed_dial),
                fabIcon = R.drawable.ic_unfold_more_24dp,
                showsBottomNav = false,
                lightStatusBar = !context.isDarkTheme,
                navBarColor = context.themeColorAt(R.attr.nav_bar_color),
                fabClickListener = StateAwareSpeedDial(
                        uiController = this,
                        tint = context.themeColorAt(R.attr.colorAccent),
                        items = speedDialItems(),
                        dismissListener = this@SpeedDialFragment::onSpeedDialClicked
                )
        )

        view.postDelayed(2000) { if (isResumed) uiState = uiState.copy(fabExtended = false) }
    }

    private fun speedDialItems(): List<Pair<CharSequence?, Drawable>> = requireActivity().run {
        listOf(
                getString(R.string.expand_fab).color(color) to drawableAt(R.drawable.ic_expand_24dp)
                        ?.withTint(color)!!,
                getString(R.string.option_1).color(color) to drawableAt(R.drawable.ic_numeric_1_outline_24dp)
                        ?.withTint(color)!!,
                getString(R.string.option_2).color(color) to drawableAt(R.drawable.ic_numeric_2_outline_24dp)
                        ?.withTint(color)!!)
    }

    private fun onSpeedDialClicked(it: Int?) = when (it) {
        0 -> uiState = uiState.copy(fabExtended = true)
        else -> Unit
    }

    companion object {
        fun newInstance(): SpeedDialFragment = SpeedDialFragment().apply { arguments = Bundle() }
    }
}
