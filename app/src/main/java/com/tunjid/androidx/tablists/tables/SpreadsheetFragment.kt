package com.tunjid.androidx.tablists.tables

import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.delegates.fragmentArgs
import com.tunjid.androidx.databinding.FragmentSpreadsheetChildBinding
import com.tunjid.androidx.databinding.FragmentSpreadsheetParentBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.material.viewpager.configureWith
import com.tunjid.androidx.recyclerview.multiscroll.CellSizer
import com.tunjid.androidx.recyclerview.multiscroll.DynamicCellSizer
import com.tunjid.androidx.recyclerview.multiscroll.StaticCellSizer
import com.tunjid.androidx.tabnav.routing.routeName
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.uiState
import com.tunjid.viewpager2.FragmentListAdapter
import com.tunjid.viewpager2.FragmentTab

class SpreadSheetParentFragment : Fragment(R.layout.fragment_spreadsheet_parent) {

    private var isTopLevel by fragmentArgs<Boolean>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isTopLevel) uiState = uiState.copy(
            toolbarTitle = this::class.java.routeName,
            toolbarShows = true,
            fabShows = false,
            showsBottomNav = true,
            lightStatusBar = !requireContext().isDarkTheme,
            navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color)
        )

        val binding = FragmentSpreadsheetParentBinding.bind(view)
        val viewPager = binding.viewPager
        val pagerAdapter = FragmentListAdapter<SpreadsheetTab>(fragment = this)

        pagerAdapter.submitList(listOf(
            SpreadsheetTab(isDynamic = false),
            SpreadsheetTab(isDynamic = true),
        ))

        viewPager.adapter = pagerAdapter

        binding.tabs.configureWith(binding.viewPager) { tab, position ->
            tab.text = pagerAdapter.getPageTitle(position)
        }
    }

    companion object {
        fun newInstance(isTopLevel: Boolean): SpreadSheetParentFragment = SpreadSheetParentFragment().apply { this.isTopLevel = isTopLevel }
    }
}

private data class SpreadsheetTab(val isDynamic: Boolean) : FragmentTab {
    override fun title(res: Resources): CharSequence = res.getString(when {
        isDynamic -> R.string.dynamic_cells
        else -> R.string.static_cells
    })

    override fun createFragment(): Fragment = SpreadsheetFragment.newInstance(isDynamic)
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