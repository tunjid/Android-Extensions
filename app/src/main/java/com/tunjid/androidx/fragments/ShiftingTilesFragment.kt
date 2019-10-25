package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.PlaceHolder
import com.tunjid.androidx.R
import com.tunjid.androidx.adapters.TileAdapter
import com.tunjid.androidx.adapters.withPaddedAdapter
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.content.resolveThemeColor
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.recyclerview.ListManager
import com.tunjid.androidx.recyclerview.ListManagerBuilder
import com.tunjid.androidx.uidrivers.SlideInItemAnimator
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.InsetFlags.Companion.NO_BOTTOM
import com.tunjid.androidx.viewholders.TileViewHolder
import com.tunjid.androidx.viewmodels.ShiftingTileViewModel
import com.tunjid.androidx.viewmodels.routeName

class ShiftingTilesFragment : AppBaseFragment(R.layout.fragment_route) {

    override val insetFlags: InsetFlags = NO_BOTTOM

    private val viewModel by viewModels<ShiftingTileViewModel>()

    private lateinit var listManager: ListManager<TileViewHolder, PlaceHolder.State>

    private val fabIconRes: Int
        get() = if (viewModel.changes()) R.drawable.ic_grid_24dp else R.drawable.ic_blur_24dp

    private val fabText: CharSequence
        get() = getString(if (viewModel.changes()) R.string.static_tiles else R.string.dynamic_tiles)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.watchTiles().observe(this) { listManager.onDiff(it) }
    }

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
                navBarColor = requireContext().resolveThemeColor(R.attr.nav_bar_color),
                fabClickListener = View.OnClickListener {
                    viewModel.toggleChanges()
                    uiState = uiState.copy(fabIcon = fabIconRes, fabText = fabText)
                }
        )

        listManager = ListManagerBuilder<TileViewHolder, PlaceHolder.State>()
                .withRecyclerView(view.findViewById<RecyclerView>(R.id.recycler_view)
                        .apply { itemAnimator = SlideInItemAnimator() }
                )
                .withGridLayoutManager(4)
                .withPaddedAdapter(TileAdapter(viewModel.tiles) { uiState = uiState.copy(snackbarText = it.diffId) }, 4)
                .build()
    }

    companion object {
        fun newInstance(): ShiftingTilesFragment = ShiftingTilesFragment().apply { arguments = Bundle() }
    }
}
