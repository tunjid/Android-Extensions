package com.tunjid.androidx.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

internal val Bundle?.hashString: String
    get() = if (this == null) ""
    else keySet().joinToString(separator = "-", transform = { get(it)?.toString() ?: it })

internal val Fragment.backStackTag: String
    get() = "${javaClass.simpleName}-${arguments.hashString}"

internal fun Fragment.doOnLifeCycleOnce(
        targetEvent: Lifecycle.Event,
        action: () -> Unit
) = when {
    lifecycle.currentState.isAtLeast(targetEvent.toState) -> action()
    else -> lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) = when (event) {
            targetEvent -> {
                action()
                lifecycle.removeObserver(this)
            }
            else -> Unit
        }
    })
}

private val Lifecycle.Event.toState
    get() = when (this) {
        Lifecycle.Event.ON_CREATE -> Lifecycle.State.CREATED
        Lifecycle.Event.ON_START -> Lifecycle.State.STARTED
        Lifecycle.Event.ON_RESUME -> Lifecycle.State.RESUMED
        Lifecycle.Event.ON_PAUSE -> Lifecycle.State.DESTROYED
        Lifecycle.Event.ON_STOP -> Lifecycle.State.DESTROYED
        Lifecycle.Event.ON_DESTROY -> Lifecycle.State.DESTROYED
        Lifecycle.Event.ON_ANY -> Lifecycle.State.DESTROYED
    }