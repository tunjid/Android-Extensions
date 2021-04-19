package com.tunjid.androidx.core.components

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Analogous to [androidx.core.view.doOnLayout], this method performs the [action] immediately
 * if the [Lifecycle] is already in the specified state, otherwise it registers a
 * [LifecycleEventObserver] to listen for the [targetEvent], performs the [action],
 * then removes the [LifecycleEventObserver].
 */
fun Lifecycle.doOnEvent(
    targetEvent: Lifecycle.Event,
    action: () -> Unit
) {
    val lastReceivedEvent = currentState.lastReceivedEvent
    if (lastReceivedEvent != null && lastReceivedEvent >= targetEvent) return action()

    addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == targetEvent) {
                action()
                source.lifecycle.removeObserver(this)
            }
        }
    })
}

fun Lifecycle.doOnEveryEvent(
    targetEvent: Lifecycle.Event,
    action: () -> Unit
) {
    addObserver(LifecycleEventObserver { _, event ->
        if (event == targetEvent) action()
    })
}

private val Lifecycle.State.lastReceivedEvent
    get() = when (this) {
        Lifecycle.State.DESTROYED -> Lifecycle.Event.ON_DESTROY
        Lifecycle.State.INITIALIZED -> null
        Lifecycle.State.CREATED -> Lifecycle.Event.ON_CREATE
        Lifecycle.State.STARTED -> Lifecycle.Event.ON_START
        Lifecycle.State.RESUMED -> Lifecycle.Event.ON_RESUME
    }