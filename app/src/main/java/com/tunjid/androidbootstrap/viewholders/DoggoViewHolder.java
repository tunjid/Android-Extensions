package com.tunjid.androidbootstrap.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.DoggoAdapter;
import com.tunjid.androidbootstrap.model.Doggo;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;
import com.tunjid.androidbootstrap.view.util.ViewUtil;

import androidx.core.view.ViewCompat;

public class DoggoViewHolder extends InteractiveViewHolder<DoggoAdapter.ImageListAdapterListener>
        implements View.OnClickListener {

    private static final int FULL_SIZE_DELAY = 100;
    private static final int THUMBNAIL_SIZE = 250;

    private Doggo doggo;
    private final TextView textView;
    public final ImageView fullSize;
    public final ImageView thumbnail;

    public DoggoViewHolder(View itemView, DoggoAdapter.ImageListAdapterListener adapterListener) {
        super(itemView, adapterListener);

        textView = itemView.findViewById(R.id.doggo_name);
        fullSize = itemView.findViewById(R.id.full_size);
        thumbnail = itemView.findViewById(R.id.doggo_image);
        itemView.setOnClickListener(this);
    }

    public void bind(Doggo doggo) {
        this.doggo = doggo;

        ViewCompat.setTransitionName(thumbnail, ViewUtil.transitionName(doggo, thumbnail));
        getCreator(doggo)
                .resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                .into(thumbnail, onSuccess(this::onThumbnailLoaded));

        textView.setText(doggo.getName());
    }

    private RequestCreator getCreator(Doggo doggo) {
        return Picasso.with(thumbnail.getContext())
                .load(doggo.getImageRes()).centerCrop();
    }

    private void onThumbnailLoaded() {
        adapterListener.onDoggoImageLoaded(doggo);
        if (fullSize != null) fullSize.postDelayed(() -> getCreator(doggo).fit()
                .into(fullSize, onSuccess(() -> fullSize.setVisibility(View.VISIBLE))), FULL_SIZE_DELAY);
    }

    private Callback onSuccess(Runnable runnable) {
        return new Callback() {
            public void onSuccess() { runnable.run(); }

            public void onError() { }
        };
    }

    @Override
    public void onClick(View v) { adapterListener.onDoggoClicked(doggo); }
}
