package com.tunjid.androidx.tablists.tiles

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.databinding.FragmentRouteBinding
import com.tunjid.androidx.databinding.ViewholderTileBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.recyclerview.acceptDiff
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.gridLayoutManager
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.uidrivers.SpringItemAnimator
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.uiState
import com.tunjid.androidx.uidrivers.updatePartial
import com.tunjid.androidx.uidrivers.InsetFlags.Companion.NO_BOTTOM
import com.tunjid.androidx.viewholders.bind
import com.tunjid.androidx.viewholders.tile
import com.tunjid.androidx.viewholders.tileViewHolder
import com.tunjid.androidx.viewholders.unbind
import com.tunjid.androidx.viewmodels.routeName

class ShiftingTilesFragment : Fragment(R.layout.fragment_route) {

    private val viewModel by viewModels<ShiftingTileViewModel>()

    private val fabIconRes: Int
        get() = if (viewModel.changes()) R.drawable.ic_grid_24dp else R.drawable.ic_blur_24dp

    private val fabText: CharSequence
        get() = getString(if (viewModel.changes()) R.string.static_tiles else R.string.dynamic_tiles)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = UiState(
                toolbarTitle = this::class.java.routeName,
                toolbarShows = true,
                toolbarMenuRes = 0,
                fabShows = true,
                showsBottomNav = false,
                fabIcon = fabIconRes,
                fabText = fabText,
                insetFlags = NO_BOTTOM,
                lightStatusBar = !requireContext().isDarkTheme,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color),
                fabClickListener = {
                    viewModel.toggleChanges()
                    ::uiState.updatePartial { copy(fabIcon = fabIconRes, fabText = fabText) }
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
            itemAnimator = SpringItemAnimator()
        }

        viewModel.watchTiles().observe(viewLifecycleOwner, tileAdapter::acceptDiff)
    }

    companion object {
        fun newInstance(): ShiftingTilesFragment = ShiftingTilesFragment().apply { arguments = Bundle() }
    }
}
