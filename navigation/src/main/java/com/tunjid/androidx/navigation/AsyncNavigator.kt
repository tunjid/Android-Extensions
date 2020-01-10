package com.tunjid.androidx.navigation

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AsyncNavigator(private val navigator: Navigator) {
    suspend fun pop() = suspendCancellableCoroutine<Fragment?> { continuation ->
        when (val previous = navigator.previous) {
            null -> continuation.ifActive { resume(null) }
            else -> {
                previous.doOnLifeCycleOnce(Lifecycle.Event.ON_START) { continuation.ifActive { resume(previous) } }
                navigator.pop()
            }
        }
    }

    suspend fun <T : Fragment> push(fragment: T) = suspendCancellableCoroutine<T?> { continuation ->
        when (navigator.current?.navigatorTag) {
            fragment.navigatorTag -> continuation.ifActive { resume(null) }
            else -> {
                fragment.doOnLifeCycleOnce(Lifecycle.Event.ON_START) { continuation.ifActive { resume(fragment) } }
                navigator.push(fragment)
            }
        }
    }
}

fun <T> CancellableContinuation<T>.ifActive(action: CancellableContinuation<T>.() -> Unit) {
    if (isActive) action(this)
}