package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.databinding.FragmentRouteBinding
import com.tunjid.androidx.databinding.ViewholderTileBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.recyclerview.acceptDiff
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.gridLayoutManager
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.uidrivers.SlideInItemAnimator
import com.tunjid.androidx.uidrivers.activityGlobalUiController
import com.tunjid.androidx.uidrivers.update
import com.tunjid.androidx.view.util.InsetFlags.Companion.NO_BOTTOM
import com.tunjid.androidx.viewholders.bind
import com.tunjid.androidx.viewholders.tile
import com.tunjid.androidx.viewholders.tileViewHolder
import com.tunjid.androidx.viewholders.unbind
import com.tunjid.androidx.viewmodels.ShiftingTileViewModel
import com.tunjid.androidx.viewmodels.routeName

class ShiftingTilesFragment : Fragment(R.layout.fragment_route) {

    private var uiState by activityGlobalUiController()
    private val viewModel by viewModels<ShiftingTileViewModel>()

    private val fabIconRes: Int
        get() = if (viewModel.changes()) R.drawable.ic_grid_24dp else R.drawable.ic_blur_24dp

    private val fabText: CharSequence
        get() = getString(if (viewModel.changes()) R.string.static_tiles else R.string.dynamic_tiles)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName,
                toolbarShows = true,
                toolBarMenu = 0,
                fabShows = true,
                showsBottomNav = false,
                fabIcon = fabIconRes,
                fabText = fabText,
                insetFlags = NO_BOTTOM,
                lightStatusBar = !requireContext().isDarkTheme,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color),
                fabClickListener = {
                    viewModel.toggleChanges()
                    ::uiState.update { copy(fabIcon = fabIconRes, fabText = fabText) }
                }
        )

        val tileAdapter = adapterOf(
                itemsSource = viewModel::tiles,
                viewHolderCreator = { parent, _ ->
                    parent.tileViewHolder().apply {
                        itemView.setOnClickListener { uiState = uiState.copy(snackbarText = tile.diffId) }
                    }
                },
                viewHolderBinder = { viewHolder, tile, _ -> viewHolder.bind(tile) },
                itemIdFunction = { it.hashCode().toLong() },
                onViewHolderRecycled = BindingViewHolder<ViewholderTileBinding>::unbind,
                onViewHolderDetached = BindingViewHolder<ViewholderTileBinding>::unbind
        )

        FragmentRouteBinding.bind(view).recyclerView.apply {
            adapter = tileAdapter
            layoutManager = gridLayoutManager(4)
            itemAnimator = SlideInItemAnimator()
        }

        viewModel.watchTiles().observe(viewLifecycleOwner, tileAdapter::acceptDiff)
    }

    companion object {
        fun newInstance(): ShiftingTilesFragment = ShiftingTilesFragment().apply { arguments = Bundle() }
    }
}
