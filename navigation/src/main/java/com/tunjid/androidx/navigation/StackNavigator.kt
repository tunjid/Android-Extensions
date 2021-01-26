package com.tunjid.androidx.navigation

import android.util.Log
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun Fragment.childStackNavigationController(@IdRes containerId: Int): Lazy<StackNavigator> = lazy {
    StackNavigator(childFragmentManager, containerId)
}

@Suppress("unused")
fun FragmentActivity.stackNavigationController(@IdRes containerId: Int): Lazy<StackNavigator> = lazy {
    StackNavigator(supportFragmentManager, containerId)
}

/**
 * A class that keeps track of the fragments [Fragment] in a [FragmentManager], and enforces that
 * they are added to the FragmentManager back stack with unique tags.
 *
 * It is best used for managing Fragments that are navigation destinations in either a [FragmentActivity]
 * or [Fragment].
 *
 * Created by tj.dahunsi on 4/23/17.
 */

class StackNavigator constructor(
    internal val fragmentManager: FragmentManager,
    @param:IdRes @field:IdRes @get:IdRes override val containerId: Int
) : Navigator {

    /**
     * Allows for the customization or augmentation of the [FragmentTransaction] that will show
     * the [Fragment] passed in to it
     */
    var transactionModifier: (FragmentTransaction.(Fragment) -> Unit)? = null

    /**
     * Gets the last fragment added to the [FragmentManager]
     */
    override val current: Fragment?
        get() = fragmentManager.findFragmentById(containerId)

    private val String.toEntry
        get() = "$containerId-$this"

    private val FragmentManager.BackStackEntry.inContainer: Boolean
        get() = name?.split("-")?.firstOrNull() == containerId.toString()

    private val FragmentManager.BackStackEntry.tag
        get() = name?.run { this.removePrefix(split("-").first() + "-") }

    private val baskStackEntries
        get() = fragmentManager.run { (0 until backStackEntryCount).map(this::getBackStackEntryAt).filter { it.inContainer } }

    internal val fragmentTags
        get() = baskStackEntries.map { it.tag }

    init {
        fragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentResumed(fm: FragmentManager, f: Fragment) = auditFragment(f)
        }, false)
    }

    /**
     * Attempts to show the fragment provided, retrieving it from the back stack
     * if an identical instance of it already exists in the [FragmentManager] under the specified
     * tag.
     *
     * @param fragment    The fragment to push.
     * @param tag         the value to supply to this fragment for it's backstack entry name and tag
     * It takes precedence over that supplied by the [transactionModifier]
     * @return true if the a fragment provided will be shown, false if the fragment instance already
     * exists and will be restored instead.
     */
    override fun push(fragment: Fragment, tag: String): Boolean {
        val tags = fragmentTags
        val currentFragmentTag = tags.lastOrNull()
        if (currentFragmentTag != null && currentFragmentTag == tag) return false

        val fragmentAlreadyExists = tags.contains(tag)

        val fragmentShown = !fragmentAlreadyExists

        val fragmentToShow =
            (if (fragmentAlreadyExists) fragmentManager.findFragmentByTag(tag)
            else fragment) ?: throw NullPointerException(MSG_DODGY_FRAGMENT)

        fragmentManager.commit {
            transactionModifier?.invoke(this, fragment)
            replace(containerId, fragmentToShow, tag)
            addToBackStack(tag.toEntry)
        }

        return fragmentShown
    }

    override val previous: Fragment?
        get() = fragmentTags.run {
            elementAtOrNull(lastIndex - 1).let(fragmentManager::findFragmentByTag)
        }

    override fun pop(): Boolean = fragmentTags.run {
        if (size > 1) clear(last(), true).let { true }
        else false
    }

    override fun clear(upToTag: String?, includeMatch: Boolean) {
        // Empty string will be treated as a no-op internally
        val tag = upToTag?.toEntry ?: baskStackEntries.firstOrNull()?.name ?: ""
        fragmentManager.popBackStack(tag, if (includeMatch) FragmentManager.POP_BACK_STACK_INCLUSIVE else 0)
    }

    override fun find(tag: String): Fragment? = fragmentManager.findFragmentByTag(tag)

    fun performConsecutively(scope: CoroutineScope, block: suspend SuspendingStackNavigator.() -> Unit) {
        scope.launch {
            block(SuspendingStackNavigator(this@StackNavigator))
        }
    }

    private fun auditFragment(fragment: Fragment) {
        if (fragment.id != containerId || fragment.view == null) return

        fragment.tag ?: throw IllegalStateException(MSG_FRAGMENT_HAS_NO_TAG)

        // Make sure every fragment created is added to the back stack
        if (!fragmentTags.contains(fragment.tag)) {
            Log.d("StackNavigator", MSG_FRAGMENT_NOT_ADDED_TO_BACKSTACK
                + "\n Fragment Attached: " + fragment.toString()
                + "\n Fragment Tag: " + fragment.tag
                + "\n Backstack Entry Count: " + baskStackEntries.size
                + "\n Tracked Fragments: " + fragmentTags)
        }
    }

    companion object {
        internal const val MSG_FRAGMENT_NOT_ADDED_TO_BACKSTACK = "A fragment cannot be added to a FragmentManager managed by StackNavigator without adding it to the back stack"
        internal const val MSG_FRAGMENT_HAS_NO_TAG = "A fragment cannot be added to a FragmentManager managed by StackNavigator without a Tag"
        private const val MSG_DODGY_FRAGMENT = "Tag exists in StackNavigator but not in FragmentManager"
    }
}
