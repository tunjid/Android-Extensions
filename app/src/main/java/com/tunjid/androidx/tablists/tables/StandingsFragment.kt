package com.tunjid.androidx.tablists.tables

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.databinding.FragmentStandingsBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.recyclerview.multiscroll.DynamicCellSizer
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.uiState
import com.tunjid.androidx.tabnav.routing.routeName

class StandingsFragment : Fragment(R.layout.fragment_standings) {

    private val viewModel by viewModels<StandingsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = UiState(
            toolbarTitle = this::class.java.routeName,
            toolbarShows = true,
            toolbarOverlaps = false,
            toolbarMenuRes = 0,
            fabShows = false,
            showsBottomNav = false,
            lightStatusBar = !requireContext().isDarkTheme,
            navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color)
        )

        val binding = FragmentStandingsBinding.bind(view)

        binding.tabs.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && checkedId != View.NO_ID && host != null && !childFragmentManager.isStateSaved) when (checkedId) {
                R.id.all -> GameFilter.All
                R.id.home -> GameFilter.Home
                R.id.away -> GameFilter.Away
                else -> null
            }
                ?.let(StandingInput::Filter)
                ?.let(viewModel::accept)
        }

        binding.tableContainer.bind(
            table = viewModel.state,
            columnSizer = ::DynamicCellSizer,
            owner = viewLifecycleOwner,
            onCellClicked = ::onCellClicked
        )
    }

    private fun onCellClicked(cell: Cell) = when (cell) {
        is Cell.Stat -> Unit
        is Cell.Text -> Unit
        is Cell.Image -> Unit
        is Cell.Header -> viewModel.accept(StandingInput.Sort(cell.copy(ascending = !cell.ascending)))
    }

    companion object {
        fun newInstance(): StandingsFragment = StandingsFragment()
    }
}
//endregion
