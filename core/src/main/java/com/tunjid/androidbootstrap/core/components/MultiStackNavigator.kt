package com.tunjid.androidbootstrap.core.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import java.util.*
import kotlin.collections.set

const val MULTI_STACK_NAVIGATOR = "com.tunjid.androidbootstrap.core.components.MultiStackNavigator"

fun FragmentActivity.multiStackNavigatorFor(
        @IdRes containerId: Int,
        @MenuRes stackMenu: Int,
        rootFunction: (Int) -> Pair<Fragment, String>
): Lazy<MultiStackNavigator> = lazy {
    PopupMenu(this, findViewById(containerId)).run {
        inflate(stackMenu)
        MultiStackNavigator(
                stateContainerFor("$MULTI_STACK_NAVIGATOR-$containerId", this@multiStackNavigatorFor),
                supportFragmentManager,
                menu.children.map(MenuItem::getItemId).toList().toIntArray(),
                containerId, rootFunction
        ).apply { dismiss() }
    }
}

/**
 * Manages navigation for independent stacks of [Fragment]s, where each stack is managed by a
 * [FragmentStackNavigator].
 */
class MultiStackNavigator(
        private val stateContainer: LifecycleSavedStateContainer,
        private val fragmentManager: FragmentManager,
        val stackIds: IntArray,
        @IdRes val containerId: Int,
        val rootFunction: (Int) -> Pair<Fragment, String>) {

    var stackSelectedListener: ((Int) -> Unit)? = null
    var stackTransactionModifier: (FragmentTransaction.(Int) -> Unit)? = null

    var transactionProvider: ((Fragment) -> FragmentTransaction?)? = null
        set(value) {
            field = value
            stackMap.values
                    .filter { it.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) }
                    .forEach { it.navigator.transactionProvider = value }
        }

    private val navStack: Stack<StackFragment> = Stack()
    private val stackMap = mutableMapOf<Int, StackFragment>()

    private val selectedFragment: StackFragment
        get() = stackMap.values.run { firstOrNull(Fragment::isVisible) ?: first() }

    val currentNavigator
        get() = selectedFragment.navigator

    init {
        fragmentManager.registerFragmentLifecycleCallbacks(StackLifecycleCallback(), false)
        fragmentManager.addOnBackStackChangedListener { throw IllegalStateException("Fragments may not be added to the back stack of a FragmentManager managed by a MultiStackNavigator") }

        if (stateContainer.isFreshState) fragmentManager.commitNow {
            stackIds.forEachIndexed { index, id -> add(containerId, StackFragment.newInstance(id), index.toString()) }
        }
        else fragmentManager.fragments.filterIsInstance(StackFragment::class.java).forEach {
            navStack.push(it)
            stackMap[it.stackId] = it
        }.apply { stateContainer.savedState.getIntArray(NAV_STACK_ORDER)?.apply { navStack.sortBy { indexOf(it.stackId) } } }
    }

    fun show(@IdRes toShow: Int) = showInternal(toShow, true)

    fun pop(): Boolean = when (val selected = selectedFragment) {
        else -> when {
            selected.navigator.pop() -> true
            else -> when {
                navStack.run { remove(selected); isEmpty() } -> false
                else -> showInternal(navStack.peek().stackId, false).let { true }
            }
        }
    }

    private fun showInternal(@IdRes toShow: Int, addTap: Boolean) {
        fragmentManager.commit {
            stackTransactionModifier?.invoke(this, toShow)

            for ((id, fragment) in stackMap) when {
                id == toShow && fragment.isVisible -> return@commit
                id == toShow && fragment.isHidden -> show(fragment).also { if (addTap) track(fragment) }
                else -> hide(fragment)
            }
        }
        stackSelectedListener?.invoke(toShow)
    }

    private fun track(tab: StackFragment) {
        if (navStack.contains(tab)) navStack.remove(tab)
        navStack.push(tab)
        stateContainer.savedState.putIntArray(NAV_STACK_ORDER, navStack.map(StackFragment::stackId).toIntArray())
    }

    inner class StackLifecycleCallback : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentCreated(fm: FragmentManager, fragment: Fragment, savedInstanceState: Bundle?) {
            if (fragment.id != containerId) return
            check(fragment is StackFragment) { "Only Stack Fragments may be added to a container View managed by a MultiStackNavigator" }

            fragment.apply { stackMap[stackId] = fragment }

            if (savedInstanceState == null && stackIds.indexOf(fragment.stackId) != 0) fm.beginTransaction().hide(fragment).commit()
            else navStack.add(fragment)
        }

        override fun onFragmentViewCreated(fm: FragmentManager, fragment: Fragment, view: View, savedInstanceState: Bundle?) {
            if (fragment.id != containerId) return
            check(fragment is StackFragment) { "Only Stack Fragments may be added to a container View managed by a MultiStackNavigator" }

            if (stateContainer.isFreshState) rootFunction(fragment.stackId).apply {
                fragment.navigator.show(first, second)
            }
        }

        override fun onFragmentResumed(fm: FragmentManager, fragment: Fragment) {
            if (fragment.id != containerId) return
            check(fragment is StackFragment) { "Only Stack Fragments may be added to a container View managed by a MultiStackNavigator" }

            fragment.navigator.transactionProvider = this@MultiStackNavigator.transactionProvider
        }
    }
}

const val ID_KEY = "id"
const val NAV_STACK_ORDER = "navState"

class StackFragment : Fragment() {

    lateinit var navigator: FragmentStackNavigator

    val stackId: Int
        get() = arguments?.getInt(ID_KEY)!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deferred: FragmentStackNavigator by childFragmentStackNavigator(stackId)
        navigator = deferred
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            FragmentContainerView(inflater.context).apply { id = arguments!!.getInt(ID_KEY) }

    companion object {
        internal fun newInstance(id: Int) = StackFragment().apply {
            arguments = bundleOf(ID_KEY to id)
        }
    }
}
