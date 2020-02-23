package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.multiscroll.DynamicSizer
import com.tunjid.androidx.recyclerview.multiscroll.ExperimentalRecyclerViewMultiScrolling
import com.tunjid.androidx.recyclerview.multiscroll.RecyclerViewMultiScroller
import com.tunjid.androidx.recyclerview.multiscroll.StaticSizer
import com.tunjid.androidx.recyclerview.verticalLayoutManager
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.InsetFlags.Companion.NO_BOTTOM
import com.tunjid.androidx.viewholders.SpreadsheetRowViewHolder
import com.tunjid.androidx.viewmodels.SpreadsheetViewModel
import com.tunjid.androidx.viewmodels.routeName

class SpreadSheetParentFragment : AppBaseFragment(R.layout.fragment_spreadsheet_parent) {

    override val insetFlags: InsetFlags = NO_BOTTOM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int): Fragment =
                    if (position == 0) SpreadsheetFragment.newInstance(false)
                    else SpreadsheetFragment.newInstance(true)
        }

        TabLayoutMediator(view.findViewById(R.id.tabs), viewPager) { tab, position ->
            tab.text = if (position == 0) "Static" else "Dynamic"
        }.attach()
    }

    companion object {
        fun newInstance(): SpreadSheetParentFragment = SpreadSheetParentFragment().apply { arguments = Bundle() }
    }
}

@UseExperimental(ExperimentalRecyclerViewMultiScrolling::class)
class SpreadsheetFragment : AppBaseFragment(R.layout.fragment_route) {

    private var isDynamic by args<Boolean>()

    private val viewModel by viewModels<SpreadsheetViewModel>()

    private val scroller by lazy {
        RecyclerViewMultiScroller(sizeUpdater = when {
            isDynamic -> DynamicSizer()
            else -> StaticSizer(this@SpreadsheetFragment::staticSizeAt)
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName,
                toolbarShows = true,
                toolBarMenu = 0,
                fabShows = false,
                showsBottomNav = true,
                lightStatusBar = !requireContext().isDarkTheme,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color)
        )

        view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            val viewPool = RecyclerView.RecycledViewPool()

            layoutManager = verticalLayoutManager()
            adapter = adapterOf(
                    itemsSource = viewModel::rows,
                    viewHolderCreator = { parent, _ -> SpreadsheetRowViewHolder(parent, scroller, viewPool) },
                    viewHolderBinder = { viewHolder, tile, _ -> viewHolder.bind(tile) },
                    itemIdFunction = { it.index.toLong() }
            )
        }
    }

    private fun staticSizeAt(position: Int) = requireContext().resources.getDimensionPixelSize(when (position) {
        0 -> R.dimen.single_and_half_margin
        else -> R.dimen.sexdecuple_margin
    })

    override fun onDestroyView() {
        super.onDestroyView()
        scroller.clear()
    }

    companion object {
        fun newInstance(isDynamic: Boolean): SpreadsheetFragment = SpreadsheetFragment().apply { this.isDynamic = isDynamic }
    }
}
