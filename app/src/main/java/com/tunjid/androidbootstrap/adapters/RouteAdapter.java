package com.tunjid.androidbootstrap.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

/**
 * Adapter for displaying links to various parts of the app
 * <p>
 * Created by tj.dahunsi on 5/6/16.
 */
public class RouteAdapter extends BaseRecyclerViewAdapter<RouteAdapter.RouteItemViewHolder, RouteAdapter.RouteAdapterListener> {

    private List<String> socialItems;

    public RouteAdapter(List<String> routes, RouteAdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.socialItems = routes;
    }

    @Override
    public RouteItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).
                inflate(R.layout.viewholder_text, parent, false);

        return new RouteItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RouteItemViewHolder holder, int recyclerViewPosition) {
        final String item = socialItems.get(recyclerViewPosition);
        holder.bind(item, adapterListener);
    }

    @Override
    public int getItemCount() {
        return socialItems.size();
    }

    @Override
    public long getItemId(int position) {
        return socialItems.get(position).hashCode();
    }


    public interface RouteAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onItemClicked(String item);
    }

    static class RouteItemViewHolder extends BaseViewHolder<RouteAdapterListener>
            implements View.OnClickListener {

        String item;

        TextView textView;

        RouteItemViewHolder(View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.text);

            itemView.setOnClickListener(this);
            textView.setOnClickListener(this);
        }

        void bind(String item, RouteAdapterListener socialAdapterListener) {

            this.item = item;
            adapterListener = socialAdapterListener;

            textView.setText(item);
        }

        @Override
        public void onClick(View v) {
            adapterListener.onItemClicked(item);
        }
    }
}
