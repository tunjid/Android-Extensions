package com.tunjid.androidbootstrap.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.tunjid.androidbootstrap.PlaceHolder
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.adapters.TileAdapter
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.recyclerview.ListManager
import com.tunjid.androidbootstrap.recyclerview.ListManagerBuilder
import com.tunjid.androidbootstrap.viewholders.TileViewHolder
import com.tunjid.androidbootstrap.viewmodels.EndlessTileViewModel
import com.tunjid.androidbootstrap.viewmodels.EndlessTileViewModel.Companion.NUM_TILES

class EndlessTileFragment : AppBaseFragment() {

    companion object {
        fun newInstance(): EndlessTileFragment {
            val fragment = EndlessTileFragment()
            fragment.arguments = Bundle()
            return fragment
        }
    }

    private lateinit var viewModel: EndlessTileViewModel
    private lateinit var listManager: ListManager<TileViewHolder, PlaceHolder.State>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProviders.of(this).get(EndlessTileViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_route, container, false)
        listManager = ListManagerBuilder<TileViewHolder, PlaceHolder.State>()
                .withRecyclerView(root.findViewById(R.id.recycler_view))
                .withGridLayoutManager(3)
                .withAdapter(TileAdapter(viewModel.tiles) { showSnackbar { snackBar -> snackBar.setText(it.toString()) } })
                .withEndlessScrollCallback(NUM_TILES) { disposables.add(viewModel.moreTiles.subscribe(listManager::onDiff, Throwable::printStackTrace)) }
                .build()

        return root
    }
}
