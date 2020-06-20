package com.tunjid.androidx.navigation

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

internal val Bundle?.hashString: String
    get() = if (this == null) ""
    else keySet().joinToString(separator = "-", transform = { get(it)?.toString() ?: it })

internal val Fragment.bundleTag: String
    get() = "${javaClass.name}-${arguments.hashString}"

internal val Fragment.navigatorTag
    get() = if (this is Navigator.TagProvider) stableTag else bundleTag

/**
 * Analogous to [androidx.core.view.doOnLayout], this method performs the [action] immediately
 * if the [Fragment] is already in the specified state, otherwise it registers a
 * [LifecycleEventObserver] to listen for the [targetEvent], performs the [action],
 * then removes the [LifecycleEventObserver].
 */
fun Fragment.doOnLifecycleEvent(
        targetEvent: Lifecycle.Event,
        action: () -> Unit
) {
    val lastReceivedEvent = lifecycle.currentState.lastReceivedEvent
    if (lastReceivedEvent != null && lastReceivedEvent >= targetEvent) return action()

    lifecycle.addObserver { observer, _, event ->
        if (event == targetEvent) {
            action()
            lifecycle.removeObserver(observer)
        }
    }
}

/**
 * Registers an [OnBackPressedCallback] for this [Fragment].
 *
 * NOTE: The [OnBackPressedDispatcher] addCallback extension method optionally takes a [Lifecycle]
 * to automatically remove the added callback when destroyed. However Android dispatches onStart
 * to Fragments and Activities in parallel, which can cause a Fragment's callback to defer to the
 * Activity's first. This is bad because the Fragment's callback should take precedence over the
 * activity's.
 *
 * The solution is to register the Fragment's callback with the Activity's lifecycle, but manually
 * remove it when the Fragment is destroyed so it isn't leaked.
 *
 * @see <a href="https://issuetracker.google.com/issues/145688725">Issue 1<a/>
 * @see <a href="https://issuetracker.google.com/issues/133272537">Issue 2<a/>
 * @see <a href="https://issuetracker.google.com/issues/127528777">Issue 3<a/>
 */

fun Fragment.addOnBackPressedCallback(action: OnBackPressedCallback.() -> Unit): OnBackPressedCallback {
    val host = requireActivity()
    val callback = host.onBackPressedDispatcher.addCallback(host, false) { action(this) }
    lifecycle.addObserver { observer, source, event ->
        when (event) {
            Lifecycle.Event.ON_START -> callback.isEnabled = true
            Lifecycle.Event.ON_PAUSE -> callback.isEnabled = false
            Lifecycle.Event.ON_DESTROY -> {
                callback.remove()
                source.lifecycle.removeObserver(observer)
            }
            else -> Unit
        }
    }
    return callback
}

private val Lifecycle.State.lastReceivedEvent
    get() = when (this) {
        Lifecycle.State.DESTROYED -> Lifecycle.Event.ON_DESTROY
        Lifecycle.State.INITIALIZED -> null
        Lifecycle.State.CREATED -> Lifecycle.Event.ON_CREATE
        Lifecycle.State.STARTED -> Lifecycle.Event.ON_START
        Lifecycle.State.RESUMED -> Lifecycle.Event.ON_RESUME
    }

// TODO: Create a lifecycle module and make this public there
private fun Lifecycle.addObserver(callback: (observer: LifecycleEventObserver, source: LifecycleOwner, event: Lifecycle.Event) -> Unit) =
        addObserver(ReferenceHoldingLifecycleObserver(callback))

private class ReferenceHoldingLifecycleObserver(
        private val callBack: (observer: LifecycleEventObserver, source: LifecycleOwner, event: Lifecycle.Event) -> Unit
) : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) = callBack(this, source, event)
}