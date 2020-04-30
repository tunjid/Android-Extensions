package com.tunjid.androidx.viewmodels

import android.app.Application
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import com.tunjid.androidx.R
import com.tunjid.androidx.core.text.italic
import com.tunjid.androidx.fragments.*
import com.tunjid.androidx.model.RouteItem
import kotlin.random.Random

class RouteViewModel(application: Application) : AndroidViewModel(application) {

    private val mapping = listOf(
            listOf(
                    RouteItem.Destination(DoggoListFragment::class.java.routeName, formatRoute(R.string.route_doggo_list)),
                    RouteItem.Destination(IndependentStacksFragment::class.java.routeName, formatRoute(R.string.route_independent_stack)),
                    RouteItem.Destination(MultipleStacksFragment::class.java.routeName, formatRoute(R.string.route_multiple_inner_stack)),
                    RouteItem.Spacer
            ),
            listOf(
                    RouteItem.Destination(DoggoRankFragment::class.java.routeName, formatRoute(R.string.route_doggo_rank)),
                    RouteItem.Destination(ShiftingTilesFragment::class.java.routeName, formatRoute(R.string.route_shifting_tile)),
                    RouteItem.Destination(EndlessTilesFragment::class.java.routeName, formatRoute(R.string.route_endless_tile)),
                    RouteItem.Destination(SpreadSheetParentFragment::class.java.routeName, formatRoute(R.string.route_spreadsheet)),
                    RouteItem.Spacer
            ),
            listOf(
                    RouteItem.Destination(BleScanFragment::class.java.routeName, formatRoute(R.string.route_ble_scan)),
                    RouteItem.Destination(NsdScanFragment::class.java.routeName, formatRoute(R.string.route_nsd_scan)),
                    RouteItem.Spacer
            ),
            listOf(
                    RouteItem.Destination(SpringAnimationFragment::class.java.routeName, formatRoute(R.string.route_spring_animation)),
                    RouteItem.Destination(FabTransformationsFragment::class.java.routeName, formatRoute(R.string.route_fab_transformations)),
                    RouteItem.Destination(CharacterSequenceExtensionsFragment::class.java.routeName, formatRoute(R.string.route_span_builder)),
                    RouteItem.Destination(HardServiceConnectionFragment::class.java.routeName, formatRoute(R.string.route_hard_service_connection)),
                    RouteItem.Spacer
            )
    )

    operator fun get(@IdRes index: Int): List<RouteItem> = mapping[index]

    fun randomRoute() = Random.nextInt(mapping.size).let {
        it to mapping[it]
                .shuffled()
                .filterIsInstance<RouteItem.Destination>()
                .first()
    }

    private fun formatRoute(@StringRes stringRes: Int): CharSequence = getApplication<Application>().run {
        getString(stringRes).italic()
    }
}


val <T : Fragment> Class<T>.routeName
    get() = simpleName
            .replace("Fragment", "")
            .replace(
                    String.format("%s|%s|%s",
                            "(?<=[A-Z])(?=[A-Z][a-z])",
                            "(?<=[^A-Z])(?=[A-Z])",
                            "(?<=[A-Za-z])(?=[^A-Za-z])"
                    ).toRegex(),
                    " "
            )