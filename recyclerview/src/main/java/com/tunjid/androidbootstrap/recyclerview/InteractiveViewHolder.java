package com.tunjid.androidbootstrap.recyclerview;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public abstract class InteractiveViewHolder<T>
        extends RecyclerView.ViewHolder {

    protected T adapterListener;

    public InteractiveViewHolder(View itemView) {
        super(itemView);
    }

    public InteractiveViewHolder(View itemView, T adapterListener) {
        super(itemView);
        this.adapterListener = adapterListener;
    }
}
