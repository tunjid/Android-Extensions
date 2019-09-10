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
import java.util.*
import kotlin.collections.set

const val MULTI_STACK_NAVIGATOR = "com.tunjid.androidbootstrap.core.components.MultiStackNavigator"

fun FragmentActivity.multiStackNavigatorFor(
        @IdRes containerId: Int,
        @MenuRes stackMenu: Int,
        rootFunction: (Int) -> Pair<Fragment, String>
): Lazy<MultiStackNavigator> = lazy {

    val popUp = PopupMenu(this, findViewById(containerId)).apply { inflate(stackMenu) }
    val items = popUp.menu.children.map(MenuItem::getItemId).toList().toIntArray()
    popUp.dismiss()

    MultiStackNavigator(
            stateContainerFor("$MULTI_STACK_NAVIGATOR-$containerId", this),
            supportFragmentManager,
            items,
            containerId, rootFunction
    )
}

class MultiStackNavigator(
        private val stateContainer: LifecycleSavedStateContainer,
        private val fragmentManager: FragmentManager,
        val stackIds: IntArray,
        @IdRes containerId: Int,
        val rootFunction: (Int) -> Pair<Fragment, String>) {

    var stackSelectedListener: ((Int) -> Unit)? = null

    private val navStack: Stack<StackFragment> = Stack()
    private val stackMap = mutableMapOf<Int, StackFragment>()

    private val selectedFragment: StackFragment?
        get() = stackMap.values.firstOrNull(Fragment::isVisible)

    val currentNavigator
        get() = selectedFragment?.navigator

    val currentFragment: Fragment?
        get() = currentNavigator?.currentFragment

    init {
        fragmentManager.registerFragmentLifecycleCallbacks(StackLifecycleCallback(), false)
        fragmentManager.commit {
            for ((index, id) in stackIds.withIndex()) add(containerId, StackFragment.newInstance(id), index.toString())
        }
    }

    fun show(@IdRes toShow: Int) = showInternal(toShow, true)

    fun pop(): Boolean = when (val selected = selectedFragment) {
        is StackFragment -> when {
            selected.navigator.pop() -> true
            else -> when {
                navStack.run { remove(selected); isEmpty() } -> false
                else -> showInternal(navStack.pop().stackId, false).let { true }
            }
        }
        else -> false
    }

    private fun showInternal(@IdRes toShow: Int, addTap: Boolean) {
        fragmentManager.commit {
            setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            )
            for ((id, fragment) in stackMap) when {
                id == toShow && fragment.isVisible -> return@commit
                id == toShow && fragment.isHidden -> fragment.apply { show(this); if (addTap) track(this) }
                else -> hide(fragment)
            }
        }
        stackSelectedListener?.invoke(toShow)
    }

    private fun track(tab: StackFragment) {
        if (navStack.contains(tab)) navStack.remove(tab)
        navStack.add(tab)
        stateContainer.savedState.putIntArray(NAV_STATE, navStack.map(StackFragment::stackId).toIntArray())
    }

    inner class StackLifecycleCallback : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentCreated(fm: FragmentManager, fragment: Fragment, savedInstanceState: Bundle?) {
            if (fragment !is StackFragment) return

            fragment.apply { stackMap[stackId] = this }

            if (savedInstanceState == null && stackIds.indexOf(fragment.stackId) != 0) fm.beginTransaction().hide(fragment).commit()
            else navStack.add(fragment)
        }

        override fun onFragmentViewCreated(fm: FragmentManager, fragment: Fragment, view: View, savedInstanceState: Bundle?) {
            if (fragment is StackFragment && savedInstanceState == null) rootFunction(fragment.stackId).apply {
                fragment.navigator.show(first, second)
            }
        }
    }
}

const val ID_KEY = "id"
const val NAV_STATE = "navState"

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
