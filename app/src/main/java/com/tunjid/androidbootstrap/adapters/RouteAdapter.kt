package com.tunjid.androidbootstrap.adapters

import android.view.ViewGroup

import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.model.Route
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter
import com.tunjid.androidbootstrap.viewholders.RouteItemViewHolder

/**
 * Adapter for displaying links to various parts of the app
 *
 *
 * Created by tj.dahunsi on 5/6/16.
 */
class RouteAdapter(private val routes: List<Route>, listener: RouteAdapterListener)
    : InteractiveAdapter<RouteItemViewHolder, RouteAdapter.RouteAdapterListener>(listener) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteItemViewHolder =
            RouteItemViewHolder(getItemView(R.layout.viewholder_route, parent), adapterListener)

    override fun onBindViewHolder(holder: RouteItemViewHolder, recyclerViewPosition: Int) =
            holder.bind(routes[recyclerViewPosition])

    override fun getItemCount(): Int = routes.size

    override fun getItemId(position: Int): Long = routes[position].hashCode().toLong()


    interface RouteAdapterListener : AdapterListener {
        fun onItemClicked(route: Route)
    }

}
