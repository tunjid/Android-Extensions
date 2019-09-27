package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.tunjid.androidx.PlaceHolder
import com.tunjid.androidx.R
import com.tunjid.androidx.adapters.TileAdapter
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.recyclerview.ListManager
import com.tunjid.androidx.recyclerview.ListManagerBuilder
import com.tunjid.androidx.uidrivers.GlobalUiController
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.activityGlobalUiController
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.InsetFlags.Companion.NO_BOTTOM
import com.tunjid.androidx.viewholders.TileViewHolder
import com.tunjid.androidx.viewmodels.EndlessTileViewModel
import com.tunjid.androidx.viewmodels.EndlessTileViewModel.Companion.NUM_TILES
import kotlin.math.abs

class EndlessTileFragment : AppBaseFragment(R.layout.fragment_route), GlobalUiController {

    override var uiState: UiState by activityGlobalUiController()

    private val viewModel by viewModels<EndlessTileViewModel>()
    private lateinit var listManager: ListManager<TileViewHolder, PlaceHolder.State>

    override val insetFlags: InsetFlags = NO_BOTTOM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.moreTiles.observe(this) { listManager.onDiff(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarTitle = this::class.java.simpleName,
                toolbarShows = true,
                toolBarMenu = 0,
                fabShows = true,
                fabIcon = R.drawable.ic_info_outline_24dp,
                fabText = getString(R.string.tile_info),
                showsBottomNav = false,
                navBarColor = ContextCompat.getColor(requireContext(), R.color.white_75),
                fabClickListener = View.OnClickListener {
                    uiState = uiState.copy(snackbarText = "There are ${viewModel.tiles.size} tiles")
                }
        )

        listManager = ListManagerBuilder<TileViewHolder, PlaceHolder.State>()
                .withRecyclerView(view.findViewById(R.id.recycler_view))
                .withGridLayoutManager(3)
                .withAdapter(TileAdapter(viewModel.tiles) { uiState = uiState.copy(snackbarText = it.toString()) })
                .withEndlessScrollCallback(NUM_TILES) { viewModel.fetchMore() }
                .addScrollListener { _, dy -> if (abs(dy) > 3) uiState = uiState.copy(fabShows = dy < 0) }
                .build()
    }

    companion object {
        fun newInstance(): EndlessTileFragment = EndlessTileFragment().apply { arguments = Bundle() }
    }
}
