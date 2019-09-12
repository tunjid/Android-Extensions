package com.tunjid.androidbootstrap.fragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.tunjid.androidbootstrap.*
import com.tunjid.androidbootstrap.adapters.RouteAdapter
import com.tunjid.androidbootstrap.adapters.withPaddedAdapter
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.core.components.FragmentStackNavigator
import com.tunjid.androidbootstrap.core.components.activityNavigationController
import com.tunjid.androidbootstrap.model.Route
import com.tunjid.androidbootstrap.recyclerview.ListManagerBuilder
import com.tunjid.androidbootstrap.viewholders.RouteItemViewHolder
import com.tunjid.androidbootstrap.viewmodels.RouteViewModel

class RouteFragment : AppBaseFragment(R.layout.fragment_route), GlobalUiController, RouteAdapter.RouteAdapterListener {

    override var uiState: UiState by activityGlobalUiController()

    private val viewModel: RouteViewModel by viewModels()

    private val navigator: FragmentStackNavigator by activityNavigationController()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarTitle = getString(R.string.app_name),
                toolBarMenu = 0,
                toolbarShows = true,
                fabShows = false,
                showsBottomNav = true,
                navBarColor = ContextCompat.getColor(requireContext(), R.color.white_75)
        )

        ListManagerBuilder<RouteItemViewHolder, PlaceHolder.State>()
                .withRecyclerView(view.findViewById(R.id.recycler_view))
                .withLinearLayoutManager()
                .withPaddedAdapter(RouteAdapter(viewModel[arguments?.getInt(ARG_MAPPING) ?: 0], this))
                .build()
    }

    override fun onItemClicked(route: Route) {
        navigator.show<AppBaseFragment>(when (route.destination) {
            DoggoListFragment::class.java.simpleName -> DoggoListFragment.newInstance()
            BleScanFragment::class.java.simpleName -> BleScanFragment.newInstance()
            NsdScanFragment::class.java.simpleName -> NsdScanFragment.newInstance()
            HidingViewFragment::class.java.simpleName -> HidingViewFragment.newInstance()
            SpanbuilderFragment::class.java.simpleName -> SpanbuilderFragment.newInstance()
            ShiftingTileFragment::class.java.simpleName -> ShiftingTileFragment.newInstance()
            EndlessTileFragment::class.java.simpleName -> EndlessTileFragment.newInstance()
            DoggoRankFragment::class.java.simpleName -> DoggoRankFragment.newInstance()
            else -> newInstance(arguments?.getInt(ARG_MAPPING) ?: 0) // No-op, all RouteFragment instances have the same tag
        })
    }

    companion object {
        private const val ARG_MAPPING = "mapping"
        fun newInstance(id: Int): RouteFragment = RouteFragment().apply { arguments = bundleOf(ARG_MAPPING to id) }
    }
}
