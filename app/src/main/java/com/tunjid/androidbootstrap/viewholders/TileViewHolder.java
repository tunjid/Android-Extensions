package com.tunjid.androidbootstrap.viewholders;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.TileAdapter;
import com.tunjid.androidbootstrap.model.Tile;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

public class TileViewHolder extends InteractiveViewHolder<TileAdapter.AdapterListener> {

    private static final int COLOR_CHANGE_DURATION = 1000;
    private static final int START_DELAY = 300;

    private TextView text;
    private Tile tile;

    private final ValueAnimator animator;
    private final ValueAnimator.AnimatorUpdateListener listener;

    public TileViewHolder(View itemView, TileAdapter.AdapterListener scanAdapterListener) {
        super(itemView, scanAdapterListener);

        text = itemView.findViewById(R.id.tile_text);
        animator = ValueAnimator.ofObject(new ArgbEvaluator(), Color.RED);
        animator.setDuration(COLOR_CHANGE_DURATION);

        listener = this::updateTextColor;
        itemView.setOnClickListener(view -> adapterListener.onTileClicked(tile));
    }

    public void bind(Tile tile) {
        this.tile = tile;
        text.setText(tile.getId());

        animator.setIntValues(text.getCurrentTextColor(), tile.getColor());
        animator.addUpdateListener(listener);
        animator.setStartDelay(START_DELAY); // Cheeky bit of code to keep scrolling smooth on fling
        animator.start();
    }

    public void unBind() {
        animator.cancel();
        animator.removeUpdateListener(listener);
    }

    private void updateTextColor(ValueAnimator animation) {
        text.setTextColor((int) animation.getAnimatedValue());
    }
}
