package com.tunjid.androidbootstrap.core.components

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import java.util.*

fun Fragment.childFragmentManagerNavigator(@IdRes containerId: Int): Lazy<FragmentStackNavigator> =
        lazy { FragmentStackNavigator(this, this, childFragmentManager, containerId) }

@Suppress("unused")
fun FragmentActivity.fragmentManagerNavigator(@IdRes containerId: Int): Lazy<FragmentStackNavigator> =
        lazy { FragmentStackNavigator(this, this, supportFragmentManager, containerId) }

/**
 * A class that keeps track of the fragments [Fragment] in a [FragmentManager], and enforces that
 * they are added to the FragmentManager back stack with unique tags.
 *
 * It is best used for managing Fragments that are navigation destinations in either a [FragmentActivity]
 * or [Fragment].
 *
 * Created by tj.dahunsi on 4/23/17.
 */

class FragmentStackNavigator constructor(
        lifecycleOwner: LifecycleOwner,
        private val savedStateRegistryOwner: SavedStateRegistryOwner,
        internal val fragmentManager: FragmentManager,
        @param:IdRes @field:IdRes @get:IdRes val containerId: Int
) : LifecycleEventObserver, SavedStateRegistry.SavedStateProvider {

    internal val fragmentTags = mutableSetOf<String>()

    private val savedStateKey: String
        get() = "$CURRENT_FRAGMENT_KEY-$containerId"

    private val savedState: Bundle

    /**
     * Gets the last fragment added to the [FragmentManager]
     */
    val currentFragment: Fragment?
        get() =
            if (currentFragmentTag == null) null
            else fragmentManager.findFragmentByTag(currentFragmentTag)

    private var currentFragmentTag: String? = null
        set(value) {
            field = value
            savedState.putString(CURRENT_FRAGMENT_KEY, value)
        }

    /**
     * A class that keeps track of the fragments in the FragmentManager
     */
    private val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) =
                this@FragmentStackNavigator.onFragmentCreated(fm, f, savedInstanceState)

        override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) =
                this@FragmentStackNavigator.onFragmentViewCreated(f)

        override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) =
                this@FragmentStackNavigator.onFragmentDestroyed(f)
    }

    init {
        lifecycleOwner.lifecycle.addObserver(this)

        savedStateRegistryOwner.savedStateRegistry.apply {
            savedState = consumeRestoredStateForKey(savedStateKey) ?: Bundle()
            registerSavedStateProvider(savedStateKey, this@FragmentStackNavigator)
        }

        currentFragmentTag = savedState.getString(CURRENT_FRAGMENT_KEY)

        // Restore previous back stack entries in the Fragment manager
        for (i in 0 until fragmentManager.backStackEntryCount) fragmentTags.add(fragmentManager.getBackStackEntryAt(i).name
                ?: throw IllegalStateException(MSG_FRAGMENT_HAS_NO_TAG))

        fragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) = when (event) {
        Lifecycle.Event.ON_DESTROY -> {
            source.lifecycle.removeObserver(this)
            savedStateRegistryOwner.savedStateRegistry.unregisterSavedStateProvider(savedStateKey)
        }
        else -> Unit
    }

    override fun saveState(): Bundle = savedState

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
                .replace(containerId, fragmentToShow, tag)
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
    fun <T> show(fragment: T, transaction: FragmentTransaction? = null) where T : Fragment, T : FragmentTagProvider =
            show(fragment, fragment.stableTag, transaction)

    private fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        // Not a fragment managed by this FragmentStackNavigator
        if (f.id != containerId) return

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
            if (shownFragment.id != containerId) continue

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

    private fun onFragmentViewCreated(f: Fragment) {
        // Not a fragment managed by this FragmentStackNavigator
        if (f.id != containerId) return
        requireNotNull(f.tag) {
            ("Fragment instance "
                    + f.javaClass.name
                    + " with no tag cannot be added to the back stack with " +
                    "a FragmentStackNavigator")
        }

        currentFragmentTag = f.tag
    }

    private fun onFragmentDestroyed(f: Fragment) {
        if (f.id == containerId) fragmentTags.remove(f.tag)
    }

    /**
     * An interface to provide unique tags for [Fragment]. Fragment implementers typically delegate
     * this to a hash string of their arguments.
     *
     * It's convenient to let  Fragments implement this interface, among with [FragmentTransactionProvider].
     */

    interface FragmentTagProvider {
        val stableTag: String
    }

    /**
     * An interface for delegating the provision  of a [FragmentTransaction] that will show
     * the passed in Fragment. Implementers typically configure mappings for
     * shared element transitions, or other kinds of animations.
     *
     * It's convenient to let  Fragments implement this interface, among with [FragmentTagProvider].
     */
    interface FragmentTransactionProvider {
        fun provideFragmentTransaction(fragmentTo: Fragment): FragmentTransaction?
    }

    companion object {

        private const val CURRENT_FRAGMENT_KEY = "com.tunjid.androidbootstrap.core.components.FragmentStackNavigator.currentFragmentTag"
        private const val MSG_FRAGMENT_MISMATCH = "Fragment back stack entry name does not match a tag in the fragment manager"
        internal const val MSG_FRAGMENT_NOT_ADDED_TO_BACKSTACK = "A fragment cannot be added to a FragmentManager managed by FragmentStackNavigator without adding it to the back stack"
        internal const val MSG_FRAGMENT_HAS_NO_TAG = "A fragment cannot be added to a FragmentManager managed by FragmentStackNavigator without a Tag"
        private const val MSG_DODGY_FRAGMENT = "Tag exists in FragmentStackNavigator but not in FragmentManager"
    }
}
