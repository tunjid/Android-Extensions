package com.tunjid.androidbootstrap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import com.tunjid.androidbootstrap.core.components.FragmentStateViewModel
import com.tunjid.androidbootstrap.core.components.childFragmentStateViewModelFactory
import java.util.*
import kotlin.collections.set

class MultiStackNavigator(
        private val fragmentManager: FragmentManager,
        val stackIds: IntArray,
        @IdRes containerId: Int,
        val rootFunction: (Int) -> Pair<Fragment, String>) {

    private val tapped: Deque<TabbedFragment> = ArrayDeque()
    private val tabMap = mutableMapOf<Int, TabbedFragment>()

    private val selectedFragment: TabbedFragment?
        get() = tabMap.values.firstOrNull { it.isVisible }

    val currentFragmentStateViewModel
        get() = selectedFragment?.fragmentStateViewModel

    val currentFragment: Fragment?
        get() = selectedFragment?.run { childFragmentManager.findFragmentById(rootId) }

    init {
        fragmentManager.registerFragmentLifecycleCallbacks(TabLifecycleCallback(), false)
        fragmentManager.commit {
            for ((index, id) in stackIds.withIndex()) add(containerId, TabbedFragment.newInstance(id), index.toString())
        }
    }

    fun show(@IdRes toShow: Int) = showInternal(toShow, true)

    fun pop(): Boolean = when (val selected = selectedFragment) {
        is TabbedFragment -> when {
            selected.childFragmentManager.backStackEntryCount > 1 -> selected.childFragmentManager.popBackStack().let { true }
            tapped.run { remove(selected); isEmpty() } -> false
            else -> showInternal(tapped.remove().rootId, false).let { true }
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
            for ((id, fragment) in tabMap) when {
                id == toShow && fragment.isVisible -> return@commit
                id == toShow && fragment.isHidden -> fragment.apply { show(this); if (addTap) track(this) }
                else -> hide(fragment)
            }
        }
    }

    private fun track(tab: TabbedFragment) {
        if (tapped.contains(tab)) {
            tapped.remove(tab)
            tapped.addFirst(tab)
        } else tapped.add(tab)
    }

    inner class TabLifecycleCallback : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentCreated(fm: FragmentManager, fragment: Fragment, savedInstanceState: Bundle?) {
            if (fragment !is TabbedFragment) return

            fragment.apply { tabMap[rootId] = this }

            if (savedInstanceState == null && stackIds.indexOf(fragment.rootId) != 0) fm.beginTransaction().hide(fragment).commit()
            else tapped.add(fragment)
        }

        override fun onFragmentViewCreated(fm: FragmentManager, fragment: Fragment, view: View, savedInstanceState: Bundle?) {
            if (fragment !is TabbedFragment) return

            val rootId = fragment.rootId
            if (savedInstanceState == null) rootFunction(rootId).apply {
                fragment.fragmentStateViewModel.showFragment(first, second)
            }
        }
    }
}

const val ID_KEY = "id"

class TabbedFragment : Fragment() {

    lateinit var fragmentStateViewModel: FragmentStateViewModel

    val rootId: Int
        get() = arguments?.getInt(ID_KEY)!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deferred: FragmentStateViewModel by childFragmentStateViewModelFactory(rootId)
        fragmentStateViewModel = deferred
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            FragmentContainerView(inflater.context).apply { id = arguments!!.getInt(ID_KEY) }

    companion object {
        fun newInstance(id: Int) = TabbedFragment().apply {
            arguments = bundleOf(ID_KEY to id)
        }
    }
}
