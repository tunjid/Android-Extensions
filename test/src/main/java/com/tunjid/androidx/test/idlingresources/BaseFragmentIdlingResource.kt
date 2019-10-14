package com.tunjid.androidx.test.idlingresources

import androidx.fragment.app.FragmentManager
import androidx.test.espresso.IdlingResource

/**
 * [IdlingResource] for fragments with a tag.
 *
 * Created by tj.dahunsi on 4/29/17.
 */
abstract class BaseFragmentIdlingResource(
        fragmentManager: FragmentManager,
        private var fragmentTag: String) : FragmentManager.FragmentLifecycleCallbacks(), IdlingResource {

    protected var idle = false
        set(value) {
            field = value
            if (value) resourceCallback?.onTransitionToIdle()
        }

    private var resourceCallback: IdlingResource.ResourceCallback? = null

    init {
        @Suppress("LeakingThis")
        fragmentManager.registerFragmentLifecycleCallbacks(this, false)
    }

    override fun isIdleNow(): Boolean = idle

    override fun getName(): String = javaClass.simpleName + ": " + fragmentTag

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.resourceCallback = callback
    }
}
