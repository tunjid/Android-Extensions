package com.tunjid.androidx.navigation

import androidx.fragment.app.Fragment
import androidx.lifecycle.whenResumed
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/**
 * A stateless class that keeps [SuspendingStackNavigator] and [SuspendingMultiStackNavigator] DRY
 */
internal class CommonSuspendingNavigator(private val navigator: Navigator) : SuspendingNavigator {
    override val containerId: Int get() = navigator.containerId

    override val current: Fragment? get() = navigator.current

    override val previous: Fragment? get() = navigator.previous

    override suspend fun find(tag: String): Fragment? = navigator.find(tag)

    override suspend fun pop(): Fragment? {
        val current = navigator.current ?: return null
        current.whenResumed(doNothing)

        return when (val previous = navigator.previous) {
            null -> null
            else -> {
                navigator.pop()
                previous.whenResumed { previous }
            }
        }
    }

    override suspend fun <T : Fragment> push(fragment: T, tag: String): T? {
        val current = navigator.current

        if (current == null) {
            navigator.push(fragment, tag)
            return fragment.whenResumed { fragment }
        }

        if (current.tag == tag) return null

        current.whenResumed { navigator.push(fragment, tag) }
        return fragment.whenResumed { fragment }
    }

    override suspend fun clear(upToTag: String?, includeMatch: Boolean): Fragment? =
        throw IllegalArgumentException("Override this")
}

internal suspend inline fun <T> mainThreadSuspendCancellableCoroutine(
    crossinline block: (CancellableContinuation<T>) -> Unit
): T = withContext(Dispatchers.Main) { suspendCancellableCoroutine(block) }

internal fun <T> CancellableContinuation<T>.resumeIfActive(item: T) {
    if (isActive) resume(item)
}

internal val doNothing: suspend CoroutineScope.() -> Unit get() = {}