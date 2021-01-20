package com.tunjid.androidx.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.whenResumed

class SuspendingMultiStackNavigator(
    private val navigator: MultiStackNavigator
) : SuspendingNavigator by CommonSuspendingNavigator(navigator) {

    suspend fun show(index: Int): Fragment {
        navigator.stackFragments[navigator.activeIndex].whenResumed { navigator.show(index) }
        return navigator.stackFragments[index].waitForChild()
    }

    override suspend fun clear(upToTag: String?, includeMatch: Boolean) =
        SuspendingStackNavigator(navigator.activeNavigator).clear(upToTag, includeMatch)

    /**
     * @see MultiStackNavigator.clearAll
     */
    suspend fun clearAll() {
        navigator.activeFragment.whenResumed(doNothing)
        mainThreadSuspendCancellableCoroutine<Unit> { continuation ->
            navigator.reset(commitNow = false) { continuation.resumeIfActive(Unit) }
        }
        navigator.stackFragments[0].waitForChild()
    }

    private suspend fun StackFragment.waitForChild(): Fragment {
        whenResumed(doNothing)
        val current = navigator.current
        if (current != null) return current

        // Root has not been shown yet, defer until the first fragment shows
        return mainThreadSuspendCancellableCoroutine { continuation ->
            val fragment = navigator.current

            if (fragment != null) continuation.resumeIfActive(fragment)
            else navigator.fragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                    fm.unregisterFragmentLifecycleCallbacks(this)
                    continuation.resumeIfActive(f)
                }
            }, false)
        }
    }
}
