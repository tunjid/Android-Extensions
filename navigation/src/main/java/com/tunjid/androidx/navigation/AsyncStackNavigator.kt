package com.tunjid.androidx.navigation

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AsyncStackNavigator(private val navigator: StackNavigator) {
    suspend fun pop() = AsyncNavigator(navigator).pop()

    suspend fun <T : Fragment> push(fragment: T) = AsyncNavigator(navigator).push(fragment)

    suspend fun clear(upToTag: String? = null, includeMatch: Boolean = false) = suspendCoroutine<Fragment?> { continuation ->
        val tag = upToTag ?: navigator.fragmentTags.firstOrNull() ?: ""
        val index = navigator.fragmentTags.indexOf(tag).let { if (includeMatch) it - 1 else it }
        val toShow = if (index < 0) null else navigator.find(navigator.fragmentTags[index] ?: "")

        toShow?.doOnLifeCycleOnce(continuation.context, Lifecycle.Event.ON_START) { continuation.resume(toShow) }
                ?: continuation.resume(null)

        navigator.clear(upToTag, includeMatch)
    }

}