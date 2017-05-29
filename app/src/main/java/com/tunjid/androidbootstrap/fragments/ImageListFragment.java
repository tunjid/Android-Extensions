package com.tunjid.androidbootstrap.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.ImageListAdapter;
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.model.Doggo;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment showing a static list of images
 * <p>
 * Created by tj.dahunsi on 5/6/17.
 */

public class ImageListFragment extends AppBaseFragment
        implements ImageListAdapter.ImageListAdapterListener {

    public static ImageListFragment newInstance() {
        ImageListFragment fragment = new ImageListFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    private final List<Doggo> doggos;
    private RecyclerView recyclerView;

    {
        doggos = new ArrayList<>();
        doggos.add(new Doggo(R.drawable.doggo1));
        doggos.add(new Doggo(R.drawable.doggo2));
        doggos.add(new Doggo(R.drawable.doggo3));
        doggos.add(new Doggo(R.drawable.doggo4));
        doggos.add(new Doggo(R.drawable.doggo5));
        doggos.add(new Doggo(R.drawable.doggo6));
        doggos.add(new Doggo(R.drawable.doggo7));
        doggos.add(new Doggo(R.drawable.doggo8));
        doggos.add(new Doggo(R.drawable.doggo9));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_route, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(new ImageListAdapter(doggos, this));
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
            Doggo doggo = fragmentTo.getArguments().getParcelable(ImageDetailFragment.ARG_DOGGO);
            if (doggo == null) return null;

            ImageListAdapter.ImageViewHolder holder = (ImageListAdapter.ImageViewHolder)
                    recyclerView.findViewHolderForItemId(doggo.hashCode());

            return getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .addSharedElement(holder.imageView, holder.doggo.hashCode() + "-" + holder.imageView.getId());
        }
        return null;
    }

    @Override
    public void onDoggoClicked(Doggo doggo) {
        showFragment(ImageDetailFragment.newInstance(doggo));
    }
}
