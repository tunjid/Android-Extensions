package com.tunjid.androidbootstrap.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.tunjid.androidbootstrap.PlaceHolder
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.adapters.RouteAdapter
import com.tunjid.androidbootstrap.adapters.withPaddedAdapter
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.model.Route
import com.tunjid.androidbootstrap.recyclerview.ListManagerBuilder
import com.tunjid.androidbootstrap.view.util.InsetFlags
import com.tunjid.androidbootstrap.viewholders.RouteItemViewHolder
import com.tunjid.androidbootstrap.viewmodels.RouteViewModel

class RouteFragment : AppBaseFragment(), RouteAdapter.RouteAdapterListener {

    companion object {
        fun newInstance(): RouteFragment {
            val fragment = RouteFragment()
            fragment.arguments = Bundle()
            return fragment
        }
    }

    private lateinit var viewModel: RouteViewModel

    override val title: String
        get() = getString(R.string.app_name)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProviders.of(this).get(RouteViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_route, container, false)

        ListManagerBuilder<RouteItemViewHolder, PlaceHolder.State>()
                .withRecyclerView(rootView.findViewById(R.id.recycler_view))
                .withLinearLayoutManager()
                .withPaddedAdapter(RouteAdapter(viewModel.routes, this))
                .build()

        return rootView
    }

    override fun insetFlags(): InsetFlags = NO_BOTTOM

    override fun onItemClicked(route: Route) {
        showFragment(when (route.destination) {
            DoggoListFragment::class.java.simpleName -> DoggoListFragment.newInstance()
            BleScanFragment::class.java.simpleName -> BleScanFragment.newInstance()
            NsdScanFragment::class.java.simpleName -> NsdScanFragment.newInstance()
            HidingViewFragment::class.java.simpleName -> HidingViewFragment.newInstance()
            SpanbuilderFragment::class.java.simpleName -> SpanbuilderFragment.newInstance()
            ShiftingTileFragment::class.java.simpleName -> ShiftingTileFragment.newInstance()
            EndlessTileFragment::class.java.simpleName -> EndlessTileFragment.newInstance()
            DoggoRankFragment::class.java.simpleName -> DoggoRankFragment.newInstance()
            else -> newInstance() // No-op, all RouteFragment instances have the same tag
        })
    }

}
