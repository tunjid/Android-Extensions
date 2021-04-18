package com.tunjid.androidx.tabnav.routing

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.tunjid.androidx.R
import com.tunjid.androidx.core.text.italic
import com.tunjid.androidx.tabcomms.ble.BleScanFragment
import com.tunjid.androidx.tabcomms.nsd.NsdScanFragment
import com.tunjid.androidx.tablists.doggo.DoggoListFragment
import com.tunjid.androidx.tablists.doggo.DoggoRankFragment
import com.tunjid.androidx.tablists.doggo.RankArgs
import com.tunjid.androidx.tablists.tables.SpreadSheetParentFragment
import com.tunjid.androidx.tablists.tables.StandingsFragment
import com.tunjid.androidx.tablists.tables.ViewPagerListAdapterFragment
import com.tunjid.androidx.tablists.tiles.EndlessTilesFragment
import com.tunjid.androidx.tablists.tiles.ShiftingTilesFragment
import com.tunjid.androidx.tabmisc.CharacterSequenceExtensionsFragment
import com.tunjid.androidx.tabmisc.FabTransformationsFragment
import com.tunjid.androidx.tabmisc.HardServiceConnectionFragment
import com.tunjid.androidx.tabmisc.SpringAnimationFragment
import com.tunjid.androidx.tabmisc.UiStatePlaygroundFragment
import com.tunjid.androidx.tabnav.navigator.IndependentStacksFragment
import com.tunjid.androidx.tabnav.navigator.MultipleStacksFragment

/**
 * Routes in the sample app
 *
 *
 * Created by tj.dahunsi on 5/30/17.
 */

sealed class RouteItem {
    object Spacer : RouteItem()
    data class Destination(
        val destination: CharSequence,
        val description: CharSequence
    ) : RouteItem(), Parcelable {
        constructor(parcel: Parcel) : this(
            TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel),
            TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel))

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            TextUtils.writeToParcel(destination, parcel, 0)
            TextUtils.writeToParcel(description, parcel, 0)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Destination> {
            override fun createFromParcel(parcel: Parcel): Destination = Destination(parcel)

            override fun newArray(size: Int): Array<Destination?> = arrayOfNulls(size)
        }
    }
}

private fun Context.formatRoute(@StringRes stringRes: Int): CharSequence = getString(stringRes).italic()

val Context.routeDestinations get() = routeItems.flatten().filterIsInstance<RouteItem.Destination>()

val Context.routeItems
    get() = listOf(
        listOf(
            RouteItem.Destination(UiStatePlaygroundFragment::class.java.routeName, formatRoute(R.string.route_ui_state)),
            RouteItem.Destination(IndependentStacksFragment::class.java.routeName, formatRoute(R.string.route_independent_stack)),
            RouteItem.Destination(MultipleStacksFragment::class.java.routeName, formatRoute(R.string.route_multiple_inner_stack)),
            RouteItem.Spacer
        ),
        listOf(
            RouteItem.Destination(DoggoListFragment::class.java.routeName, formatRoute(R.string.route_doggo_list)),
            RouteItem.Destination(DoggoRankFragment::class.java.routeName, formatRoute(R.string.route_doggo_rank)),
            RouteItem.Destination(ViewPagerListAdapterFragment::class.java.routeName, formatRoute(R.string.route_viewpager)),
            RouteItem.Destination(ShiftingTilesFragment::class.java.routeName, formatRoute(R.string.route_shifting_tile)),
            RouteItem.Destination(EndlessTilesFragment::class.java.routeName, formatRoute(R.string.route_endless_tile)),
            RouteItem.Destination(StandingsFragment::class.java.routeName, formatRoute(R.string.route_standings)),
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

fun RouteItem.Destination.fragment(isTopLevel: Boolean): Fragment = when (destination) {
    DoggoListFragment::class.java.routeName -> DoggoListFragment.newInstance(isTopLevel = isTopLevel)
    BleScanFragment::class.java.routeName -> BleScanFragment.newInstance(isTopLevel = isTopLevel)
    NsdScanFragment::class.java.routeName -> NsdScanFragment.newInstance(isTopLevel = isTopLevel)
    SpringAnimationFragment::class.java.routeName -> SpringAnimationFragment.newInstance(isTopLevel = isTopLevel)
    CharacterSequenceExtensionsFragment::class.java.routeName -> CharacterSequenceExtensionsFragment.newInstance(isTopLevel = isTopLevel)
    ShiftingTilesFragment::class.java.routeName -> ShiftingTilesFragment.newInstance(isTopLevel = isTopLevel)
    EndlessTilesFragment::class.java.routeName -> EndlessTilesFragment.newInstance(isTopLevel = isTopLevel)
    DoggoRankFragment::class.java.routeName -> DoggoRankFragment.newInstance(RankArgs(isRanking = true, isTopLevel = isTopLevel))
    IndependentStacksFragment::class.java.routeName -> IndependentStacksFragment.newInstance(isTopLevel = isTopLevel)
    MultipleStacksFragment::class.java.routeName -> MultipleStacksFragment.newInstance(isTopLevel = isTopLevel)
    HardServiceConnectionFragment::class.java.routeName -> HardServiceConnectionFragment.newInstance(isTopLevel = isTopLevel)
    FabTransformationsFragment::class.java.routeName -> FabTransformationsFragment.newInstance(isTopLevel = isTopLevel)
    StandingsFragment::class.java.routeName -> StandingsFragment.newInstance(isTopLevel = isTopLevel)
    SpreadSheetParentFragment::class.java.routeName -> SpreadSheetParentFragment.newInstance(isTopLevel = isTopLevel)
    UiStatePlaygroundFragment::class.java.routeName -> UiStatePlaygroundFragment.newInstance(isTopLevel = isTopLevel)
    ViewPagerListAdapterFragment::class.java.routeName -> ViewPagerListAdapterFragment.newInstance(isTopLevel = isTopLevel)
    else -> throw IllegalArgumentException("Unknown destination")
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