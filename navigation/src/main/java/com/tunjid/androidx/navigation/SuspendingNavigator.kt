package com.tunjid.androidx.navigation

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle

/**
 * A like for like API of a [Navigator] that returns suspending equivalents of the original
 * functions that complete when the navigation action has finished. This typically means
 * the [Fragment] navigated to is in the [Lifecycle.State.RESUMED] state
 */
interface SuspendingNavigator {
    /**
     * @see Navigator.containerId
     */
    @get:IdRes
    val containerId: Int

    /**
     * @see Navigator.current
     */
    val current: Fragment?

    /**
     * @see Navigator.previous
     */
    val previous: Fragment?

    /**
     * @see Navigator.find
     */
    suspend fun find(tag: String): Fragment?

    /**
     * @see Navigator.pop
     */
    suspend fun pop(): Fragment?

    /**
     * @see Navigator.clear
     */
    suspend fun clear(upToTag: String? = null, includeMatch: Boolean = false): Fragment?

    /**
     * @see Navigator.push
     */
    suspend fun <T : Fragment> push(fragment: T, tag: String): T?

    /**
     * @see Navigator.push
     */
    suspend fun <T : Fragment> push(fragment: T): T? = push(fragment, fragment.navigatorTag)
}