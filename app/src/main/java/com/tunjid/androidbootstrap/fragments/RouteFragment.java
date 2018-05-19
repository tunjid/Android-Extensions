package com.tunjid.androidbootstrap.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.RouteAdapter;
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment;
import com.tunjid.androidbootstrap.core.text.SpanBuilder;
import com.tunjid.androidbootstrap.model.Route;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment used to test the environment switcher.
 * <p>
 * Created by tj.dahunsi on 5/6/16.
 */
public class RouteFragment extends AppBaseFragment
        implements RouteAdapter.RouteAdapterListener {

    private List<Route> routes = new ArrayList<>();

    public static RouteFragment newInstance() {
        RouteFragment fragment = new RouteFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_route, container, false);

        routes.clear();
        routes.add(new Route(ImageListFragment.class.getSimpleName(), formatRoute(R.string.route_image_list)));
        routes.add(new Route(BleScanFragment.class.getSimpleName(), formatRoute(R.string.route_ble_scan)));
        routes.add(new Route(NsdScanFragment.class.getSimpleName(), formatRoute(R.string.route_nsd_scan)));
        routes.add(new Route(HidingViewFragment.class.getSimpleName(), formatRoute(R.string.route_hiding_view)));
        routes.add(new Route(SpanbuilderFragment.class.getSimpleName(), formatRoute(R.string.route_span_builder)));

        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new RouteAdapter(routes, this));

        return rootView;
    }

    @Override
    public void onItemClicked(Route route) {
        if (route.getDestination().equals(ImageListFragment.class.getSimpleName())) {
            showFragment(ImageListFragment.newInstance());
        }
        else if (route.getDestination().equals(BleScanFragment.class.getSimpleName())) {
            showFragment(BleScanFragment.newInstance());
        }
        else if (route.getDestination().equals(NsdScanFragment.class.getSimpleName())) {
            showFragment(NsdScanFragment.newInstance());
        }
        else if (route.getDestination().equals(HidingViewFragment.class.getSimpleName())) {
            showFragment(HidingViewFragment.newInstance());
        }
        else if (route.getDestination().equals(SpanbuilderFragment.class.getSimpleName())) {
            showFragment(SpanbuilderFragment.newInstance());
        }
    }

    private CharSequence formatRoute(@StringRes int stringRes) {
        return new SpanBuilder(requireContext(), getString(stringRes)).italic().underline().color(R.color.colorPrimary).build();
    }
}
