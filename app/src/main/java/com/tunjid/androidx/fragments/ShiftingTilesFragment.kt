package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.recyclerview.acceptDiff
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.gridLayoutManager
import com.tunjid.androidx.uidrivers.InsetLifecycleCallbacks
import com.tunjid.androidx.uidrivers.SlideInItemAnimator
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.InsetFlags.Companion.NO_BOTTOM
import com.tunjid.androidx.view.util.inflate
import com.tunjid.androidx.viewholders.TileViewHolder
import com.tunjid.androidx.viewmodels.ShiftingTileViewModel
import com.tunjid.androidx.viewmodels.routeName

class ShiftingTilesFragment : AppBaseFragment(R.layout.fragment_route) {

    override val insetFlags: InsetFlags = NO_BOTTOM

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
                lightStatusBar = !requireContext().isDarkTheme,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color),
                fabClickListener = View.OnClickListener {
                    viewModel.toggleChanges()
                    uiState = uiState.copy(fabIcon = fabIconRes, fabText = fabText)
                }
        )

        val tileAdapter = adapterOf(
                itemsSource = viewModel::tiles,
                viewHolderCreator = { parent, _ ->
                    TileViewHolder(parent.inflate(R.layout.viewholder_tile)) { tile ->
                        uiState = uiState.copy(snackbarText = tile.diffId)
                    }
                },
                viewHolderBinder = { viewHolder, tile, _ -> viewHolder.bind(tile) },
                itemIdFunction = { it.hashCode().toLong() },
                onViewHolderRecycled = TileViewHolder::unbind,
                onViewHolderDetached = TileViewHolder::unbind
        )

        view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            adapter = tileAdapter
            layoutManager = gridLayoutManager(4)
            itemAnimator = SlideInItemAnimator()

            updatePadding(bottom = InsetLifecycleCallbacks.bottomInset)
        }

        viewModel.watchTiles().observe(viewLifecycleOwner, tileAdapter::acceptDiff)
    }

    companion object {
        fun newInstance(): ShiftingTilesFragment = ShiftingTilesFragment().apply { arguments = Bundle() }
    }
}
