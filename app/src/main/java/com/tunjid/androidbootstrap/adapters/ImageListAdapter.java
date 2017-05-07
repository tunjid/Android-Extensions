package com.tunjid.androidbootstrap.adapters;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

/**
 * Adapter for displaying links to various parts of the app
 * <p>
 * Created by tj.dahunsi on 5/6/16.
 */
public class ImageListAdapter extends BaseRecyclerViewAdapter<ImageListAdapter.ImageViewHolder, ImageListAdapter.ImageListAdapterListener> {

    private List<Integer> imageResources;

    public ImageListAdapter(List<Integer> imageResources, ImageListAdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.imageResources = imageResources;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_image, parent, false);

        return new ImageViewHolder(itemView, adapterListener);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int recyclerViewPosition) {
        final int item = imageResources.get(recyclerViewPosition);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return imageResources.size();
    }

    @Override
    public long getItemId(int position) {
        return imageResources.get(position);
    }


    public interface ImageListAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onItemClicked(@DrawableRes int imageResource);
    }

    public static class ImageViewHolder extends BaseViewHolder<ImageListAdapterListener>
            implements View.OnClickListener {

        public int imageResource;
        public ImageView imageView;

        public ImageViewHolder(View itemView, @Nullable ImageListAdapterListener adapterListener) {
            super(itemView, adapterListener);

            imageView = (ImageView) itemView.findViewById(R.id.image);
            itemView.setOnClickListener(this);
        }

        public void bind(int imageResource) {
            this.imageResource = imageResource;

            ViewCompat.setTransitionName(imageView, imageResource + "-" + imageView.getId());

            Picasso.with(imageView.getContext())
                    .load(imageResource)
                    .fit()
                    .centerCrop()
                    .into(imageView);
        }

        @Override
        public void onClick(View v) {
            if (adapterListener != null) adapterListener.onItemClicked(imageResource);
        }
    }
}
