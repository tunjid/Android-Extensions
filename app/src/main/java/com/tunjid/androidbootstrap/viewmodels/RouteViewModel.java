package com.tunjid.androidbootstrap.viewmodels;

import android.app.Application;
import android.content.Context;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.core.text.SpanBuilder;
import com.tunjid.androidbootstrap.fragments.BleScanFragment;
import com.tunjid.androidbootstrap.fragments.DoggoListFragment;
import com.tunjid.androidbootstrap.fragments.DoggoRankFragment;
import com.tunjid.androidbootstrap.fragments.HidingViewFragment;
import com.tunjid.androidbootstrap.fragments.NsdScanFragment;
import com.tunjid.androidbootstrap.fragments.SpanbuilderFragment;
import com.tunjid.androidbootstrap.fragments.TileFragment;
import com.tunjid.androidbootstrap.model.Route;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.AndroidViewModel;

public class RouteViewModel extends AndroidViewModel {

    private final List<Route> routes;

    public RouteViewModel(@NonNull Application application) {
        super(application);
        routes = Arrays.asList(
                new Route(DoggoListFragment.class.getSimpleName(), formatRoute(R.string.route_doggo_list)),
                new Route(DoggoRankFragment.class.getSimpleName(), formatRoute(R.string.route_doggo_rank)),
                new Route(TileFragment.class.getSimpleName(), formatRoute(R.string.route_tile)),
                new Route(HidingViewFragment.class.getSimpleName(), formatRoute(R.string.route_hiding_view)),
                new Route(SpanbuilderFragment.class.getSimpleName(), formatRoute(R.string.route_span_builder)),
                new Route(BleScanFragment.class.getSimpleName(), formatRoute(R.string.route_ble_scan)),
                new Route(NsdScanFragment.class.getSimpleName(), formatRoute(R.string.route_nsd_scan))
        );
    }

    public List<Route> getRoutes() {
        return routes;
    }

    private CharSequence formatRoute(@StringRes int stringRes) {
        Context context = getApplication();
        return SpanBuilder.of(context.getString(stringRes))
                .italic()
                .underline()
                .color(context, R.color.colorPrimary)
                .build();
    }
}
