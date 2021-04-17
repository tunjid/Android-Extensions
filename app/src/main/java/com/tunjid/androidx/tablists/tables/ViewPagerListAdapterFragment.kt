package com.tunjid.androidx.tablists.tables

import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.delegates.viewLifecycle
import com.tunjid.androidx.databinding.FragmentSpreadsheetParentBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.material.viewpager.configureWith
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.tablists.doggo.DoggoListFragment
import com.tunjid.androidx.tablists.doggo.DoggoRankFragment
import com.tunjid.androidx.tablists.doggo.RankArgs
import com.tunjid.androidx.tablists.tiles.EndlessTilesFragment
import com.tunjid.androidx.tablists.tiles.ShiftingTilesFragment
import com.tunjid.androidx.tabnav.routing.routeName
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.uiState
import com.tunjid.viewpager2.FragmentListAdapter
import com.tunjid.viewpager2.FragmentTab

class ViewPagerListAdapterFragment : Fragment(R.layout.fragment_spreadsheet_parent),
    Navigator.TransactionModifier {

    private val binding by viewLifecycle(FragmentSpreadsheetParentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = UiState(
            toolbarTitle = this::class.java.routeName,
            toolbarShows = true,
            fabShows = true,
            fabText = "TODO()",
            showsBottomNav = false,
            lightStatusBar = !requireContext().isDarkTheme,
            navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color)
        )

        val viewPager = binding.viewPager
        val pagerAdapter = FragmentListAdapter<RouteTab>(fragment = this)

        pagerAdapter.submitList(listOf(
            RouteTab.DoggoList,
            RouteTab.DoggoRank,
            RouteTab.ShiftingTiles,
            RouteTab.EndlessTiles,
        ))

        viewPager.adapter = pagerAdapter

        binding.tabs.configureWith(binding.viewPager) { tab, position ->
            tab.text = pagerAdapter.getPageTitle(position)
        }
    }

    override fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment) {
        val fragment = childFragmentManager.findFragmentByTag("f${binding.viewPager.adapter?.getItemId(binding.viewPager.currentItem)}")
        (fragment as? Navigator.TransactionModifier)?.augmentTransaction(transaction, incomingFragment)
    }

    companion object {
        fun newInstance(): ViewPagerListAdapterFragment = ViewPagerListAdapterFragment().apply { arguments = Bundle() }
    }
}

private sealed class RouteTab : FragmentTab {
    object DoggoList : RouteTab()
    object DoggoRank : RouteTab()
    object ShiftingTiles : RouteTab()
    object EndlessTiles : RouteTab()

    override fun title(res: Resources): CharSequence = when (this) {
        DoggoList -> DoggoListFragment::class.java.routeName
        DoggoRank -> DoggoRankFragment::class.java.routeName
        ShiftingTiles -> ShiftingTilesFragment::class.java.routeName
        EndlessTiles -> EndlessTilesFragment::class.java.routeName
    }

    override fun createFragment(): Fragment = when (this) {
        DoggoList -> DoggoListFragment.newInstance(isTopLevel = false)
        DoggoRank -> DoggoRankFragment.newInstance(RankArgs(isTopLevel = false, isRanking = true))
        ShiftingTiles -> ShiftingTilesFragment.newInstance(isTopLevel = false)
        EndlessTiles -> EndlessTilesFragment.newInstance(isTopLevel = false)
    }
}
