package com.tunjid.androidbootstrap.core.components

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.tunjid.androidbootstrap.core.R
import java.util.*

/**
 * A class that keeps track of the [fragments][Fragment] in an
 * [activity&#39;s][android.app.Activity] [FragmentManager]
 *
 *
 * Created by tj.dahunsi on 4/23/17.
 */

class FragmentStateManager @JvmOverloads constructor(
        internal val fragmentManager: FragmentManager,
        @param:IdRes @field:IdRes @get:IdRes val idResource: Int = R.id.main_fragment_container
) {

    internal val fragmentTags = mutableSetOf<String>()

    private var currentFragmentTag: String? = null

    /**
     * A class that keeps track of the fragments in the FragmentManager
     */
    private val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
            // Not a fragment managed by this FragmentStateManager
            if (f.id != idResource) return

            fragmentTags.add(f.tag ?: throw IllegalStateException(MSG_FRAGMENT_HAS_NO_TAG))

            val totalBackStackCount = fm.backStackEntryCount
            val numTrackedTags = fragmentTags.size

            val uniqueBackStackEntries = HashSet<String>()
            val backStackEntries = ArrayList<String>(totalBackStackCount)

            for (i in 0 until totalBackStackCount) {
                val entry = fm.getBackStackEntryAt(i)
                val entryName = entry.name
                val shownFragment = fragmentManager.findFragmentByTag(entryName)
                        ?: throw IllegalStateException(MSG_FRAGMENT_MISMATCH)

                // Not a fragment managed by us, continue
                if (shownFragment.id != idResource) continue

                entryName ?: throw IllegalStateException(MSG_FRAGMENT_HAS_NO_TAG)

                uniqueBackStackEntries.add(entryName)
                backStackEntries.add(entryName)
            }

            // Make sure every fragment shown managed by us is added to the back stack
            check(!(uniqueBackStackEntries.size != numTrackedTags && savedInstanceState == null)) {
                (MSG_FRAGMENT_NOT_ADDED_TO_BACKSTACK
                        + "\n Fragment Attached: " + f.toString()
                        + "\n Fragment Tag: " + f.tag
                        + "\n Number of Tracked Fragments: " + numTrackedTags
                        + "\n Backstack Entry Count: " + totalBackStackCount
                        + "\n Tracked Fragments: " + fragmentTags
                        + "\n Back Stack Entries: " + backStackEntries)
            }
        }

        override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
            // Not a fragment managed by this FragmentStateManager
            if (f.id != idResource) return
            requireNotNull(f.tag) {
                ("Fragment instance "
                        + f.javaClass.name
                        + " with no tag cannot be added to the back stack with " +
                        "a FragmentStateManager")
            }

            currentFragmentTag = f.tag
        }

        override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
            if (f.id == idResource) fragmentTags.remove(f.tag)
        }
    }

    /**
     * Gets the last fragment added to the [FragmentManager]
     */
    val currentFragment: Fragment?
        get() =
            if (currentFragmentTag == null) null
            else fragmentManager.findFragmentByTag(currentFragmentTag)

    init {
        val backStackCount = fragmentManager.backStackEntryCount

        // Restore previous back stack entries in the Fragment manager
        for (i in 0 until backStackCount) {
            fragmentTags.add(fragmentManager.getBackStackEntryAt(i).name
                    ?: throw IllegalStateException(MSG_FRAGMENT_HAS_NO_TAG))
        }

        fragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false)
    }

    /**
     * Attempts to show the fragment provided, if the fragment does not already exist in the
     * [FragmentManager] under the specified tag.
     *
     * @param transaction         The fragment transaction to show the supplied fragment with.
     * @param fragmentTagProvider A fragmentTagProvider provider specifying the
     * fragment and tag
     * @return true if the a fragment provided will be shown, false if the fragment instance already
     * exists and will be restored instead.
     */
    fun showFragment(
            transaction: FragmentTransaction,
            fragmentTagProvider: FragmentTagProvider
    ): Boolean = showFragment(fragmentTagProvider.fragment, fragmentTagProvider.stableTag, transaction)

    /**
     * Attempts to show the fragment provided, if the fragment does not already exist in the
     * [FragmentManager] under the specified tag.
     *
     * @param fragment    The fragment to show.
     * @param transaction The fragment transaction to show the supplied fragment with.
     * @param tag         the value to supply to this fragment for it's backstack entry name and tag
     * @return true if the a fragment provided will be shown, false if the fragment instance already
     * exists and will be restored instead.
     */
    @JvmOverloads
    fun showFragment(
            fragment: Fragment,
            tag: String,
            transaction: FragmentTransaction = fragmentManager.beginTransaction()
    ): Boolean {
        val fragmentShown: Boolean
        if (currentFragmentTag != null && currentFragmentTag == tag) return false

        val fragmentAlreadyExists = fragmentTags.contains(tag)

        fragmentShown = !fragmentAlreadyExists

        val fragmentToShow =
                (if (fragmentAlreadyExists) fragmentManager.findFragmentByTag(tag)
                else fragment) ?: throw NullPointerException(MSG_DODGY_FRAGMENT)

        transaction.addToBackStack(tag)
                .replace(idResource, fragmentToShow, tag)
                .commit()

        return fragmentShown
    }

    fun onSaveInstanceState(outState: Bundle) =
            outState.putString(CURRENT_FRAGMENT_KEY, currentFragmentTag)

    fun onRestoreInstanceState(savedState: Bundle?) {
        if (savedState != null) currentFragmentTag = savedState.getString(CURRENT_FRAGMENT_KEY)
    }

    /**
     * An interface to provide unique tags for [Fragments][Fragment]
     */

    interface FragmentTagProvider {
        val stableTag: String

        val fragment: Fragment
    }

    companion object {

        private const val CURRENT_FRAGMENT_KEY = "com.tunjid.androidbootstrap.core.components.FragmentStateManager.currentFragmentTag"
        private const val MSG_FRAGMENT_MISMATCH = "Fragment back stack entry name does not match a tag in the fragment manager"
        internal const val MSG_FRAGMENT_NOT_ADDED_TO_BACKSTACK = "A fragment cannot be added to a FragmentManager managed by FragmentStateManager without adding it to the back stack"
        internal const val MSG_FRAGMENT_HAS_NO_TAG = "A fragment cannot be added to a FragmentManager managed by FragmentStateManager without a Tag"
        private const val MSG_DODGY_FRAGMENT = "Tag exists in FragmentStateManager but not in FragmentManager"
    }
}
