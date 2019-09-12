package com.tunjid.androidbootstrap.core.components

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner

internal fun <T> stateContainerFor(
        key: String,
        owner: T
): LifecycleSavedStateContainer where T : LifecycleOwner, T : SavedStateRegistryOwner =
        LifecycleSavedStateContainer(key, owner, owner)

class LifecycleSavedStateContainer(
        private val key: String,
        lifecycleOwner: LifecycleOwner,
        private val savedStateRegistryOwner: SavedStateRegistryOwner
) : LifecycleEventObserver, SavedStateRegistry.SavedStateProvider {

    val isFreshState: Boolean
    val savedState: Bundle

    init {
        lifecycleOwner.lifecycle.addObserver(this)
        savedStateRegistryOwner.savedStateRegistry.apply {
            val restoredState = consumeRestoredStateForKey(key)
            isFreshState = restoredState == null
            savedState = restoredState ?: Bundle()
            registerSavedStateProvider(key, this@LifecycleSavedStateContainer)
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) = when (event) {
        Lifecycle.Event.ON_DESTROY -> {
            source.lifecycle.removeObserver(this)
            savedStateRegistryOwner.savedStateRegistry.unregisterSavedStateProvider(key)
        }
        else -> Unit
    }

    override fun saveState(): Bundle = savedState
}