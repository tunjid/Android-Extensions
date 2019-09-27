package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.tunjid.androidx.PlaceHolder
import com.tunjid.androidx.R
import com.tunjid.androidx.adapters.RouteAdapter
import com.tunjid.androidx.adapters.withPaddedAdapter
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.navigation.activityNavigationController
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.model.Route
import com.tunjid.androidx.recyclerview.ListManagerBuilder
import com.tunjid.androidx.uidrivers.GlobalUiController
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.activityGlobalUiController
import com.tunjid.androidx.viewholders.RouteItemViewHolder
import com.tunjid.androidx.viewmodels.RouteViewModel

class RouteFragment : AppBaseFragment(R.layout.fragment_route), GlobalUiController, RouteAdapter.RouteAdapterListener {

    override var uiState: UiState by activityGlobalUiController()

    private val viewModel: RouteViewModel by viewModels()

    private val navigator: com.tunjid.androidx.navigation.Navigator by activityNavigationController()

    private var tabId: Int by args()

    override val stableTag: String
        get() = "${super.stableTag}-$tabId"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (navigator.currentFragment === this) uiState = uiState.copy(
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
                .withPaddedAdapter(RouteAdapter(viewModel[tabId], this))
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
            MultipleStackFragment::class.java.simpleName -> MultipleStackFragment.newInstance()
            else -> newInstance(tabId) // No-op, all RouteFragment instances have the same tag
        })
    }

    companion object {
        fun newInstance(id: Int): RouteFragment = RouteFragment().apply { tabId = id }
    }
}
