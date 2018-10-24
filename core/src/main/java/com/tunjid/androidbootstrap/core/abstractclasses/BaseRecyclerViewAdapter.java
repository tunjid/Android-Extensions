package com.tunjid.androidbootstrap.core.abstractclasses;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Base {@link RecyclerView.Adapter}
 * <p>
 * Created by tj.dahunsi on 2/13/17.
 */

public abstract class BaseRecyclerViewAdapter<VH extends BaseViewHolder, T extends BaseRecyclerViewAdapter.AdapterListener>
        extends RecyclerView.Adapter<VH> {

    protected T adapterListener;

    public BaseRecyclerViewAdapter(T adapterListener) {
        this();
        this.adapterListener = adapterListener;
    }

    public BaseRecyclerViewAdapter() {
    }


    /**
     * An interface for any iteraction this adapter carries
     */
    public interface AdapterListener {

    }

}
