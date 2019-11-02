package com.tunjid.androidx.viewholders

import android.view.View
import android.widget.TextView

import com.tunjid.androidx.R
import com.tunjid.androidx.adapters.ImageListAdapterListener
import com.tunjid.androidx.model.Doggo

class DoggoRankViewHolder(itemView: View, adapterListener: ImageListAdapterListener)
    : DoggoViewHolder(itemView, adapterListener) {

    val dragView: View = itemView.findViewById(R.id.drag_handle)
    private val doggoRank: TextView = itemView.findViewById(R.id.doggo_rank)

    override fun bind(doggo: Doggo) {
        super.bind(doggo)
        doggoRank.text = (adapterPosition + 1).toString()
    }
}
