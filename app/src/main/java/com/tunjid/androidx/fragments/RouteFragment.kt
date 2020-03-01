package com.tunjid.androidx.fragments

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.graphics.drawable.withTint
import com.tunjid.androidx.core.graphics.drawable.withTintMode
import com.tunjid.androidx.databinding.FragmentRouteBinding
import com.tunjid.androidx.databinding.ViewholderRouteBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.model.RouteItem
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.verticalLayoutManager
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderFrom
import com.tunjid.androidx.uidrivers.InsetLifecycleCallbacks
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

    private fun onRouteClicked(destination: RouteItem.Destination) {
        navigator.push(destination.fragment)
    }

    private fun goSomewhereRandom() = navigator.performConsecutively(lifecycleScope) {
        val (tabIndex, route) = viewModel.randomRoute()
        show(tabIndex)
        push(route.fragment)
    }

    private val RouteItem.Destination.fragment: AppBaseFragment
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
            SpreadSheetParentFragment::class.java.routeName -> SpreadSheetParentFragment.newInstance()
            else -> newInstance(tabIndex) // No-op, all RouteFragment instances have the same tag
        }

    companion object {
        fun newInstance(tabIndex: Int): RouteFragment = RouteFragment().apply { this.tabIndex = tabIndex }
    }
}

private var BindingViewHolder<ViewholderRouteBinding>.route by BindingViewHolder.Prop<RouteItem?>()

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

    binding.description.isVisible = visible
    (binding.destination.compoundDrawablesRelative[2] as AnimatedVectorDrawableCompat).start()
}