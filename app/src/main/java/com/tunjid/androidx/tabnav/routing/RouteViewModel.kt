package com.tunjid.androidx.tabnav.routing

import android.app.Application
import androidx.annotation.IdRes
import androidx.lifecycle.AndroidViewModel
import kotlin.random.Random

class RouteViewModel(application: Application) : AndroidViewModel(application) {

    private val mapping = application.routeItems

    operator fun get(@IdRes index: Int): List<RouteItem> = mapping[index]

    fun randomRoute() = Random.nextInt(mapping.size).let {
        it to mapping[it]
            .shuffled()
            .filterIsInstance<RouteItem.Destination>()
            .first()
    }
}
