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

const val MULTI_STACK_NAVIGATOR = "com.tunjid.androidx.navigation.MultiStackNavigator"

fun Fragment.childMultiStackNavigationController(
        stackCount: Int,
        @IdRes containerId: Int,
        rootFunction: (Int) -> Pair<Fragment, String>
): Lazy<MultiStackNavigator> = lazy {
    MultiStackNavigator(
            stackCount,
            savedStateFor(this@childMultiStackNavigationController, "$MULTI_STACK_NAVIGATOR-$containerId"),
            childFragmentManager,
            containerId, rootFunction
    )
}

fun FragmentActivity.multiStackNavigationController(
        stackCount: Int,
        @IdRes containerId: Int,
        rootFunction: (Int) -> Pair<Fragment, String>
): Lazy<MultiStackNavigator> = lazy {
    MultiStackNavigator(
            stackCount,
            savedStateFor(this@multiStackNavigationController, "$MULTI_STACK_NAVIGATOR-$containerId"),
            supportFragmentManager,
            containerId,
            rootFunction
    )
}

/**
 * Manages navigation for independent stacks of [Fragment]s, where each stack is managed by a
 * [StackNavigator].
 */
class MultiStackNavigator(
        stackCount: Int,
        private val stateContainer: LifecycleSavedStateContainer,
        private val fragmentManager: FragmentManager,
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

    private val indices = 0 until stackCount
    internal val visitStack: Stack<Int> = Stack()
    internal val stackFragments: List<StackFragment>

    private val activeFragment: StackFragment
        get() = stackFragments.run { firstOrNull(Fragment::isAttached) ?: first() }

    val activeIndex
        get() = activeFragment.index

    val activeNavigator
        get() = activeFragment.navigator

    override val currentFragment: Fragment?
        get() = activeNavigator.currentFragment

    init {
        fragmentManager.registerFragmentLifecycleCallbacks(StackLifecycleCallback(), false)
        fragmentManager.addOnBackStackChangedListener { throw IllegalStateException("Fragments may not be added to the back stack of a FragmentManager managed by a MultiStackNavigator") }

        val freshState = stateContainer.isFreshState

        if (freshState) fragmentManager.commitNow {
            indices.forEach { index -> add(containerId, StackFragment.newInstance(index), index.toString()) }
        }
        else fragmentManager.addedStackFragments(indices).forEach { stackFragment ->
            visitStack.push(stackFragment.index)
        }

        stateContainer.savedState.getIntArray(NAV_STACK_ORDER)?.apply { visitStack.sortBy { indexOf(it) } }
        stackFragments = fragmentManager.addedStackFragments(indices)

        if (freshState) show(0)
    }

    fun show(index: Int) = showInternal(index, true)

    override fun peek(): Fragment? = when (val peeked = activeNavigator.peek()) {
        is Fragment -> peeked
        else -> visitStack.run { elementAtOrNull(lastIndex - 1) }?.let { penultimate ->
            stackFragments.elementAtOrNull(penultimate)?.navigator?.currentFragment
        }
    }

    /**
     * Pops the current fragment off the stack in focus. If The current
     * Fragment is the only Fragment on it's stack, the stack that was active before the current
     * stack is switched to.
     *
     * @see [StackNavigator.pop]
     */
    override fun pop(): Boolean = when {
        activeFragment.navigator.pop() -> true
        visitStack.run { remove(activeFragment.index); isEmpty() } -> false
        else -> showInternal(visitStack.peek(), false).let { true }
    }

    override fun clear(upToTag: String?, includeMatch: Boolean) = activeNavigator.clear(upToTag, includeMatch)

    override fun push(fragment: Fragment, tag: String): Boolean = activeNavigator.push(fragment, tag)

    private fun showInternal(index: Int, addTap: Boolean) = fragmentManager.commit {
        val toShow = stackFragments[index]
        if (addTap) track(toShow)

        stackTransactionModifier?.invoke(this, index)

        transactions@ for (fragment in stackFragments) when {
            fragment.index == index && !fragment.isDetached -> continue@transactions
            fragment.index == index && fragment.isDetached -> attach(fragment)
            else -> if (!fragment.isDetached) detach(fragment)
        }

        runOnCommit { stackSelectedListener?.invoke(index) }
    }

    private fun track(tab: StackFragment) = tab.run {
        if (visitStack.contains(index)) visitStack.remove(index)
        visitStack.push(index)
        stateContainer.savedState.putIntArray(NAV_STACK_ORDER, visitStack.toIntArray())
    }

    private fun StackFragment.showRoot() = rootFunction(index).apply { navigator.push(first, second) }

    private inner class StackLifecycleCallback : FragmentManager.FragmentLifecycleCallbacks() {

        override fun onFragmentCreated(fm: FragmentManager, fragment: Fragment, savedInstanceState: Bundle?) = fragment.run {
            if (id != this@MultiStackNavigator.containerId) return
            check(this is StackFragment) { "Only Stack Fragments may be added to a container View managed by a MultiStackNavigator" }

            if (!stateContainer.isFreshState) return

            if (index != 0) fm.commit { detach(this@run) }
        }

        override fun onFragmentResumed(fm: FragmentManager, fragment: Fragment) = fragment.run {
            if (id != this@MultiStackNavigator.containerId) return
            check(this is StackFragment) { "Only Stack Fragments may be added to a container View managed by a MultiStackNavigator" }

            navigator.transactionModifier = this@MultiStackNavigator.transactionModifier
            if (hasNoRoot) showRoot()
        }
    }
}

class StackFragment : Fragment() {

    internal lateinit var navigator: StackNavigator

    internal var index: Int by args()
    private var containerId: Int by args()

    internal val hasNoRoot get() = navigator.currentFragment == null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deferred: StackNavigator by childStackNavigationController(containerId)
        navigator = deferred
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            FragmentContainerView(inflater.context).apply { id = containerId }

    companion object {
        internal fun newInstance(index: Int) = StackFragment().apply { this.index = index; containerId = View.generateViewId() }
    }
}

const val NAV_STACK_ORDER = "navState"

private val Fragment.isAttached get() = !isDetached

private fun FragmentManager.addedStackFragments(indices: IntRange) = indices
        .map(Int::toString)
        .map(::findFragmentByTag)
        .filterIsInstance(StackFragment::class.java)