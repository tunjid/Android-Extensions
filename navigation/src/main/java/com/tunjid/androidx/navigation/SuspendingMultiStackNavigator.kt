package com.tunjid.androidx.navigation

import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

class SuspendingMultiStackNavigator(
        private val navigator: MultiStackNavigator
) : SuspendingNavigator by CommonSuspendingNavigator(navigator) {

    suspend fun show(index: Int) = suspendCancellableCoroutine<Fragment?> { continuation ->
        navigator.stackFragments[navigator.activeIndex].doOnLifecycleEvent(Lifecycle.Event.ON_RESUME) {
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

    override suspend fun clear(upToTag: String?, includeMatch: Boolean) =
            SuspendingStackNavigator(navigator.activeNavigator).clear(upToTag, includeMatch)

    /**
     * @see MultiStackNavigator.clearAll
     */
    suspend fun clearAll() {
        internalClearAll()
    }

    private suspend fun internalClearAll(): Fragment? = suspendCancellableCoroutine { continuation ->
        // Clear all uses FragmentTransaction.commitNow, make sure calls start on the UI thread
        Handler(Looper.getMainLooper()).post {
            navigator.clearAll()

            // Root function will be invoked for newly added StackFragment, wait on it's child
            navigator.stackFragments[0].waitForChild(continuation)
        }
    }
}

private fun StackFragment.waitForChild(continuation: CancellableContinuation<Fragment?>) = doOnLifecycleEvent(Lifecycle.Event.ON_RESUME) {
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