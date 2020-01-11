package com.tunjid.androidx.navigation

import androidx.fragment.app.Fragment

interface AsyncNavigator {
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