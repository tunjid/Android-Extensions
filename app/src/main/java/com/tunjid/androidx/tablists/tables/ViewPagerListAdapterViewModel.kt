package com.tunjid.androidx.tablists.tables

import android.app.Application
import android.content.res.Resources
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import com.tunjid.androidx.tabnav.routing.RouteItem
import com.tunjid.androidx.tabnav.routing.fragment
import com.tunjid.androidx.tabnav.routing.routeDestinations
import com.tunjid.androidx.tabnav.routing.routeName
import com.tunjid.androidx.toLiveData
import com.tunjid.viewpager2.FragmentTab
import io.reactivex.processors.PublishProcessor


class ViewPagerListAdapterViewModel(application: Application) : AndroidViewModel(application) {

    private val processor = PublishProcessor.create<Input>()

    val state = processor.scan(State(
        allItems = application.routeDestinations
            .filterNot { it.destination == ViewPagerListAdapterFragment::class.java.routeName }
            .map(::RouteTab),
    )) { state, input ->
        when (input) {
            is Input.Add -> state.copy(visibleItems = state.visibleItems + input.tab)
            is Input.Remove -> state.copy(visibleItems = state.visibleItems - input.tab)
            Input.Shuffle -> state.copy(visibleItems = state.allItems.shuffled().take(4))
        }
    }.toLiveData()

    fun accept(input: Input) = processor.onNext(input)
}

data class State(
    val allItems: List<RouteTab>,
    val visibleItems: List<RouteTab> = allItems.shuffled().take(4),
)

fun State?.items(res: Resources): Pair<Array<CharSequence>, BooleanArray> = when (this) {
    null -> emptyArray<CharSequence>() to BooleanArray(0)
    else -> Pair(
        first = allItems.map { it.title(res) }.toTypedArray(),
        second = visibleItems.toSet().let { set -> allItems.map(set::contains) }.toBooleanArray())
}

sealed class Input {
    data class Add(val tab: RouteTab) : Input()
    data class Remove(val tab: RouteTab) : Input()
    object Shuffle : Input()
}

data class RouteTab(val route: RouteItem.Destination) : FragmentTab {
    override fun title(res: Resources): CharSequence = route.fragment(isTopLevel = false).javaClass.routeName

    override fun createFragment(): Fragment = route.fragment(isTopLevel = false)
}
