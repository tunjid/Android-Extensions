package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.model.Route
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.verticalLayoutManager
import com.tunjid.androidx.uidrivers.InsetLifecycleCallbacks
import com.tunjid.androidx.view.util.inflate
import com.tunjid.androidx.viewholders.RouteItemViewHolder
import com.tunjid.androidx.viewmodels.RouteViewModel
import com.tunjid.androidx.viewmodels.routeName

class RouteFragment : AppBaseFragment(R.layout.fragment_route) {

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
                fabShows = true,
                fabIcon = R.drawable.ic_dice_24dp,
                fabText = getString(R.string.route_feeling_lucky),
                fabClickListener = View.OnClickListener { goSomewhereRandom() },
                showsBottomNav = true,
                lightStatusBar = !requireContext().isDarkTheme,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color)
        )

        view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            layoutManager = verticalLayoutManager()
            adapter = adapterOf(
                    itemsSource = { viewModel[tabIndex] },
                    viewHolderCreator = { parent, _ -> RouteItemViewHolder(parent.inflate(R.layout.viewholder_route), ::onRouteClicked) },
                    viewHolderBinder = { routeViewHolder, route, _ -> routeViewHolder.bind(route) },
                    itemIdFunction = { it.hashCode().toLong() }
            )
            updatePadding(bottom = InsetLifecycleCallbacks.bottomInset)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_theme -> AppCompatDelegate.setDefaultNightMode(
                if (requireContext().isDarkTheme) MODE_NIGHT_NO
                else MODE_NIGHT_YES
        ).let { true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun onRouteClicked(route: Route) {
        navigator.push(route.fragment)
    }

    private fun goSomewhereRandom() = navigator.performConsecutively(lifecycleScope) {
        val (tabIndex, route) = viewModel.randomRoute()
        show(tabIndex)
        push(route.fragment)
    }

    private val Route.fragment: AppBaseFragment
        get() = when (destination) {
            DoggoListFragment::class.java.routeName -> DoggoListFragment.newInstance()
            BleScanFragment::class.java.routeName -> BleScanFragment.newInstance()
            NsdScanFragment::class.java.routeName -> NsdScanFragment.newInstance()
            HidingViewsFragment::class.java.routeName -> HidingViewsFragment.newInstance()
            CharacterSequenceExtensionsFragment::class.java.routeName -> CharacterSequenceExtensionsFragment.newInstance()
            ShiftingTilesFragment::class.java.routeName -> ShiftingTilesFragment.newInstance()
            EndlessTilesFragment::class.java.routeName -> EndlessTilesFragment.newInstance()
            DoggoRankFragment::class.java.routeName -> DoggoRankFragment.newInstance()
            IndependentStacksFragment::class.java.routeName -> IndependentStacksFragment.newInstance()
            MultipleStacksFragment::class.java.routeName -> MultipleStacksFragment.newInstance()
            HardServiceConnectionFragment::class.java.routeName -> HardServiceConnectionFragment.newInstance()
            FabTransformationsFragment::class.java.routeName -> FabTransformationsFragment.newInstance()
            SpreadsheetFragment::class.java.routeName -> SpreadsheetFragment.newInstance()
            else -> newInstance(tabIndex) // No-op, all RouteFragment instances have the same tag
        }

    companion object {
        fun newInstance(tabIndex: Int): RouteFragment = RouteFragment().apply { this.tabIndex = tabIndex }
    }
}
