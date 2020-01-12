package com.tunjid.androidx.navigation

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.suspendCancellableCoroutine

class SuspendingStackNavigator(
        private val navigator: StackNavigator
) : AsyncNavigator by SuspendingNavigator(navigator) {

   override suspend fun clear(upToTag: String?, includeMatch: Boolean) = suspendCancellableCoroutine<Fragment?> { continuation ->
        val tag = upToTag ?: navigator.fragmentTags.firstOrNull() ?: ""
        val index = navigator.fragmentTags.indexOf(tag).let { if (includeMatch) it - 1 else it }
        val toShow = if (index < 0) null else navigator.find(navigator.fragmentTags[index] ?: "")

        navigator.clear(upToTag, includeMatch)
        toShow?.doOnLifeCycleOnce(Lifecycle.Event.ON_RESUME) { continuation.resumeIfActive(toShow) }
                ?: continuation.resumeIfActive(null)
    }
}