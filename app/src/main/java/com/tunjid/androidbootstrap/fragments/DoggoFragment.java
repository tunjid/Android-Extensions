package com.tunjid.androidbootstrap.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.ImageListAdapter.ImageListAdapterListener;
import com.tunjid.androidbootstrap.adapters.ImageListAdapter.ImageViewHolder;
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment;
import com.tunjid.androidbootstrap.model.Doggo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DoggoFragment extends AppBaseFragment
        implements ImageListAdapterListener {

    private static final String ARG_DOGGO = "doggo";

    public static DoggoFragment newInstance(Doggo doggo) {
        DoggoFragment fragment = new DoggoFragment();
        Bundle args = new Bundle();
        args.putParcelable("doggo", doggo);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        return super.getStableTag() + "-" + getArguments().getParcelable(ARG_DOGGO);
    }

    @Nullable
    @SuppressWarnings("ConstantConditions")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_detail, container, false);
        new ImageViewHolder(rootView, this).bind(getArguments().getParcelable(ARG_DOGGO));
        return rootView;
    }

    public void toggleToolbar(boolean show) { /* Nothing, delegate to parent fragment */ }

    public void toggleFab(boolean show) { /* Nothing, delegate to parent fragment */ }

    public void togglePersistentUi() { /* Nothing, delegate to parent fragment */ }

    public void onDoggoClicked(Doggo doggo) { }

    public void onDoggoImageLoaded() {
        getParentFragment().startPostponedEnterTransition();
    }
}
