package com.tunjid.androidbootstrap.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getDrawable
import androidx.lifecycle.ViewModelProviders
import com.tunjid.androidbootstrap.PlaceHolder
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.adapters.TileAdapter
import com.tunjid.androidbootstrap.adapters.withPaddedAdapter
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.material.animator.FabExtensionAnimator
import com.tunjid.androidbootstrap.recyclerview.ListManager
import com.tunjid.androidbootstrap.recyclerview.ListManagerBuilder
import com.tunjid.androidbootstrap.view.util.InsetFlags
import com.tunjid.androidbootstrap.viewholders.TileViewHolder
import com.tunjid.androidbootstrap.viewmodels.ShiftingTileViewModel

class ShiftingTileFragment : AppBaseFragment() {

    private lateinit var viewModel: ShiftingTileViewModel
    private lateinit var listManager: ListManager<TileViewHolder, PlaceHolder.State>

    override val fabState: FabExtensionAnimator.GlyphState
        get() = if (viewModel.changes())
            FabExtensionAnimator.newState(getText(R.string.static_tiles), getDrawable(requireContext(), R.drawable.ic_grid_24dp))
        else
            FabExtensionAnimator.newState(getText(R.string.dynamic_tiles), getDrawable(requireContext(), R.drawable.ic_blur_24dp))

    override val fabClickListener: View.OnClickListener
        get() = View.OnClickListener {
            viewModel.toggleChanges()
            togglePersistentUi()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProviders.of(this).get(ShiftingTileViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_route, container, false)
        listManager = ListManagerBuilder<TileViewHolder, PlaceHolder.State>()
                .withRecyclerView(root.findViewById(R.id.recycler_view))
                .withGridLayoutManager(4)
                .withPaddedAdapter(TileAdapter(viewModel.tiles) { showSnackbar { bar -> bar.setText(it.id) } }, 4)
                .build()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        disposables.add(viewModel.watchTiles().subscribe(listManager::onDiff, Throwable::printStackTrace))
    }

    override fun showsFab(): Boolean = true

    override fun insetFlags(): InsetFlags = NO_BOTTOM

    companion object {

        fun newInstance(): ShiftingTileFragment {
            val fragment = ShiftingTileFragment()
            fragment.arguments = Bundle()
            return fragment
        }
    }
}
