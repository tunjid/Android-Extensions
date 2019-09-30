package com.tunjid.androidx.navigation

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.tunjid.androidx.savedstate.LifecycleSavedStateContainer
import com.tunjid.androidx.savedstate.savedStateFor
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

const val STACK_NAVIGATOR = "com.tunjid.androidx.navigation.StackNavigator"

/**
 * Convenience method for [Fragment] delegation to a [FragmentActivity] when implementing
 * [Navigator.NavigationController]
 */
fun Fragment.activityNavigationController() = object : ReadOnlyProperty<Fragment, Navigator> {
    override operator fun getValue(thisRef: Fragment, property: KProperty<*>): Navigator =
            (activity as? Navigator.NavigationController)?.navigator
                    ?: throw IllegalStateException("The hosting Activity is not a NavigationController")
}

fun Fragment.childStackNavigationController(@IdRes containerId: Int): Lazy<StackNavigator> = lazy {
    StackNavigator(
            savedStateFor(this, "$STACK_NAVIGATOR-$containerId"),
            childFragmentManager,
            containerId
    )
}

@Suppress("unused")
fun FragmentActivity.stackNavigationController(@IdRes containerId: Int): Lazy<StackNavigator> = lazy {
    StackNavigator(
            savedStateFor(this, "$STACK_NAVIGATOR-$containerId"),
            supportFragmentManager,
            containerId
    )
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
        private val stateContainer: LifecycleSavedStateContainer,
        internal val fragmentManager: FragmentManager,
        @param:IdRes @field:IdRes @get:IdRes override val containerId: Int
) : Navigator {

    internal val fragmentTags = linkedSetOf<String>()

    /**
     * Allows for the customization or augmentation of the [FragmentTransaction] that will show
     * the [Fragment] passed in to it
     */
    var transactionModifier: (FragmentTransaction.(Fragment) -> Unit)? = null

    /**
     * Gets the last fragment added to the [FragmentManager]
     */
    override val currentFragment: Fragment?
        get() = fragmentManager.findFragmentById(containerId)

    private var currentFragmentTag: String? = null
        set(value) {
            field = value
            stateContainer.savedState.putString(CURRENT_FRAGMENT_KEY, value)
        }

    /**
     * A class that keeps track of the fragments in the FragmentManager
     */
    private val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) =
                this@StackNavigator.onFragmentCreated(fm, f, savedInstanceState)

        override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) =
                this@StackNavigator.onFragmentViewCreated(f)

        override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) =
                this@StackNavigator.onFragmentDestroyed(f)
    }

    init {
        currentFragmentTag = stateContainer.savedState.getString(CURRENT_FRAGMENT_KEY)

        // Restore previous back stack entries in the Fragment manager
        for (i in 0 until fragmentManager.backStackEntryCount) fragmentTags.add(fragmentManager.getBackStackEntryAt(i).name
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
     * It takes precedence over that supplied by the [transactionModifier]
     * @return true if the a fragment provided will be shown, false if the fragment instance already
     * exists and will be restored instead.
     */
    override fun show(fragment: Fragment, tag: String): Boolean {
        val fragmentShown: Boolean
        if (currentFragmentTag != null && currentFragmentTag == tag) return false

        val fragmentAlreadyExists = fragmentTags.contains(tag)

        fragmentShown = !fragmentAlreadyExists

        val fragmentToShow =
                (if (fragmentAlreadyExists) fragmentManager.findFragmentByTag(tag)
                else fragment) ?: throw NullPointerException(MSG_DODGY_FRAGMENT)

        fragmentManager.beginTransaction().apply { transactionModifier?.invoke(this, fragment) }
                .addToBackStack(tag)
                .replace(containerId, fragmentToShow, tag)
                .commit()

        return fragmentShown
    }

    override fun pop(): Boolean =
            if (fragmentManager.backStackEntryCount > 1) fragmentManager.popBackStack().let { true }
            else false

    override fun clear(upToTag: String?, includeMatch: Boolean) {
        val tag = upToTag ?: fragmentTags.firstOrNull() ?: "" // Empty string will be treated as a no-op internally
        fragmentManager.popBackStack(tag, if (includeMatch) FragmentManager.POP_BACK_STACK_INCLUSIVE else 0)
    }

    private fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        // Not a fragment managed by this StackNavigator
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
        // Not a fragment managed by this StackNavigator
        if (f.id != containerId) return
        requireNotNull(f.tag) {
            ("Fragment instance "
                    + f.javaClass.name
                    + " with no tag cannot be added to the back stack with " +
                    "a StackNavigator")
        }

        currentFragmentTag = f.tag
    }

    private fun onFragmentDestroyed(f: Fragment) {
        if (f.id == containerId) fragmentTags.remove(f.tag)
    }

    companion object {

        private const val CURRENT_FRAGMENT_KEY = "com.tunjid.androidx.navigation.StackNavigator.currentFragmentTag"
        private const val MSG_FRAGMENT_MISMATCH = "Fragment back stack entry name does not match a tag in the fragment manager"
        internal const val MSG_FRAGMENT_NOT_ADDED_TO_BACKSTACK = "A fragment cannot be added to a FragmentManager managed by StackNavigator without adding it to the back stack"
        internal const val MSG_FRAGMENT_HAS_NO_TAG = "A fragment cannot be added to a FragmentManager managed by StackNavigator without a Tag"
        private const val MSG_DODGY_FRAGMENT = "Tag exists in StackNavigator but not in FragmentManager"
    }
}
