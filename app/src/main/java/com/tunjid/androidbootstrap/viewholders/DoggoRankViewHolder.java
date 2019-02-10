package com.tunjid.androidbootstrap.viewholders;

import android.view.View;
import android.widget.TextView;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.DoggoAdapter;
import com.tunjid.androidbootstrap.model.Doggo;

public class DoggoRankViewHolder extends DoggoViewHolder{

    private final View dragView;
    private final TextView doggoRank;

    public DoggoRankViewHolder(View itemView, DoggoAdapter.ImageListAdapterListener adapterListener) {
        super(itemView, adapterListener);
        dragView = itemView.findViewById(R.id.drag_handle);
        doggoRank = itemView.findViewById(R.id.doggo_rank);
    }

    @Override
    public void bind(Doggo doggo) {
        super.bind(doggo);
        doggoRank.setText(String.valueOf(getAdapterPosition() + 1));
    }

    public View getDragView () {
        return dragView;
    }
}
