package com.tunjid.androidx.adapters

import android.view.ViewGroup

import com.tunjid.androidx.R
import com.tunjid.androidx.model.Route
import com.tunjid.androidx.recyclerview.InteractiveAdapter
import com.tunjid.androidx.view.util.inflate
import com.tunjid.androidx.viewholders.RouteItemViewHolder

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
            RouteItemViewHolder(parent.inflate(R.layout.viewholder_route), delegate)

    override fun onBindViewHolder(holder: RouteItemViewHolder, recyclerViewPosition: Int) =
            holder.bind(routes[recyclerViewPosition])

    override fun getItemCount(): Int = routes.size

    override fun getItemId(position: Int): Long = routes[position].hashCode().toLong()


    interface RouteAdapterListener {
        fun onItemClicked(route: Route)
    }

}
