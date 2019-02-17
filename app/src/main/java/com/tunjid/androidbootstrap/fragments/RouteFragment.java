package com.tunjid.androidbootstrap.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.PlaceHolder;
import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.RouteAdapter;
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment;
import com.tunjid.androidbootstrap.model.Route;
import com.tunjid.androidbootstrap.recyclerview.ListManagerBuilder;
import com.tunjid.androidbootstrap.viewholders.RouteItemViewHolder;
import com.tunjid.androidbootstrap.viewmodels.RouteViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

public class RouteFragment extends AppBaseFragment
        implements RouteAdapter.RouteAdapterListener {

    private RouteViewModel viewModel;

    public static RouteFragment newInstance() {
        RouteFragment fragment = new RouteFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = ViewModelProviders.of(this).get(RouteViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_route, container, false);

        new ListManagerBuilder<RouteItemViewHolder, PlaceHolder.State>()
                .withRecyclerView(rootView.findViewById(R.id.recycler_view))
                .withLinearLayoutManager()
                .withAdapter(new RouteAdapter(viewModel.getRoutes(), this))
                .build();

        return rootView;
    }

    @Override
    public void onItemClicked(Route route) {
        if (route.getDestination().equals(DoggoListFragment.class.getSimpleName())) {
            showFragment(DoggoListFragment.newInstance());
        } else if (route.getDestination().equals(BleScanFragment.class.getSimpleName())) {
            showFragment(BleScanFragment.newInstance());
        } else if (route.getDestination().equals(NsdScanFragment.class.getSimpleName())) {
            showFragment(NsdScanFragment.newInstance());
        } else if (route.getDestination().equals(HidingViewFragment.class.getSimpleName())) {
            showFragment(HidingViewFragment.newInstance());
        } else if (route.getDestination().equals(SpanbuilderFragment.class.getSimpleName())) {
            showFragment(SpanbuilderFragment.newInstance());
        } else if (route.getDestination().equals(ShiftingTileFragment.class.getSimpleName())) {
            showFragment(ShiftingTileFragment.newInstance());
        } else if (route.getDestination().equals(EndlessTileFragment.class.getSimpleName())) {
            showFragment(EndlessTileFragment.newInstance());
        } else if (route.getDestination().equals(DoggoRankFragment.class.getSimpleName())) {
            showFragment(DoggoRankFragment.newInstance());
        }
    }

}
