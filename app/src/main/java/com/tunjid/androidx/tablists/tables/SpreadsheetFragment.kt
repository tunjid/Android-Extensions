package com.tunjid.androidx.tablists.tables

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.delegates.fragmentArgs
import com.tunjid.androidx.databinding.FragmentSpreadsheetChildBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.recyclerview.multiscroll.CellSizer
import com.tunjid.androidx.recyclerview.multiscroll.DynamicCellSizer
import com.tunjid.androidx.recyclerview.multiscroll.StaticCellSizer
import com.tunjid.androidx.tabnav.routing.routeName
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.uiState

class SpreadSheetParentFragment : Fragment(R.layout.fragment_spreadsheet_parent) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = UiState(
            toolbarTitle = this::class.java.routeName,
            toolbarShows = true,
            fabShows = false,
            showsBottomNav = true,
            lightStatusBar = !requireContext().isDarkTheme,
            navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color)
        )

        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)
        val pagerAdapter = object : FragmentStateAdapter(this.childFragmentManager, viewLifecycleOwner.lifecycle) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int): Fragment =
                SpreadsheetFragment.newInstance(isDynamic = position != 0)
        }

        viewPager.isUserInputEnabled = false
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(view.findViewById(R.id.tabs), viewPager) { tab, position ->
            tab.text = context?.getString(if (position != 0) R.string.dynamic_cells else R.string.static_cells)
        }.attach()
    }

    companion object {
        fun newInstance(): SpreadSheetParentFragment = SpreadSheetParentFragment().apply { arguments = Bundle() }
    }
}

class SpreadsheetFragment : Fragment(R.layout.fragment_spreadsheet_child) {

    private var isDynamic by fragmentArgs<Boolean>()

    private val viewModel by viewModels<SpreadsheetViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentSpreadsheetChildBinding.bind(view)
        binding.tableContainer.bind(
            table = viewModel.state,
            columnSizer = ::cellSizer,
            owner = viewLifecycleOwner,
            onCellClicked = { if (it is Cell.Header) viewModel.accept(it.copy(ascending = !it.ascending)) }
        )
    }

    private fun cellSizer(): CellSizer = when {
        isDynamic -> DynamicCellSizer()
        else -> StaticCellSizer(sizeLookup = ::staticSizeAt)
    }

    private fun staticSizeAt(position: Int) = requireContext().resources.getDimensionPixelSize(when (position) {
        0 -> R.dimen.triple_margin
        in 1..3 -> R.dimen.septuple_margin
        else -> R.dimen.sexdecuple_margin
    })

    companion object {
        fun newInstance(isDynamic: Boolean): SpreadsheetFragment = SpreadsheetFragment().apply { this.isDynamic = isDynamic }
    }
}