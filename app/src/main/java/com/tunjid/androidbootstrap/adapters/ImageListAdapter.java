package com.tunjid.androidbootstrap.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.model.Doggo;
import com.tunjid.androidbootstrap.view.recyclerview.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.view.recyclerview.BaseViewHolder;

import java.util.List;

/**
 * Adapter for displaying links to various parts of the app
 * <p>
 * Created by tj.dahunsi on 5/6/16.
 */
public class ImageListAdapter extends BaseRecyclerViewAdapter<ImageListAdapter.ImageViewHolder, ImageListAdapter.ImageListAdapterListener> {

    private List<Doggo> doggos;

    public ImageListAdapter(List<Doggo> doggos, ImageListAdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.doggos = doggos;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_image_list, parent, false);

        return new ImageViewHolder(itemView, adapterListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int recyclerViewPosition) {
        final Doggo item = doggos.get(recyclerViewPosition);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return doggos.size();
    }

    @Override
    public long getItemId(int position) {
        return doggos.get(position).hashCode();
    }


    public interface ImageListAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onDoggoClicked(Doggo doggo);
    }

    public static class ImageViewHolder extends BaseViewHolder<ImageListAdapterListener>
            implements View.OnClickListener {

        public Doggo doggo;
        public final TextView textView;
        public final ImageView imageView;

        public ImageViewHolder(View itemView, @Nullable ImageListAdapterListener adapterListener) {
            super(itemView, adapterListener);

            textView = itemView.findViewById(R.id.doggo_name);
            imageView = itemView.findViewById(R.id.doggo_image);
            itemView.setOnClickListener(this);
        }

        public void bind(Doggo doggo) {
            this.doggo = doggo;

            ViewCompat.setTransitionName(imageView, doggo.hashCode() + "-" + imageView.getId());

            textView.setText(doggo.getName());

            Picasso.with(imageView.getContext())
                    .load(doggo.getImageRes())
                    .fit()
                    .centerCrop()
                    .into(imageView);
        }

        @Override
        public void onClick(View v) {
            if (adapterListener != null) adapterListener.onDoggoClicked(doggo);
        }
    }
}
