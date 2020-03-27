package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.databinding.FragmentRouteBinding
import com.tunjid.androidx.databinding.ViewholderTileBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.recyclerview.acceptDiff
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.addScrollListener
import com.tunjid.androidx.recyclerview.gridLayoutManager
import com.tunjid.androidx.recyclerview.setEndlessScrollListener
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.uidrivers.SlideInItemAnimator
import com.tunjid.androidx.uidrivers.update
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.InsetFlags.Companion.NO_BOTTOM
import com.tunjid.androidx.viewholders.bind
import com.tunjid.androidx.viewholders.tile
import com.tunjid.androidx.viewholders.tileViewHolder
import com.tunjid.androidx.viewholders.unbind
import com.tunjid.androidx.viewmodels.EndlessTileViewModel
import com.tunjid.androidx.viewmodels.EndlessTileViewModel.Companion.NUM_TILES
import com.tunjid.androidx.viewmodels.routeName
import kotlin.math.abs

class EndlessTilesFragment : AppBaseFragment(R.layout.fragment_route) {

    private val viewModel by viewModels<EndlessTileViewModel>()

    override val insetFlags: InsetFlags = NO_BOTTOM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName,
                toolbarShows = true,
                toolBarMenu = 0,
                fabShows = true,
                fabIcon = R.drawable.ic_info_outline_24dp,
                fabText = getString(R.string.tile_info),
                showsBottomNav = false,
                lightStatusBar = !requireContext().isDarkTheme,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color),
                fabClickListener = { ::uiState.update { copy(snackbarText = "There are ${viewModel.tiles.size} tiles") } }
        )

        FragmentRouteBinding.bind(view).recyclerView.apply {
            itemAnimator = SlideInItemAnimator()
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

            viewModel.moreTiles.observe(viewLifecycleOwner, this::acceptDiff)
        }

    }

    companion object {
        fun newInstance(): EndlessTilesFragment = EndlessTilesFragment().apply { arguments = Bundle() }
    }
}
