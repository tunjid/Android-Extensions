package com.tunjid.androidx.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.savedstate.LifecycleSavedStateContainer
import com.tunjid.androidx.savedstate.savedStateFor
import java.util.*

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
            stackFragments
                    .filter { it.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) }
                    .forEach { it.navigator.transactionModifier = value }
        }

    private val backStack: Stack<Int> = Stack()
    private val stackFragments: List<StackFragment>

    private val activeFragment: StackFragment
        get() = stackFragments.run { firstOrNull(Fragment::isVisible) ?: first() }

    val activeNavigator
        get() = activeFragment.navigator

    override val currentFragment: Fragment?
        get() = activeNavigator.currentFragment

    private val StackFragment.index
        get() = stackIds.indexOf(stackId)

    init {
        fragmentManager.registerFragmentLifecycleCallbacks(StackLifecycleCallback(), false)
        fragmentManager.addOnBackStackChangedListener { throw IllegalStateException("Fragments may not be added to the back stack of a FragmentManager managed by a MultiStackNavigator") }

        val stack = Stack<StackFragment>()

        if (stateContainer.isFreshState) fragmentManager.commitNow {
            stackIds.forEachIndexed { index, id -> add(containerId, stack.push(StackFragment.newInstance(id)), index.toString()) }
            track(stack.firstElement())
        }
        else fragmentManager.fragments.filterIsInstance(StackFragment::class.java).forEach { stackFragment ->
            backStack.push(stackFragment.stackId)
            stack.push(stackFragment)
        }

        stackFragments = stack.sortedBy { it.index }
        stateContainer.savedState.getIntArray(NAV_STACK_ORDER)?.apply { backStack.sortBy { indexOf(it) } }
    }

    fun show(@IdRes toShow: Int) = showInternal(toShow, true)

    fun navigatorAt(index: Int) = stackFragments[index].navigator

    /**
     * Pops the current fragment off the stack in focus. If The current
     * Fragment is the only Fragment on it's stack, the stack that was active before the current
     * stack is switched to.
     *
     * @see [StackNavigator.pop]
     */
    override fun pop(): Boolean = when {
        activeFragment.navigator.pop() -> true
        backStack.run { remove(activeFragment.stackId); isEmpty() } -> false
        else -> showInternal(backStack.peek(), false).let { true }
    }

    override fun clear(upToTag: String?, includeMatch: Boolean) = activeNavigator.clear(upToTag, includeMatch)

    override fun show(fragment: Fragment, tag: String): Boolean = activeNavigator.show(fragment, tag)

    private fun showInternal(@IdRes toShow: Int, addTap: Boolean) = fragmentManager.commit {
        stackTransactionModifier?.invoke(this, toShow)

        transactions@ for (fragment in stackFragments) when {
            fragment.stackId == toShow && !fragment.isDetached -> continue@transactions
            fragment.stackId == toShow && fragment.isDetached -> attach(fragment).also { if (addTap) track(fragment) }
            else -> if (!fragment.isDetached) detach(fragment)
        }

        runOnCommit { stackSelectedListener?.invoke(toShow) }
    }

    private fun track(tab: StackFragment) = tab.run {
        if (backStack.contains(stackId)) backStack.remove(stackId)
        backStack.push(stackId)
        stateContainer.savedState.putIntArray(NAV_STACK_ORDER, backStack.toIntArray())
    }

    private inner class StackLifecycleCallback : FragmentManager.FragmentLifecycleCallbacks() {
        val seenMap = BooleanArray(stackIds.size) { false }

        override fun onFragmentCreated(fm: FragmentManager, fragment: Fragment, savedInstanceState: Bundle?) {
            if (fragment.id != containerId) return
            check(fragment is StackFragment) { "Only Stack Fragments may be added to a container View managed by a MultiStackNavigator" }

            if (!stateContainer.isFreshState) return

            if (stackIds.indexOf(fragment.stackId) != 0) fm.beginTransaction().detach(fragment).commit()
        }

        override fun onFragmentViewCreated(fm: FragmentManager, fragment: Fragment, view: View, savedInstanceState: Bundle?) {
            if (fragment.id != containerId) return
            check(fragment is StackFragment) { "Only Stack Fragments may be added to a container View managed by a MultiStackNavigator" }

            fragment.index.let {
                if (!seenMap[it]) rootFunction(fragment.stackId).apply { fragment.navigator.show(first, second) }
                seenMap[it] = true
            }
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
