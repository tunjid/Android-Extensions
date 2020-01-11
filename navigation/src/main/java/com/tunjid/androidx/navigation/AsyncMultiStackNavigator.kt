package com.tunjid.androidx.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

class AsyncMultiStackNavigator(private val navigator: MultiStackNavigator) {
    suspend fun pop() = AsyncNavigator(navigator).pop()

    suspend fun <T : Fragment> push(fragment: T) = AsyncNavigator(navigator).push(fragment)

    suspend fun show(index: Int) = suspendCancellableCoroutine<Fragment?> { continuation ->
        navigator.stackFragments[navigator.activeIndex].doOnLifeCycleOnce(Lifecycle.Event.ON_RESUME) {
            when (navigator.activeIndex) {
                index -> continuation.resumeIfActive(navigator.current)
                else -> {
                    when (val upcomingStack = navigator.stackFragments.getOrNull(index)) {
                        null -> continuation.resumeIfActive(null)  // out of index. Throw an exception maybe?
                        else -> upcomingStack.waitForChild(continuation)
                    }
                    navigator.show(index)
                }
            }
        }
    }

    suspend fun clear(upToTag: String? = null, includeMatch: Boolean = false) =
            AsyncStackNavigator(navigator.activeNavigator).clear(upToTag, includeMatch)

    suspend fun clearAll() {
        internalClearAll()
    }

    private suspend fun internalClearAll(): Fragment? = suspendCancellableCoroutine { continuation ->
        // Clear all uses FragmentTransaction.commitNow, make sure calls start on the UI thread
        val first = navigator.stackFragments.first()
        first.view?.post {
            navigator.clearAll()

            // Root function will be invoked for newly added StackFragment, wait on it's child
            navigator.stackFragments[0].waitForChild(continuation)
        }
    }
}

private fun StackFragment.waitForChild(continuation: CancellableContinuation<Fragment?>) = doOnLifeCycleOnce(Lifecycle.Event.ON_RESUME) {
    when (val current = navigator.current) {
        null -> { // Root has not been shown yet, defer until the first fragment shows
            val fragmentManager = navigator.fragmentManager
            fragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                    continuation.resumeIfActive(f)
                    fragmentManager.unregisterFragmentLifecycleCallbacks(this)
                }
            }, false)
        }
        else -> continuation.resumeIfActive(current)
    }
}