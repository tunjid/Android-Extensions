package com.tunjid.androidx.viewmodels

import android.app.Application
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import com.tunjid.androidx.R
import com.tunjid.androidx.core.text.SpanBuilder
import com.tunjid.androidx.fragments.*
import com.tunjid.androidx.model.Route

class RouteViewModel(application: Application) : AndroidViewModel(application) {

    private val mapping = listOf(
            listOf(
                    Route(DoggoListFragment::class.java.simpleName, formatRoute(R.string.route_doggo_list)),
                    Route(IndependentStackFragment::class.java.simpleName, formatRoute(R.string.route_independent_stack)),
                    Route(MultipleStackFragment::class.java.simpleName, formatRoute(R.string.route_multiple_inner_stack))
            ),
            listOf(
                    Route(DoggoRankFragment::class.java.simpleName, formatRoute(R.string.route_doggo_rank)),
                    Route(ShiftingTileFragment::class.java.simpleName, formatRoute(R.string.route_shifting_tile)),
                    Route(EndlessTileFragment::class.java.simpleName, formatRoute(R.string.route_endless_tile))
            ),
            listOf(
                    Route(BleScanFragment::class.java.simpleName, formatRoute(R.string.route_ble_scan)),
                    Route(NsdScanFragment::class.java.simpleName, formatRoute(R.string.route_nsd_scan))
            ),
            listOf(
                    Route(HidingViewFragment::class.java.simpleName, formatRoute(R.string.route_hiding_view)),
                    Route(SpanbuilderFragment::class.java.simpleName, formatRoute(R.string.route_span_builder)),
                    Route(HardServiceConnectionFragment::class.java.simpleName, formatRoute(R.string.route_hard_service_connection))
            )
    )

    operator fun get(@IdRes index: Int): List<Route> = mapping[index]

    private fun formatRoute(@StringRes stringRes: Int): CharSequence = getApplication<Application>().run {
        SpanBuilder.of(getString(stringRes))
                .italic()
                .underline()
                .color(this, R.color.colorPrimary)
                .build()
    }
}
