package com.tunjid.androidbootstrap.viewholders

import android.view.View
import android.widget.TextView

import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.adapters.DoggoAdapter
import com.tunjid.androidbootstrap.model.Doggo

class DoggoRankViewHolder(itemView: View, adapterListener: DoggoAdapter.ImageListAdapterListener)
    : DoggoViewHolder(itemView, adapterListener) {

    val dragView: View = itemView.findViewById(R.id.drag_handle)
    private val doggoRank: TextView = itemView.findViewById(R.id.doggo_rank)

    override fun bind(doggo: Doggo) {
        super.bind(doggo)
        doggoRank.text = (adapterPosition + 1).toString()
    }
}
