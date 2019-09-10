package com.tunjid.androidbootstrap.core.components

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.fragment.app.*
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import java.util.*

@Suppress("unused")
fun Fragment.childFragmentStateViewModelFactory(@IdRes idResource: Int) =
        viewModels<FragmentStateViewModel> { FragmentStateViewModelFactory(childFragmentManager, idResource, this) }

fun FragmentActivity.fragmentStateViewModelFactory(@IdRes idResource: Int) =
        viewModels<FragmentStateViewModel> { FragmentStateViewModelFactory(supportFragmentManager, idResource, this) }

@Suppress("UNCHECKED_CAST")
private class FragmentStateViewModelFactory(
        private val fragmentManager: FragmentManager,
        @param:IdRes @field:IdRes @get:IdRes val idResource: Int,
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
    ): T = FragmentStateViewModel(handle, fragmentManager, idResource) as T
}

/**
 * A class that keeps track of the [fragments][Fragment] in a
 * [FragmentManager], and enforces that they are added to the back stack.
 *
 * It is best used for managing Fragments that are navigation destinations in either a [FragmentActivity]
 * or [Fragment].
 *
 * Created by tj.dahunsi on 4/23/17.
 */

class FragmentStateViewModel constructor(
        private val state: SavedStateHandle,
        internal val fragmentManager: FragmentManager,
        @param:IdRes @field:IdRes @get:IdRes val idResource: Int
) : ViewModel() {

    internal val fragmentTags = mutableSetOf<String>()

    private val key: String
        get() = "$CURRENT_FRAGMENT_KEY-$idResource"

    /**
     * Gets the last fragment added to the [FragmentManager]
     */
    val currentFragment: Fragment?
        get() =
            if (currentFragmentTag == null) null
            else fragmentManager.findFragmentByTag(currentFragmentTag)

    private var currentFragmentTag: String? = state[key]
        set(value) {
            field = value
            state[key] = value
        }

    /**
     * A class that keeps track of the fragments in the FragmentManager
     */
    private val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
            // Not a fragment managed by this FragmentStateViewModel
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
            // Not a fragment managed by this FragmentStateViewModel
            if (f.id != idResource) return
            requireNotNull(f.tag) {
                ("Fragment instance "
                        + f.javaClass.name
                        + " with no tag cannot be added to the back stack with " +
                        "a FragmentStateViewModel")
            }

            currentFragmentTag = f.tag
        }

        override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
            if (f.id == idResource) fragmentTags.remove(f.tag)
        }
    }

    init {
        val backStackCount = fragmentManager.backStackEntryCount

        // Restore previous back stack entries in the Fragment manager
        for (i in 0 until backStackCount) fragmentTags.add(fragmentManager.getBackStackEntryAt(i).name
                ?: throw IllegalStateException(MSG_FRAGMENT_HAS_NO_TAG))

        fragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false)
    }

    /**
     * Attempts to show the fragment provided, retrieving it from the back stack
     * if an identical instance of it already exists in the [FragmentManager] under the specified
     * tag.
     *
     * @param fragment    The fragment to show.
     * @param tag         the value to supply to this fragment for it's backstack entry name and tag
     * @param transaction The fragment transaction to show the supplied fragment with.
     * @return true if the a fragment provided will be shown, false if the fragment instance already
     * exists and will be restored instead.
     */
    @JvmOverloads
    fun show(
            fragment: Fragment,
            tag: String,
            transaction: FragmentTransaction? = null
    ): Boolean {
        val fragmentShown: Boolean
        if (currentFragmentTag != null && currentFragmentTag == tag) return false

        val fragmentAlreadyExists = fragmentTags.contains(tag)

        fragmentShown = !fragmentAlreadyExists

        val fragmentToShow =
                (if (fragmentAlreadyExists) fragmentManager.findFragmentByTag(tag)
                else fragment) ?: throw NullPointerException(MSG_DODGY_FRAGMENT)

        (transaction ?: fragmentManager.beginTransaction()).addToBackStack(tag)
                .replace(idResource, fragmentToShow, tag)
                .commit()

        return fragmentShown
    }

    /**
     * Attempts to show the fragment provided, retrieving it from the back stack
     * if an identical instance of it already exists in the [FragmentManager] under the specified
     * tag.
     *
     * @see show
     */
    @JvmOverloads
    fun <T> show(item: T, transaction: FragmentTransaction? = null) where T : Fragment, T : FragmentTagProvider =
            show(item, item.stableTag, transaction)

    /**
     * An interface to provide unique tags for [Fragment]. Fragment implementers typically delegate
     * this to a hash string of their arguments.
     */

    interface FragmentTagProvider {
        val stableTag: String
    }

    companion object {

        private const val CURRENT_FRAGMENT_KEY = "com.tunjid.androidbootstrap.core.components.FragmentStateViewModel.currentFragmentTag"
        private const val MSG_FRAGMENT_MISMATCH = "Fragment back stack entry name does not match a tag in the fragment manager"
        internal const val MSG_FRAGMENT_NOT_ADDED_TO_BACKSTACK = "A fragment cannot be added to a FragmentManager managed by FragmentStateViewModel without adding it to the back stack"
        internal const val MSG_FRAGMENT_HAS_NO_TAG = "A fragment cannot be added to a FragmentManager managed by FragmentStateViewModel without a Tag"
        private const val MSG_DODGY_FRAGMENT = "Tag exists in FragmentStateViewModel but not in FragmentManager"
    }
}
