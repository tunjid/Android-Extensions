package com.tunjid.androidbootstrap.fragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidbootstrap.GlobalUiController
import com.tunjid.androidbootstrap.PlaceHolder
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.SlideInItemAnimator
import com.tunjid.androidbootstrap.UiState
import com.tunjid.androidbootstrap.activityGlobalUiController
import com.tunjid.androidbootstrap.adapters.TileAdapter
import com.tunjid.androidbootstrap.adapters.withPaddedAdapter
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.recyclerview.ListManager
import com.tunjid.androidbootstrap.recyclerview.ListManagerBuilder
import com.tunjid.androidbootstrap.view.util.InsetFlags
import com.tunjid.androidbootstrap.viewholders.TileViewHolder
import com.tunjid.androidbootstrap.viewmodels.ShiftingTileViewModel

class ShiftingTileFragment : AppBaseFragment(R.layout.fragment_route), GlobalUiController {

    override var uiState: UiState by activityGlobalUiController()

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
                toolbarTitle = this::class.java.simpleName,
                toolbarShows = true,
                toolBarMenu = 0,
                fabShows = true,
                showsBottomNav = false,
                fabIcon = fabIconRes,
                fabText = fabText,
                navBarColor = ContextCompat.getColor(requireContext(), R.color.white_75),
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
                .withPaddedAdapter(TileAdapter(viewModel.tiles) { showSnackbar { bar -> bar.setText(it.id) } }, 4)
                .build()
    }

    companion object {
        fun newInstance(): ShiftingTileFragment = ShiftingTileFragment().apply { arguments = Bundle() }
    }
}
