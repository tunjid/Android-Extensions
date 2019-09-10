package com.tunjid.androidbootstrap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.tunjid.androidbootstrap.core.components.FragmentStateViewModel
import com.tunjid.androidbootstrap.core.components.childFragmentStateViewModelFactory
import java.util.*
import kotlin.collections.set

class MultiStackNavigator(
        private val fragmentManager: FragmentManager,
        val stackIds: IntArray,
        @IdRes containerId: Int,
        val rootFunction: (Int) -> Pair<Fragment, String>) {

    var stackSelectedListener: ((Int) -> Unit)? = null
    private val navStack: Stack<StackFragment> = Stack()
    private val stackMap = mutableMapOf<Int, StackFragment>()

    private val selectedFragment: StackFragment?
        get() = stackMap.values.firstOrNull { it.isVisible }

    val currentFragmentStateViewModel
        get() = selectedFragment?.fragmentStateViewModel

    val currentFragment: Fragment?
        get() = selectedFragment?.run { childFragmentManager.findFragmentById(stackId) }

    init {
        fragmentManager.registerFragmentLifecycleCallbacks(TabLifecycleCallback(), false)
        fragmentManager.commit {
            for ((index, id) in stackIds.withIndex()) add(containerId, StackFragment.newInstance(id), index.toString())
        }
    }

    fun show(@IdRes toShow: Int) = showInternal(toShow, true)

    fun pop(): Boolean = when (val selected = selectedFragment) {
        is StackFragment -> when {
            selected.childFragmentManager.backStackEntryCount > 1 -> selected.childFragmentManager.popBackStack().let { true }
            navStack.run { remove(selected); isEmpty() } -> false
            else -> showInternal(navStack.pop().stackId, false).let { true }
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
    }

    inner class TabLifecycleCallback : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentCreated(fm: FragmentManager, fragment: Fragment, savedInstanceState: Bundle?) {
            if (fragment !is StackFragment) return

            fragment.apply { stackMap[stackId] = this }

            if (savedInstanceState == null && stackIds.indexOf(fragment.stackId) != 0) fm.beginTransaction().hide(fragment).commit()
            else navStack.add(fragment)
        }

        override fun onFragmentViewCreated(fm: FragmentManager, fragment: Fragment, view: View, savedInstanceState: Bundle?) {
            if (fragment !is StackFragment) return

            val rootId = fragment.stackId
            if (savedInstanceState == null) rootFunction(rootId).apply {
                fragment.fragmentStateViewModel.show(first, second)
            }
        }
    }
}

const val ID_KEY = "id"

class StackFragment : Fragment() {

    lateinit var fragmentStateViewModel: FragmentStateViewModel

    val stackId: Int
        get() = arguments?.getInt(ID_KEY)!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deferred: FragmentStateViewModel by childFragmentStateViewModelFactory(stackId)
        fragmentStateViewModel = deferred
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            FragmentContainerView(inflater.context).apply { id = arguments!!.getInt(ID_KEY) }

    companion object {
        internal fun newInstance(id: Int) = StackFragment().apply {
            arguments = bundleOf(ID_KEY to id)
        }
    }
}
