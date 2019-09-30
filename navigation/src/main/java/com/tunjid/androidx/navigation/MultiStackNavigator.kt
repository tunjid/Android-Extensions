package com.tunjid.androidx.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.lifecycle.Lifecycle
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.savedstate.LifecycleSavedStateContainer
import com.tunjid.androidx.savedstate.savedStateFor
import java.util.*
import kotlin.collections.set

const val MULTI_STACK_NAVIGATOR = "com.tunjid.androidx.navigation.MultiStackNavigator"

fun Fragment.childMultiStackNavigationController(
        @IdRes containerId: Int,
        stackIds: IntArray,
        rootFunction: (Int) -> Pair<Fragment, String>
): Lazy<MultiStackNavigator> = lazy {
    MultiStackNavigator(
            savedStateFor(this@childMultiStackNavigationController, "$MULTI_STACK_NAVIGATOR-$containerId"),
            childFragmentManager,
            stackIds,
            containerId, rootFunction
    )
}

fun FragmentActivity.multiStackNavigationController(
        @IdRes containerId: Int,
        stackIds: IntArray,
        rootFunction: (Int) -> Pair<Fragment, String>
): Lazy<MultiStackNavigator> = lazy {
    MultiStackNavigator(
            savedStateFor(this@multiStackNavigationController, "$MULTI_STACK_NAVIGATOR-$containerId"),
            supportFragmentManager,
            stackIds,
            containerId,
            rootFunction
    )
}

/**
 * Manages navigation for independent stacks of [Fragment]s, where each stack is managed by a
 * [StackNavigator].
 */
class MultiStackNavigator(
        private val stateContainer: LifecycleSavedStateContainer,
        private val fragmentManager: FragmentManager,
        private val stackIds: IntArray,
        @IdRes override val containerId: Int,
        private val rootFunction: (Int) -> Pair<Fragment, String>) : Navigator {

    /**
     * A callback that will be invoked when a stack is selected, either by the user selecting it,
     * or from popping another stack off.
     */
    var stackSelectedListener: ((Int) -> Unit)? = null

    /**
     * Allows for the customization or augmentation of the [FragmentTransaction] that switches
     * from one active stack to another
     */
    var stackTransactionModifier: (FragmentTransaction.(Int) -> Unit)? = null

    /**
     * Allows for the customization or augmentation of the [FragmentTransaction] that will show
     * a [Fragment] inside the stack in focus
     */
    var transactionModifier: (FragmentTransaction.(Fragment) -> Unit)? = null
        set(value) {
            field = value
            stackMap.values
                    .filter { it.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) }
                    .forEach { it.navigator.transactionModifier = value }
        }

    private val navStack: Stack<StackFragment> = Stack()
    private val stackMap = mutableMapOf<Int, StackFragment>()

    private val activeStack: StackFragment
        get() = stackMap.values.run { firstOrNull(Fragment::isVisible) ?: first() }

    val activeNavigator
        get() = activeStack.navigator

    override val currentFragment: Fragment?
        get() = activeNavigator.currentFragment

    init {
        fragmentManager.registerFragmentLifecycleCallbacks(StackLifecycleCallback(), false)
        fragmentManager.addOnBackStackChangedListener { throw IllegalStateException("Fragments may not be added to the back stack of a FragmentManager managed by a MultiStackNavigator") }

        if (stateContainer.isFreshState) fragmentManager.commitNow {
            stackIds.forEachIndexed { index, id -> add(containerId, StackFragment.newInstance(id), index.toString()) }
        }
        else fragmentManager.fragments.filterIsInstance(StackFragment::class.java).forEachIndexed { index, stackFragment ->
            navStack.push(stackFragment)
            stackMap[stackFragment.stackId] = stackFragment
            if (index == stackIds.lastIndex) stateContainer.savedState.getIntArray(NAV_STACK_ORDER)?.apply {
                navStack.sortBy { indexOf(it.stackId) }
            }
        }
    }

    fun show(@IdRes toShow: Int) = showInternal(toShow, true)

    fun navigatorAt(index: Int) = stackMap[stackIds[index]]?.navigator

    /**
     * Pops the current fragment off the stack in focus. If The current
     * Fragment is the only Fragment on it's stack, the stack that was active before the current
     * stack is switched to.
     *
     * @see [StackNavigator.pop]
     */
    override fun pop(): Boolean = when {
        activeStack.navigator.pop() -> true
        navStack.run { remove(activeStack); isEmpty() } -> false
        else -> showInternal(navStack.peek().stackId, false).let { true }
    }

    override fun clear(upToTag: String?, includeMatch: Boolean) = activeNavigator.clear(upToTag, includeMatch)

    override fun show(fragment: Fragment, tag: String): Boolean = activeNavigator.show(fragment, tag)

    private fun showInternal(@IdRes toShow: Int, addTap: Boolean) = fragmentManager.commit {
        stackTransactionModifier?.invoke(this, toShow)

        for ((id, fragment) in stackMap) when {
            id == toShow && !fragment.isHidden -> return@commit
            id == toShow && fragment.isHidden -> show(fragment).also { if (addTap) track(fragment) }
            else -> hide(fragment)
        }

        runOnCommit { stackSelectedListener?.invoke(toShow) }
    }

    private fun track(tab: StackFragment) {
        if (navStack.contains(tab)) navStack.remove(tab)
        navStack.push(tab)
        stateContainer.savedState.putIntArray(NAV_STACK_ORDER, navStack.map(StackFragment::stackId).toIntArray())
    }

    private inner class StackLifecycleCallback : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentCreated(fm: FragmentManager, fragment: Fragment, savedInstanceState: Bundle?) {
            if (fragment.id != containerId) return
            check(fragment is StackFragment) { "Only Stack Fragments may be added to a container View managed by a MultiStackNavigator" }

            stackMap[fragment.stackId] = fragment

            if (!stateContainer.isFreshState) return

            if (stackIds.indexOf(fragment.stackId) == 0) track(fragment)
            else fm.beginTransaction().hide(fragment).commit()
        }

        override fun onFragmentViewCreated(fm: FragmentManager, fragment: Fragment, view: View, savedInstanceState: Bundle?) {
            if (fragment.id != containerId) return
            check(fragment is StackFragment) { "Only Stack Fragments may be added to a container View managed by a MultiStackNavigator" }

            if (stateContainer.isFreshState) rootFunction(fragment.stackId).apply { fragment.navigator.show(first, second) }
        }

        override fun onFragmentResumed(fm: FragmentManager, fragment: Fragment) {
            if (fragment.id != containerId) return
            check(fragment is StackFragment) { "Only Stack Fragments may be added to a container View managed by a MultiStackNavigator" }

            fragment.navigator.transactionModifier = this@MultiStackNavigator.transactionModifier
        }
    }
}

const val NAV_STACK_ORDER = "navState"

class StackFragment : Fragment() {

    internal lateinit var navigator: StackNavigator

    internal var stackId: Int by args()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deferred: StackNavigator by childStackNavigationController(stackId)
        navigator = deferred
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            FragmentContainerView(inflater.context).apply { id = stackId }

    companion object {
        internal fun newInstance(id: Int) = StackFragment().apply { stackId = id }
    }
}
