package com.tunjid.androidbootstrap.viewmodels

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.core.text.SpanBuilder
import com.tunjid.androidbootstrap.fragments.BleScanFragment
import com.tunjid.androidbootstrap.fragments.DoggoListFragment
import com.tunjid.androidbootstrap.fragments.DoggoRankFragment
import com.tunjid.androidbootstrap.fragments.EndlessTileFragment
import com.tunjid.androidbootstrap.fragments.HidingViewFragment
import com.tunjid.androidbootstrap.fragments.NsdScanFragment
import com.tunjid.androidbootstrap.fragments.ShiftingTileFragment
import com.tunjid.androidbootstrap.fragments.SpanbuilderFragment
import com.tunjid.androidbootstrap.model.Route
import java.util.*

class RouteViewModel(application: Application) : AndroidViewModel(application) {

    val routes: List<Route> = Arrays.asList(
                Route(DoggoListFragment::class.java.simpleName, formatRoute(R.string.route_doggo_list)),
                Route(DoggoRankFragment::class.java.simpleName, formatRoute(R.string.route_doggo_rank)),
                Route(ShiftingTileFragment::class.java.simpleName, formatRoute(R.string.route_shifting_tile)),
                Route(EndlessTileFragment::class.java.simpleName, formatRoute(R.string.route_endless_tile)),
                Route(HidingViewFragment::class.java.simpleName, formatRoute(R.string.route_hiding_view)),
                Route(SpanbuilderFragment::class.java.simpleName, formatRoute(R.string.route_span_builder)),
                Route(BleScanFragment::class.java.simpleName, formatRoute(R.string.route_ble_scan)),
                Route(NsdScanFragment::class.java.simpleName, formatRoute(R.string.route_nsd_scan))
        )

    private fun formatRoute(@StringRes stringRes: Int): CharSequence {
        val context = getApplication<Application>()
        return SpanBuilder.of(context.getString(stringRes))
                .italic()
                .underline()
                .color(context, R.color.colorPrimary)
                .build()
    }
}
