package com.tunjid.androidx.navigation

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AsyncNavigator (private val navigator: Navigator) {
    suspend fun pop() = suspendCoroutine<Fragment?> { continuation ->
        when (val previous = navigator.previous) {
            null -> continuation.resume(null)
            else -> {
                previous.doOnLifeCycleOnce(continuation.context, Lifecycle.Event.ON_START) { continuation.resume(previous) }
                navigator.pop()
            }
        }
    }

    suspend fun <T : Fragment> push(fragment: T) = suspendCoroutine<T?> { continuation ->
        when (navigator.current?.navigatorTag) {
            fragment.navigatorTag -> continuation.resume(null)
            else -> {
                fragment.doOnLifeCycleOnce(continuation.context, Lifecycle.Event.ON_START) { continuation.resume(fragment) }
                navigator.push(fragment)
            }
        }
    }
}