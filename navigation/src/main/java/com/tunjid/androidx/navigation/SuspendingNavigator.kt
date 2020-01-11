package com.tunjid.androidx.navigation

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

interface AsyncNavigator {
    suspend fun pop(): Fragment?
    suspend fun clear(upToTag: String? = null, includeMatch: Boolean = false): Fragment?
    suspend fun <T : Fragment> push(fragment: T, tag: String): T?
    suspend fun <T : Fragment> push(fragment: T): T? = push(fragment, fragment.navigatorTag)
}

internal class SuspendingNavigator(private val navigator: Navigator) : AsyncNavigator {
    override suspend fun pop() = suspendCancellableCoroutine<Fragment?> { continuation ->
        when (val previous = navigator.previous) {
            null -> continuation.resumeIfActive(null)
            else -> {
                previous.doOnLifeCycleOnce(Lifecycle.Event.ON_RESUME) { continuation.resumeIfActive(previous) }
                navigator.pop()
            }
        }
    }

    override suspend fun <T : Fragment> push(fragment: T, tag: String) = suspendCancellableCoroutine<T?> { continuation ->
        when (navigator.current?.tag) {
            tag -> continuation.resumeIfActive(null)
            else -> {
                fragment.doOnLifeCycleOnce(Lifecycle.Event.ON_RESUME) { continuation.resumeIfActive(fragment) }
                navigator.push(fragment, tag)
            }
        }
    }

    override suspend fun clear(upToTag: String?, includeMatch: Boolean): Fragment? =
            throw IllegalArgumentException("Override this")
}

fun <T> CancellableContinuation<T>.resumeIfActive(item: T) {
    if (isActive) resume(item)
}