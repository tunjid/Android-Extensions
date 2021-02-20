package com.tunjid.androidx.tabnav.routing

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.colorAt
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.delegates.fragmentArgs
import com.tunjid.androidx.core.graphics.drawable.withTint
import com.tunjid.androidx.core.graphics.drawable.withTintMode
import com.tunjid.androidx.databinding.FragmentRouteBinding
import com.tunjid.androidx.databinding.ViewholderRouteBinding
import com.tunjid.androidx.tabmisc.CharacterSequenceExtensionsFragment
import com.tunjid.androidx.tabmisc.FabTransformationsFragment
import com.tunjid.androidx.tabmisc.HardServiceConnectionFragment
import com.tunjid.androidx.tabmisc.SpringAnimationFragment
import com.tunjid.androidx.tabmisc.UiStatePlaygroundFragment
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.navigation.MultiStackNavigator
import com.tunjid.androidx.navigation.activityNavigatorController
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.verticalLayoutManager
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderDelegate
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderFrom
import com.tunjid.androidx.tabcomms.ble.BleScanFragment
import com.tunjid.androidx.tabcomms.nsd.NsdScanFragment
import com.tunjid.androidx.tablists.doggo.DoggoListFragment
import com.tunjid.androidx.tablists.doggo.DoggoRankFragment
import com.tunjid.androidx.tablists.tables.SpreadSheetParentFragment
import com.tunjid.androidx.tablists.tables.StandingsFragment
import com.tunjid.androidx.tablists.tiles.EndlessTilesFragment
import com.tunjid.androidx.tablists.tiles.ShiftingTilesFragment
import com.tunjid.androidx.tabnav.RouteItem
import com.tunjid.androidx.tabnav.navigator.IndependentStacksFragment
import com.tunjid.androidx.tabnav.navigator.MultipleStacksFragment
import com.tunjid.androidx.uidrivers.InsetFlags
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.uiState
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper

class RouteFragment : Fragment(R.layout.fragment_route) {

    private val viewModel by viewModels<RouteViewModel>()
    private val navigator by activityNavigatorController<MultiStackNavigator>()

    private var tabIndex: Int by fragmentArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = UiState(
            toolbarTitle = getString(R.string.app_name),
            toolbarMenuRes = R.menu.menu_route,
            toolbarOverlaps = false,
            toolbarShows = true,
            fabShows = true,
            fabIcon = R.drawable.ic_dice_24dp,
            fabText = getString(R.string.route_feeling_lucky),
            fabClickListener = { goSomewhereRandom() },
            insetFlags = InsetFlags.ALL,
            showsBottomNav = true,
            backgroundColor = Color.TRANSPARENT,
            lightStatusBar = !requireContext().isDarkTheme,
            navBarColor = requireContext().colorAt(R.color.colorSurface),
            toolbarMenuClickListener = ::onMenuItemSelected
        )

        FragmentRouteBinding.bind(view).recyclerView.apply {
            layoutManager = verticalLayoutManager()
            adapter = adapterOf(
                itemsSource = { viewModel[tabIndex] },
                viewHolderCreator = { parent, _ ->
                    parent.viewHolderFrom(ViewholderRouteBinding::inflate).apply {
                        binding.description.setOnClickListener { (route as? RouteItem.Destination)?.let(::onRouteClicked) }
                        binding.root.setOnClickListener { changeVisibility() }
                        setIcons(true)
                    }
                },
                viewHolderBinder = { routeViewHolder, route, _ -> routeViewHolder.bind(route) },
                itemIdFunction = { it.hashCode().toLong() }
            )
            OverScrollDecoratorHelper.setUpOverScroll(this, OverScrollDecoratorHelper.ORIENTATION_VERTICAL)
        }
    }

    private fun onMenuItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_theme -> AppCompatDelegate.setDefaultNightMode(
            if (requireContext().isDarkTheme) MODE_NIGHT_NO
            else MODE_NIGHT_YES
        )
        R.id.menu_stress_test -> MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.stress_test_prompt)
            .setPositiveButton(R.string.yes) { _, _ -> stressTest() }
            .setNegativeButton(R.string.no) { dialog, _ -> dialog.cancel() }
            .show()
            .let { }
        R.id.menu_reset -> navigator.clearAll()

        else -> requireActivity().onOptionsItemSelected(item).let { }
    }

    private fun onRouteClicked(destination: RouteItem.Destination) {
        navigator.push(destination.fragment)
    }

    private fun goSomewhereRandom() = navigator.performConsecutively(lifecycleScope) {
        val (tabIndex, route) = viewModel.randomRoute()
        show(tabIndex)
        push(route.fragment)
    }

    private fun stressTest() = navigator.performConsecutively(requireActivity().lifecycleScope) {
        val nav = navigator

        (0..10)
            .map { viewModel.randomRoute() }
            .forEach { pair ->
                val (tabIndex, route) = pair

                show(tabIndex)
                push(route.fragment)
            }

        clearAll()

        (0..10)
            .map { viewModel.randomRoute() }
            .forEach { pair ->
                val (tabIndex, route) = pair
                show(tabIndex)
                clear()
                push(route.fragment)
            }

        while (nav.previous != null) pop()
    }

    private val RouteItem.Destination.fragment: Fragment
        get() = when (destination) {
            DoggoListFragment::class.java.routeName -> DoggoListFragment.newInstance()
            BleScanFragment::class.java.routeName -> BleScanFragment.newInstance()
            NsdScanFragment::class.java.routeName -> NsdScanFragment.newInstance()
            SpringAnimationFragment::class.java.routeName -> SpringAnimationFragment.newInstance()
            CharacterSequenceExtensionsFragment::class.java.routeName -> CharacterSequenceExtensionsFragment.newInstance()
            ShiftingTilesFragment::class.java.routeName -> ShiftingTilesFragment.newInstance()
            EndlessTilesFragment::class.java.routeName -> EndlessTilesFragment.newInstance()
            DoggoRankFragment::class.java.routeName -> DoggoRankFragment.newInstance()
            IndependentStacksFragment::class.java.routeName -> IndependentStacksFragment.newInstance()
            MultipleStacksFragment::class.java.routeName -> MultipleStacksFragment.newInstance()
            HardServiceConnectionFragment::class.java.routeName -> HardServiceConnectionFragment.newInstance()
            FabTransformationsFragment::class.java.routeName -> FabTransformationsFragment.newInstance()
            StandingsFragment::class.java.routeName -> StandingsFragment.newInstance()
            SpreadSheetParentFragment::class.java.routeName -> SpreadSheetParentFragment.newInstance()
            UiStatePlaygroundFragment::class.java.routeName -> UiStatePlaygroundFragment.newInstance()
            else -> newInstance(tabIndex) // No-op, all RouteFragment instances have the same tag
        }

    companion object {
        fun newInstance(tabIndex: Int): RouteFragment = RouteFragment().apply { this.tabIndex = tabIndex }
    }
}

private var BindingViewHolder<ViewholderRouteBinding>.route by viewHolderDelegate<RouteItem?>()

fun BindingViewHolder<ViewholderRouteBinding>.bind(route: RouteItem) {
    this.route = route

    itemView.visibility = if (route is RouteItem.Destination) View.VISIBLE else View.INVISIBLE
    if (route !is RouteItem.Destination) return

    binding.destination.text = route.destination
    binding.description.text = route.description
}

@SuppressLint("ResourceAsColor")
private fun BindingViewHolder<ViewholderRouteBinding>.setIcons(isDown: Boolean) =
    binding.destination.setCompoundDrawablesRelativeWithIntrinsicBounds(
        null,
        null,
        AnimatedVectorDrawableCompat.create(
            itemView.context,
            if (isDown) R.drawable.anim_vect_down_to_right_arrow
            else R.drawable.anim_vect_right_to_down_arrow
        )
            ?.withTint(itemView.context.themeColorAt(R.attr.prominent_text_color))
            ?.withTintMode(PorterDuff.Mode.SRC_IN),
        null
    )

private fun BindingViewHolder<ViewholderRouteBinding>.changeVisibility() {
    TransitionManager.beginDelayedTransition(itemView.parent as ViewGroup, AutoTransition())

    val visible = binding.description.isVisible
    setIcons(visible)

    binding.description.isVisible = !visible
    (binding.destination.compoundDrawablesRelative[2] as AnimatedVectorDrawableCompat).start()
}