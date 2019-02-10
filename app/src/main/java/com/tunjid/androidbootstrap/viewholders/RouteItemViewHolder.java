package com.tunjid.androidbootstrap.viewholders;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.RouteAdapter;
import com.tunjid.androidbootstrap.model.Route;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.TextViewCompat;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

public class RouteItemViewHolder extends InteractiveViewHolder<RouteAdapter.RouteAdapterListener>
        implements View.OnClickListener {

    private Route route;

    private TextView routeDestination;
    private TextView routeDescription;

    public RouteItemViewHolder(View itemView, RouteAdapter.RouteAdapterListener listener) {
        super(itemView, listener);

        routeDestination = itemView.findViewById(R.id.destination);
        routeDescription = itemView.findViewById(R.id.description);

        itemView.setOnClickListener(this);
        routeDescription.setOnClickListener(this);

        setIcons(true, routeDestination);
    }

    public void bind(Route route) {
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
