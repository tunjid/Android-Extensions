package com.tunjid.androidbootstrap.viewholders;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.view.View;
import android.widget.TextView;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.TileAdapter;
import com.tunjid.androidbootstrap.model.Tile;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

public class TileViewHolder extends InteractiveViewHolder<TileAdapter.AdapterListener>
        implements
        View.OnClickListener {

   private TextView text;
   private Tile tile;

    public TileViewHolder(View itemView, TileAdapter.AdapterListener scanAdapterListener) {
        super(itemView, scanAdapterListener);

        text = itemView.findViewById(R.id.tile_text);
        itemView.setOnClickListener(this);
    }

    public void bind(Tile tile) {
        this.tile = tile;
        text.setText(tile.getId());

        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), text.getCurrentTextColor(), tile.getColor());
        animator.setDuration(1000);
        animator.addUpdateListener(animation -> text.setTextColor((int) animation.getAnimatedValue()));
        animator.start();
    }

    @Override
    public void onClick(View v) {
        switch ((v.getId())) {
            case R.id.row_parent:
                adapterListener.onTileClicked(tile);
                break;
        }
    }
}
