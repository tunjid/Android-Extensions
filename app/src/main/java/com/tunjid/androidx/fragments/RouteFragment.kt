package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.tunjid.androidx.PlaceHolder
import com.tunjid.androidx.R
import com.tunjid.androidx.adapters.RouteAdapter
import com.tunjid.androidx.adapters.withPaddedAdapter
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.core.content.resolveThemeColor
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.model.Route
import com.tunjid.androidx.recyclerview.ListManagerBuilder
import com.tunjid.androidx.viewholders.RouteItemViewHolder
import com.tunjid.androidx.viewmodels.RouteViewModel
import com.tunjid.androidx.viewmodels.routeName

class RouteFragment : AppBaseFragment(R.layout.fragment_route),
        RouteAdapter.RouteAdapterListener {

    private val viewModel: RouteViewModel by viewModels()

    private var tabIndex: Int by args()

    override val stableTag: String
        get() = "${super.stableTag}-$tabIndex"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarTitle = getString(R.string.app_name),
                toolBarMenu = R.menu.menu_route,
                toolbarShows = true,
                fabShows = false,
                showsBottomNav = true,
                lightStatusBar = !requireContext().isDarkTheme,
                navBarColor = requireContext().resolveThemeColor(R.attr.nav_bar_color)
        )

        ListManagerBuilder<RouteItemViewHolder, PlaceHolder.State>()
                .withRecyclerView(view.findViewById(R.id.recycler_view))
                .withLinearLayoutManager()
                .withPaddedAdapter(RouteAdapter(viewModel[tabIndex], this))
                .build()
    }

    override fun onItemClicked(route: Route) {
        navigator.push(when (route.destination) {
            DoggoListFragment::class.java.routeName -> DoggoListFragment.newInstance()
            BleScanFragment::class.java.routeName -> BleScanFragment.newInstance()
            NsdScanFragment::class.java.routeName -> NsdScanFragment.newInstance()
            HidingViewsFragment::class.java.routeName -> HidingViewsFragment.newInstance()
            SpanbuilderFragment::class.java.routeName -> SpanbuilderFragment.newInstance()
            ShiftingTilesFragment::class.java.routeName -> ShiftingTilesFragment.newInstance()
            EndlessTilesFragment::class.java.routeName -> EndlessTilesFragment.newInstance()
            DoggoRankFragment::class.java.routeName -> DoggoRankFragment.newInstance()
            IndependentStacksFragment::class.java.routeName -> IndependentStacksFragment.newInstance()
            MultipleStacksFragment::class.java.routeName -> MultipleStacksFragment.newInstance()
            HardServiceConnectionFragment::class.java.routeName -> HardServiceConnectionFragment.newInstance()
            else -> newInstance(tabIndex) // No-op, all RouteFragment instances have the same tag
        })
    }

    companion object {
        fun newInstance(tabIndex: Int): RouteFragment = RouteFragment().apply { this.tabIndex = tabIndex }
    }
}
