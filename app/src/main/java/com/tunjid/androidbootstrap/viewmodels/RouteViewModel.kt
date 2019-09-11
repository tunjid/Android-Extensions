package com.tunjid.androidbootstrap.viewmodels

import android.app.Application
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.core.text.SpanBuilder
import com.tunjid.androidbootstrap.fragments.*
import com.tunjid.androidbootstrap.model.Route

class RouteViewModel(application: Application) : AndroidViewModel(application) {

    private val mapping = mutableMapOf(
            R.id.menu_core to listOf(
                    Route(DoggoListFragment::class.java.simpleName, formatRoute(R.string.route_doggo_list)),
                    Route(HidingViewFragment::class.java.simpleName, formatRoute(R.string.route_hiding_view)),
                    Route(SpanbuilderFragment::class.java.simpleName, formatRoute(R.string.route_span_builder))
            ),
            R.id.menu_recyclerview to listOf(
                    Route(DoggoRankFragment::class.java.simpleName, formatRoute(R.string.route_doggo_rank)),
                    Route(ShiftingTileFragment::class.java.simpleName, formatRoute(R.string.route_shifting_tile)),
                    Route(EndlessTileFragment::class.java.simpleName, formatRoute(R.string.route_endless_tile))
            ),
            R.id.menu_communications to listOf(
                    Route(BleScanFragment::class.java.simpleName, formatRoute(R.string.route_ble_scan)),
                    Route(NsdScanFragment::class.java.simpleName, formatRoute(R.string.route_nsd_scan))
            )
    )

    fun getRoutes(@IdRes id: Int): List<Route> = mapping[id] ?: mapping.values.flatten()

    private fun formatRoute(@StringRes stringRes: Int): CharSequence = getApplication<Application>().run {
        SpanBuilder.of(getString(stringRes))
                .italic()
                .underline()
                .color(this, R.color.colorPrimary)
                .build()
    }
}
