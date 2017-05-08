package com.tunjid.androidbootstrap.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.ImageListAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment showing a static list of images
 * <p>
 * Created by tj.dahunsi on 5/6/17.
 */

public class ImageListFragment extends BaseFragment
        implements ImageListAdapter.ImageListAdapterListener {

    public static ImageListFragment newInstance() {
        ImageListFragment fragment = new ImageListFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    private final List<Integer> imageResources;
    private RecyclerView recyclerView;

    {
        imageResources = new ArrayList<>();
        imageResources.add(R.drawable.doggo1);
        imageResources.add(R.drawable.doggo2);
        imageResources.add(R.drawable.doggo3);
        imageResources.add(R.drawable.doggo4);
        imageResources.add(R.drawable.doggo5);
        imageResources.add(R.drawable.doggo6);
        imageResources.add(R.drawable.doggo7);
        imageResources.add(R.drawable.doggo8);
        imageResources.add(R.drawable.doggo9);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_route, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(new ImageListAdapter(imageResources, this));
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
    }

    @Override
    @Nullable
    @SuppressLint("CommitTransaction")
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        if (fragmentTo.getStableTag().contains(ImageDetailFragment.class.getSimpleName())) {

            ImageListAdapter.ImageViewHolder holder = (ImageListAdapter.ImageViewHolder)
                    recyclerView.findViewHolderForItemId(fragmentTo.getArguments().getInt(ImageDetailFragment.ARG_IMAGE_RESOURCE));

            return getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .addSharedElement(holder.imageView, holder.imageResource + "-" + holder.imageView.getId());
        }
        return super.provideFragmentTransaction(fragmentTo);
    }

    @Override
    public void onItemClicked(@DrawableRes int imageResource) {
        showFragment(ImageDetailFragment.newInstance(imageResource));
    }
}
