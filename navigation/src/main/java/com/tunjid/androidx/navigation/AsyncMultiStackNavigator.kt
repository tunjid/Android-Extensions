package com.tunjid.androidx.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.isActive
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AsyncMultiStackNavigator(private val navigator: MultiStackNavigator) {
    suspend fun pop() = AsyncNavigator(navigator).pop()

    suspend fun <T : Fragment> push(fragment: T) = AsyncNavigator(navigator).push(fragment)

    suspend fun show(index: Int) = suspendCoroutine<Fragment?> { continuation ->
        when (navigator.activeIndex) {
            index -> continuation.resume(navigator.current)
            else -> {
                when (val upcomingStack = navigator.stackFragments.getOrNull(index)) {
                    null -> continuation.resume(null) // out of index. Throw an exception maybe?
                    else -> upcomingStack.waitForChild(continuation)
                }
                navigator.show(index)
            }
        }
    }

    suspend fun clear(upToTag: String? = null, includeMatch: Boolean = false) =
            AsyncStackNavigator(navigator.activeNavigator).clear(upToTag, includeMatch)

    suspend fun clearAll() = suspendCoroutine<Unit> { continuation ->
        // Clear all uses FragmentTransaction.commitNow, make sure calls start on the UI thread
        val first = navigator.stackFragments.first()
        first.view?.post {
            navigator.clearAll()

            // Root function will be invoked for newly added StackFragment, wait on it's child
            navigator.stackFragments[0].waitForChild(object : Continuation<Fragment?> {
                override val context: CoroutineContext = continuation.context
                override fun resumeWith(result: Result<Fragment?>) = continuation.resumeWith(
                        if (result.isSuccess) Result.success(Unit)
                        else Result.failure(Exception("Exception occurred when trying to clear all "))
                )
            })
        }
    }
}

private fun StackFragment.waitForChild(continuation: Continuation<Fragment?>) = doOnLifeCycleOnce(continuation.context, Lifecycle.Event.ON_START) {
    when (val current = navigator.current) {
        null -> { // Root has not been shown yet, defer until the first fragment shows
            val fragmentManager = navigator.fragmentManager
            fragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
                    if (continuation.context.isActive) continuation.resume(f)
                    fragmentManager.unregisterFragmentLifecycleCallbacks(this)
                }
            }, false)
        }
        else -> continuation.resume(current)
    }
}