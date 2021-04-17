package com.tunjid.androidx.tablists.tiles

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.delegates.fragmentArgs
import com.tunjid.androidx.databinding.FragmentRouteBinding
import com.tunjid.androidx.databinding.ViewholderTileBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.recyclerview.acceptDiff
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.addScrollListener
import com.tunjid.androidx.recyclerview.gridLayoutManager
import com.tunjid.androidx.recyclerview.setEndlessScrollListener
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.tablists.tiles.EndlessTileViewModel.Companion.NUM_TILES
import com.tunjid.androidx.uidrivers.InsetFlags
import com.tunjid.androidx.uidrivers.SpringItemAnimator
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.uiState
import com.tunjid.androidx.uidrivers.updatePartial
import com.tunjid.androidx.tabnav.routing.routeName
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import kotlin.math.abs

class EndlessTilesFragment : Fragment(R.layout.fragment_route) {

    private var isTopLevel by fragmentArgs<Boolean>()
    private val viewModel by viewModels<EndlessTileViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isTopLevel) uiState = UiState(
            toolbarTitle = this::class.java.routeName,
            toolbarShows = true,
            toolbarMenuRes = 0,
            fabShows = true,
            fabIcon = R.drawable.ic_info_outline_24dp,
            fabText = getString(R.string.tile_info),
            showsBottomNav = false,
            insetFlags = InsetFlags.NO_BOTTOM,
            lightStatusBar = !requireContext().isDarkTheme,
            navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color),
            fabClickListener = { ::uiState.updatePartial { copy(snackbarText = "There are ${viewModel.tiles.size} tiles") } }
        )

        FragmentRouteBinding.bind(view).recyclerView.apply {
            itemAnimator = SpringItemAnimator()
            layoutManager = gridLayoutManager(3)
            adapter = adapterOf(
                itemsSource = viewModel::tiles,
                viewHolderCreator = { parent, _ ->
                    parent.tileViewHolder().apply {
                        itemView.setOnClickListener { uiState = uiState.copy(snackbarText = tile.toString()) }
                    }
                },
                viewHolderBinder = { viewHolder, tile, _ -> viewHolder.bind(tile) },
                itemIdFunction = { it.hashCode().toLong() },
                onViewHolderRecycled = BindingViewHolder<ViewholderTileBinding>::unbind,
                onViewHolderDetached = BindingViewHolder<ViewholderTileBinding>::unbind
            )

            addScrollListener { _, dy -> if (abs(dy) > 3) uiState = uiState.copy(fabShows = dy < 0) }
            setEndlessScrollListener(NUM_TILES) { viewModel.fetchMore() }
            OverScrollDecoratorHelper.setUpOverScroll(this, OverScrollDecoratorHelper.ORIENTATION_VERTICAL)

            viewModel.moreTiles.observe(viewLifecycleOwner, this::acceptDiff)
        }

    }

    companion object {
        fun newInstance(isTopLevel: Boolean): EndlessTilesFragment = EndlessTilesFragment().apply { this.isTopLevel = isTopLevel }
    }
}
