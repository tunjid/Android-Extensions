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

    private TextView text;
    private Tile tile;
    private final ValueAnimator animator;

    public TileViewHolder(View itemView, TileAdapter.AdapterListener scanAdapterListener) {
        super(itemView, scanAdapterListener);

        text = itemView.findViewById(R.id.tile_text);
        animator = ValueAnimator.ofObject(new ArgbEvaluator(),Color.RED);
        animator.setDuration(1000);

        itemView.setOnClickListener(view -> adapterListener.onTileClicked(tile));
    }

    public void bind(Tile tile) {
        this.tile = tile;
        text.setText(tile.getId());
        animator.setIntValues(text.getCurrentTextColor(), tile.getColor());
        animator.addUpdateListener(this::updateTextColor);
        animator.start();
    }

    public void unBind() {
        animator.cancel();
        animator.removeAllUpdateListeners();
    }

    private void updateTextColor(ValueAnimator animation) {
        text.setTextColor((int) animation.getAnimatedValue());
    }
}
