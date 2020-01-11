package com.tunjid.androidx.navigation

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AsyncNavigator(private val navigator: Navigator) {
    suspend fun pop() = suspendCancellableCoroutine<Fragment?> { continuation ->
        when (val previous = navigator.previous) {
            null -> continuation.resumeIfActive(null)
            else -> {
                previous.doOnLifeCycleOnce(Lifecycle.Event.ON_START) { continuation.resumeIfActive(previous) }
                navigator.pop()
            }
        }
    }

    suspend fun <T : Fragment> push(fragment: T) = suspendCancellableCoroutine<T?> { continuation ->
        when (navigator.current?.navigatorTag) {
            fragment.navigatorTag -> continuation.resumeIfActive(null)
            else -> {
                fragment.doOnLifeCycleOnce(Lifecycle.Event.ON_START) { continuation.resumeIfActive(fragment) }
                navigator.push(fragment)
            }
        }
    }
}

fun <T> CancellableContinuation<T>.resumeIfActive(item: T) {
    if (isActive) resume(item)
}