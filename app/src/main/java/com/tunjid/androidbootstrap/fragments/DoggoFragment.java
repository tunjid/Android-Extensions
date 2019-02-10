package com.tunjid.androidbootstrap.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.DoggoAdapter.ImageListAdapterListener;
import com.tunjid.androidbootstrap.viewholders.DoggoViewHolder;
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
        Doggo doggo = getArguments().getParcelable(ARG_DOGGO);

        rootView.setTag(doggo);
        new DoggoViewHolder(rootView, this).bind(doggo);

        return rootView;
    }

    public void toggleToolbar(boolean show) { /* Nothing, delegate to parent fragment */ }

    public void toggleFab(boolean show) { /* Nothing, delegate to parent fragment */ }

    public void togglePersistentUi() { /* Nothing, delegate to parent fragment */ }

    @SuppressWarnings("ConstantConditions")
    public void onDoggoImageLoaded(Doggo doggo) {
        getParentFragment().startPostponedEnterTransition();
    }
}
