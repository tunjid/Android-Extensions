package com.tunjid.androidx.uidrivers

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class LifeCycleAwareCallback<T>(lifecycle: Lifecycle, implementation: (T) -> Unit) : (T) -> Unit {
    private var callback: (T) -> Unit = implementation

    init {
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) callback = {}
        })
    }

    override fun invoke(type: T) = callback.invoke(type)
}

fun <T> LifecycleOwner.callback(implementation: (T) -> Unit): LifeCycleAwareCallback<T> = LifeCycleAwareCallback(
    lifecycle = this.lifecycle,
    implementation = implementation
)