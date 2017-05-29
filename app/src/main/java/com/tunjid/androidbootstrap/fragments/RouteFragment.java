package com.tunjid.androidbootstrap.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.RouteAdapter;
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment used to test the environment switcher.
 * <p>
 * Created by tj.dahunsi on 5/6/16.
 */
public class RouteFragment extends AppBaseFragment
        implements RouteAdapter.RouteAdapterListener {

    private List<String> routes = new ArrayList<>();

    public static RouteFragment newInstance() {
        RouteFragment fragment = new RouteFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        routes.add(ImageListFragment.class.getSimpleName());
        routes.add(BleScanFragment.class.getSimpleName());
        routes.add(NsdScanFragment.class.getSimpleName());
        routes.add(HidingViewFragment.class.getSimpleName());
        routes.add(SpanbuilderFragment.class.getSimpleName());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_route, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new RouteAdapter(routes, this));

        return rootView;
    }

    @Override
    public void onItemClicked(String item) {
        if (item.equals(ImageListFragment.class.getSimpleName())) {
            showFragment(ImageListFragment.newInstance());
        }
        else if (item.equals(BleScanFragment.class.getSimpleName())) {
            showFragment(BleScanFragment.newInstance());
        }
        else if (item.equals(NsdScanFragment.class.getSimpleName())) {
            showFragment(NsdScanFragment.newInstance());
        }
        else if (item.equals(HidingViewFragment.class.getSimpleName())) {
            showFragment(HidingViewFragment.newInstance());
        }
        else if (item.equals(SpanbuilderFragment.class.getSimpleName())) {
            showFragment(SpanbuilderFragment.newInstance());
        }
    }
}
