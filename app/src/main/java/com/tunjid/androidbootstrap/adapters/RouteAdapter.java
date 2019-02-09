package com.tunjid.androidbootstrap.adapters;

import android.view.ViewGroup;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.model.Route;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.viewholders.RouteItemViewHolder;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Adapter for displaying links to various parts of the app
 * <p>
 * Created by tj.dahunsi on 5/6/16.
 */
public class RouteAdapter extends InteractiveAdapter<RouteItemViewHolder, RouteAdapter.RouteAdapterListener> {

    private List<Route> routes;

    public RouteAdapter(List<Route> routes, RouteAdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.routes = routes;
    }

    @NonNull
    @Override
    public RouteItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RouteItemViewHolder(getItemView(R.layout.viewholder_route, parent), adapterListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteItemViewHolder holder, int recyclerViewPosition) {
        holder.bind(routes.get(recyclerViewPosition));
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    @Override
    public long getItemId(int position) {
        return routes.get(position).hashCode();
    }


    public interface RouteAdapterListener extends InteractiveAdapter.AdapterListener {
        void onItemClicked(Route route);
    }

}
