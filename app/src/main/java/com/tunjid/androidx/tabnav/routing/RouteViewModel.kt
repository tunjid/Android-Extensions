package com.tunjid.androidx.tabnav.routing

import android.app.Application
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import com.tunjid.androidx.R
import com.tunjid.androidx.core.text.italic
import com.tunjid.androidx.tabmisc.*
import com.tunjid.androidx.tabnav.RouteItem
import com.tunjid.androidx.tabcomms.ble.BleScanFragment
import com.tunjid.androidx.tabcomms.nsd.NsdScanFragment
import com.tunjid.androidx.tablists.doggo.DoggoListFragment
import com.tunjid.androidx.tablists.doggo.DoggoRankFragment
import com.tunjid.androidx.tablists.tables.SpreadSheetParentFragment
import com.tunjid.androidx.tablists.tables.StandingsFragment
import com.tunjid.androidx.tablists.tables.ViewPagerListAdapterFragment
import com.tunjid.androidx.tablists.tiles.EndlessTilesFragment
import com.tunjid.androidx.tablists.tiles.ShiftingTilesFragment
import com.tunjid.androidx.tabnav.navigator.IndependentStacksFragment
import com.tunjid.androidx.tabnav.navigator.MultipleStacksFragment
import kotlin.random.Random

class RouteViewModel(application: Application) : AndroidViewModel(application) {

    private val mapping = listOf(
        listOf(
            RouteItem.Destination(UiStatePlaygroundFragment::class.java.routeName, formatRoute(R.string.route_ui_state)),
            RouteItem.Destination(IndependentStacksFragment::class.java.routeName, formatRoute(R.string.route_independent_stack)),
            RouteItem.Destination(MultipleStacksFragment::class.java.routeName, formatRoute(R.string.route_multiple_inner_stack)),
            RouteItem.Spacer
        ),
        listOf(
            RouteItem.Destination(DoggoListFragment::class.java.routeName, formatRoute(R.string.route_doggo_list)),
            RouteItem.Destination(DoggoRankFragment::class.java.routeName, formatRoute(R.string.route_doggo_rank)),
            RouteItem.Destination(ShiftingTilesFragment::class.java.routeName, formatRoute(R.string.route_shifting_tile)),
            RouteItem.Destination(EndlessTilesFragment::class.java.routeName, formatRoute(R.string.route_endless_tile)),
            RouteItem.Destination(StandingsFragment::class.java.routeName, formatRoute(R.string.route_standings)),
            RouteItem.Destination(SpreadSheetParentFragment::class.java.routeName, formatRoute(R.string.route_spreadsheet)),
            RouteItem.Destination(ViewPagerListAdapterFragment::class.java.routeName, formatRoute(R.string.route_viewpager)),
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