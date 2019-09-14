package com.tunjid.androidbootstrap.adapters

import android.view.ViewGroup

import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.model.Tile
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter
import com.tunjid.androidbootstrap.view.util.inflate
import com.tunjid.androidbootstrap.viewholders.TileViewHolder

class TileAdapter(private val tiles: List<Tile>, listener: (tile: Tile) -> Unit)
    : InteractiveAdapter<TileViewHolder, (tile: Tile) -> Unit>(listener) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): TileViewHolder =
            TileViewHolder(viewGroup.inflate(R.layout.viewholder_tile), adapterDelegate)

    override fun onBindViewHolder(viewHolder: TileViewHolder, position: Int) =
            viewHolder.bind(tiles[position])

    override fun onViewRecycled(holder: TileViewHolder) {
        super.onViewRecycled(holder)
        holder.unBind()
    }

    override fun getItemId(position: Int): Long = tiles[position].number.toLong()

    override fun getItemCount(): Int = tiles.size

}
