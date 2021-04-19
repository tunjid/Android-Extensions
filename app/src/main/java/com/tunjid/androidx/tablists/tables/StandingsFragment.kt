package com.tunjid.androidx.tablists.tables

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.tunjid.androidx.R
import com.tunjid.androidx.core.components.doOnEveryEvent
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.delegates.fragmentArgs
import com.tunjid.androidx.databinding.FragmentStandingsBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.recyclerview.multiscroll.DynamicCellSizer
import com.tunjid.androidx.tabnav.routing.routeName
import com.tunjid.androidx.uidrivers.callback
import com.tunjid.androidx.uidrivers.uiState
import com.tunjid.androidx.uidrivers.updatePartial

class StandingsFragment : Fragment(R.layout.fragment_standings) {

    private var isTopLevel by fragmentArgs<Boolean>()
    private val viewModel by viewModels<StandingsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isTopLevel) uiState = uiState.copy(
            toolbarTitle = this::class.java.routeName,
            toolbarShows = true,
            toolbarOverlaps = false,
            toolbarMenuRes = 0,
            fabShows = false,
            showsBottomNav = false,
            lightStatusBar = !requireContext().isDarkTheme,
            navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color)
        )
        else viewLifecycleOwner.lifecycle.doOnEveryEvent(Lifecycle.Event.ON_RESUME) {
            ::uiState.updatePartial { copy(fabShows = false) }
        }

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
        is Cell.Text -> Unit
        is Cell.Image -> Unit
        is Cell.Header -> viewModel.accept(StandingInput.Sort(cell.copy(ascending = !cell.ascending)))
    }

    companion object {
        fun newInstance(isTopLevel: Boolean): StandingsFragment = StandingsFragment().apply { this.isTopLevel = isTopLevel }
    }
}
//endregion
