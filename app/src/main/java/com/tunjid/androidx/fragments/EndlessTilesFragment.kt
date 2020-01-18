package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.model.Tile
import com.tunjid.androidx.recyclerview.acceptDiff
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.addEndlessScrollListener
import com.tunjid.androidx.recyclerview.addScrollListener
import com.tunjid.androidx.recyclerview.gridLayoutManager
import com.tunjid.androidx.uidrivers.SlideInItemAnimator
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.InsetFlags.Companion.NO_BOTTOM
import com.tunjid.androidx.view.util.inflate
import com.tunjid.androidx.viewholders.TileViewHolder
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
                fabClickListener = View.OnClickListener {
                    uiState = uiState.copy(snackbarText = "There are ${viewModel.tiles.size} tiles")
                }
        )

        val onTileClicked = { tile: Tile -> uiState = uiState.copy(snackbarText = tile.toString()) }

        view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            itemAnimator = SlideInItemAnimator()
            layoutManager = gridLayoutManager(3)
            adapter = adapterOf(
                    itemsSource = viewModel::tiles,
                    viewHolderCreator = { parent, _ -> TileViewHolder(parent.inflate(R.layout.viewholder_tile), onTileClicked) },
                    viewHolderBinder = { viewHolder, tile, _ -> viewHolder.bind(tile) },
                    itemIdFunction = { it.hashCode().toLong() },
                    onViewHolderRecycled = TileViewHolder::unbind,
                    onViewHolderDetached = TileViewHolder::unbind
            )

            addScrollListener { _, dy -> if (abs(dy) > 3) uiState = uiState.copy(fabShows = dy < 0) }
            addEndlessScrollListener(NUM_TILES) { viewModel.fetchMore() }

            viewModel.moreTiles.observe(viewLifecycleOwner, this::acceptDiff)
        }

    }

    companion object {
        fun newInstance(): EndlessTilesFragment = EndlessTilesFragment().apply { arguments = Bundle() }
    }
}
