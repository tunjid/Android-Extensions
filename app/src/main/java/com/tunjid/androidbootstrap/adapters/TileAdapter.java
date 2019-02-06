package com.tunjid.androidbootstrap.adapters;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.model.Tile;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Adapter for BLE devices found while sacnning
 */
public class TileAdapter extends InteractiveAdapter<TileAdapter.ViewHolder, TileAdapter.AdapterListener> {

    private final List<Tile> tiles;

    public TileAdapter(List<Tile> tiles, AdapterListener scanAdapterListener) {
        super(scanAdapterListener);
        setHasStableIds(true);
        this.tiles = tiles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new ViewHolder(getItemView(R.layout.viewholder_tile, viewGroup), adapterListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        viewHolder.bind(tiles.get(position));
    }

    @Override
    public long getItemId(int position) {
        return tiles.get(position).getNumber();
    }

    @Override
    public int getItemCount() {
        return tiles.size();
    }

    // ViewHolder for actual content
    static class ViewHolder extends InteractiveViewHolder<AdapterListener>
            implements
            View.OnClickListener {

        TextView text;
        Tile tile;

        ViewHolder(View itemView, AdapterListener scanAdapterListener) {
            super(itemView, scanAdapterListener);

            text = itemView.findViewById(R.id.tile_text);
            itemView.setOnClickListener(this);
        }

        void bind(Tile tile) {
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

    public interface AdapterListener extends InteractiveAdapter.AdapterListener {
        void onTileClicked(Tile tile);
    }
}
