package com.tunjid.androidbootstrap.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.TextViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;
import com.tunjid.androidbootstrap.model.Route;

import java.util.List;

/**
 * Adapter for displaying links to various parts of the app
 * <p>
 * Created by tj.dahunsi on 5/6/16.
 */
public class RouteAdapter extends BaseRecyclerViewAdapter<RouteAdapter.RouteItemViewHolder, RouteAdapter.RouteAdapterListener> {

    private List<Route> routes;

    public RouteAdapter(List<Route> routes, RouteAdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.routes = routes;
    }

    @NonNull
    @Override
    public RouteItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).
                inflate(R.layout.viewholder_route, parent, false);

        return new RouteItemViewHolder(itemView, adapterListener);
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


    public interface RouteAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onItemClicked(Route route);
    }

    static class RouteItemViewHolder extends BaseViewHolder<RouteAdapterListener>
            implements View.OnClickListener {

        Route route;

        TextView routeDestination;
        TextView routeDescription;

        RouteItemViewHolder(View itemView, RouteAdapterListener listener) {
            super(itemView, listener);

            routeDestination = itemView.findViewById(R.id.destination);
            routeDescription = itemView.findViewById(R.id.description);

            itemView.setOnClickListener(this);
            routeDescription.setOnClickListener(this);

            setIcons(true, routeDestination);
        }

        void bind(Route route) {
            this.route = route;

            routeDestination.setText(route.getDestination());
            routeDescription.setText(route.getDescription());
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.description:
                    adapterListener.onItemClicked(route);
                    break;
                default:
                    changeVisibility(routeDestination, routeDescription);
                    break;
            }
        }

        @SuppressLint("ResourceAsColor")
        private void setIcons(boolean isDown, TextView... textViews) {
            int resVal = isDown ? R.drawable.anim_vect_down_to_right_arrow : R.drawable.anim_vect_right_to_down_arrow;

            for (TextView textView : textViews) {
                Drawable icon = AnimatedVectorDrawableCompat.create(itemView.getContext(), resVal);
                if (icon != null) {
                    icon = DrawableCompat.wrap(icon.mutate());
                    DrawableCompat.setTint(icon, R.color.dark_grey);
                    DrawableCompat.setTintMode(icon, PorterDuff.Mode.SRC_IN);
                    TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(textView, null, null, icon, null);
                }
            }
        }

        private void changeVisibility(TextView clicked, View... changing) {
            TransitionManager.beginDelayedTransition((ViewGroup) itemView.getParent(), new AutoTransition());

            boolean visible = changing[0].getVisibility() == View.VISIBLE;

            setIcons(visible, clicked);

            AnimatedVectorDrawableCompat animatedDrawable = (AnimatedVectorDrawableCompat)
                    TextViewCompat.getCompoundDrawablesRelative(clicked)[2];

            animatedDrawable.start();

            int visibility = visible ? View.GONE : View.VISIBLE;
            for (View view : changing) view.setVisibility(visibility);
        }
    }
}
