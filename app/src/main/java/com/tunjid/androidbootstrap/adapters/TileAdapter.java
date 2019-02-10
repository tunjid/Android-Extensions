package com.tunjid.androidbootstrap.adapters;

import android.view.ViewGroup;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.model.Tile;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.viewholders.TileViewHolder;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Adapter for BLE devices found while sacnning
 */
public class TileAdapter extends InteractiveAdapter<TileViewHolder, TileAdapter.AdapterListener> {

    private final List<Tile> tiles;

    public TileAdapter(List<Tile> tiles, AdapterListener scanAdapterListener) {
        super(scanAdapterListener);
        setHasStableIds(true);
        this.tiles = tiles;
    }

    @NonNull
    @Override
    public TileViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new TileViewHolder(getItemView(R.layout.viewholder_tile, viewGroup), adapterListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TileViewHolder viewHolder, final int position) {
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

    public interface AdapterListener extends InteractiveAdapter.AdapterListener {
        void onTileClicked(Tile tile);
    }
}
