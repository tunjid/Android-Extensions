package com.tunjid.androidx.fragments

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.view.postDelayed
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.graphics.drawable.withTint
import com.tunjid.androidx.core.text.color
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.viewmodels.routeName

/**
 * Fragment demonstrating hiding views
 *
 *
 * Created by tj.dahunsi on 5/6/17.
 */

class SpeedDialFragment : AppBaseFragment(R.layout.fragment_hiding_view) {

    private val color
        get() = if (requireContext().isDarkTheme) Color.BLACK else Color.WHITE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val m : ExtendedFloatingActionButton? =null
        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName,
                toolbarShows = true,
                toolBarMenu = 0,
                fabShows = true,
                fabExtended = true,
                fabText = getString(R.string.speed_dial),
                fabIcon = R.drawable.ic_add_24dp,
                showsBottomNav = false,
                lightStatusBar = !requireContext().isDarkTheme,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color),
                fabClickListener = View.OnClickListener { fab ->
                    if (uiState.fabExtended) uiState = uiState.copy(fabExtended = false)
                    else speedDialer(
                            tint = requireContext().themeColorAt(R.attr.colorAccent),
                            items = *speedDialItems(),
                            dismissListener = this@SpeedDialFragment::onSpeedDialClicked
                    ).onClick(fab)
                }
        )

        view.postDelayed(2000) { uiState = uiState.copy(fabExtended = false) }
    }

    private fun speedDialItems(): Array<Pair<CharSequence?, Drawable>> = requireActivity().run {
        arrayOf(
                "expand".color(color) to drawableAt(R.drawable.ic_connect_24dp)
                        ?.withTint(color)!!,
                "bfs".color(color) to drawableAt(R.drawable.ic_compass_24dp)
                        ?.withTint(color)!!,
                "sfhvhvvjvgvghvvjvggd".color(color) to drawableAt(R.drawable.ic_circle_24dp)
                        ?.withTint(color)!!)
    }

    private fun onSpeedDialClicked(it: Int?) {
        when (it) {
            0 -> uiState = uiState.copy(fabExtended = true)
        }
    }

    companion object {
        fun newInstance(): SpeedDialFragment = SpeedDialFragment().apply { arguments = Bundle() }
    }
}
