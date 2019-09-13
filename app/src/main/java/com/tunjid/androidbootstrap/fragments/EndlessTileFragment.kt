package com.tunjid.androidbootstrap.fragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.tunjid.androidbootstrap.uidrivers.GlobalUiController
import com.tunjid.androidbootstrap.PlaceHolder
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.uidrivers.UiState
import com.tunjid.androidbootstrap.uidrivers.activityGlobalUiController
import com.tunjid.androidbootstrap.adapters.TileAdapter
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.recyclerview.ListManager
import com.tunjid.androidbootstrap.recyclerview.ListManagerBuilder
import com.tunjid.androidbootstrap.view.util.InsetFlags
import com.tunjid.androidbootstrap.view.util.InsetFlags.Companion.NO_BOTTOM
import com.tunjid.androidbootstrap.viewholders.TileViewHolder
import com.tunjid.androidbootstrap.viewmodels.EndlessTileViewModel
import com.tunjid.androidbootstrap.viewmodels.EndlessTileViewModel.Companion.NUM_TILES

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
                fabShows = false,
                showsBottomNav = false,
                navBarColor = ContextCompat.getColor(requireContext(), R.color.white_75)
        )

        listManager = ListManagerBuilder<TileViewHolder, PlaceHolder.State>()
                .withRecyclerView(view.findViewById(R.id.recycler_view))
                .withGridLayoutManager(3)
                .withAdapter(TileAdapter(viewModel.tiles) { showSnackbar { snackBar -> snackBar.setText(it.toString()) } })
                .withEndlessScrollCallback(NUM_TILES) { viewModel.fetchMore() }
                .build()
    }

    companion object {
        fun newInstance(): EndlessTileFragment = EndlessTileFragment().apply { arguments = Bundle() }
    }
}
