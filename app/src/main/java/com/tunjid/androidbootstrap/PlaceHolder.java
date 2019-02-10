package com.tunjid.androidbootstrap;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tunjid.androidbootstrap.recyclerview.ListPlaceholder;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

public class PlaceHolder implements ListPlaceholder<PlaceHolder.State> {

    private final TextView text;
    private final ImageView icon;

    public PlaceHolder(ViewGroup viewGroup) {
        this.text = viewGroup.findViewById(R.id.placeholder_text);
        this.icon = viewGroup.findViewById(R.id.placeholder_icon);
    }

    @Override
    public void toggle(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;

        text.setVisibility(visibility);
        icon.setVisibility(visibility);
    }

    @Override
    public void bind(PlaceHolder.State state) {
        text.setText(state.text);
        icon.setImageResource(state.icon);
    }

    public static class State {
        @StringRes final int text;
        @DrawableRes final int icon;

        public State(int text, int icon) {
            this.text = text;
            this.icon = icon;
        }
    }
}
