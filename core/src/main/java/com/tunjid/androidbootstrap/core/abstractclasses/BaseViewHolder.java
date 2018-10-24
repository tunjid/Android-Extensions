package com.tunjid.androidbootstrap.core.abstractclasses;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * Base {@link RecyclerView.ViewHolder}
 * <p>
 * Created by tj.dahunsi on 2/13/17.
 */

public abstract class BaseViewHolder<T extends BaseRecyclerViewAdapter.AdapterListener>
        extends RecyclerView.ViewHolder {

    protected T adapterListener;

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public BaseViewHolder(View itemView, T adapterListener) {
        super(itemView);
        this.adapterListener = adapterListener;
    }

}
