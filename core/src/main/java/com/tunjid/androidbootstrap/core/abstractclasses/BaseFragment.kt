package com.tunjid.androidbootstrap.core.abstractclasses

import android.content.Context
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.tunjid.androidbootstrap.core.components.FragmentStateManager

/**
 * Base fragment
 */
abstract class BaseFragment @JvmOverloads constructor(
        @LayoutRes contentLayoutId: Int = 0
) : Fragment(contentLayoutId), FragmentStateManager.FragmentTagProvider {

    override val stableTag: String
        get() = javaClass.simpleName

    override val fragment: Fragment
        get() = this

    fun showFragment(fragment: BaseFragment): Boolean =
            (activity as BaseActivity).showFragment(fragment)

    /**
     * Checks whether this fragment was shown before and it's view subsequently
     * destroyed by placing it in the back stack
     */
    fun restoredFromBackStack(): Boolean {
        val args = arguments
        return (args != null && args.containsKey(VIEW_DESTROYED)
                && args.getBoolean(VIEW_DESTROYED))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activity = activity
        check(!(activity != null && activity !is BaseActivity)) { "This fragment may only be used with a " + BaseActivity::class.java.name }
    }

    /**
     * Allows the providing of a [FragmentTransaction] for a particular fragment to allow for
     * adding shared transitions or other custom attributes for the transaction.
     *
     * @param fragmentTo The fragment about to be shown
     * @return A custom [FragmentTransaction] or null to use the default
     */
    open fun provideFragmentTransaction(fragmentTo: BaseFragment): FragmentTransaction? = null

    override fun onDestroyView() {
        super.onDestroyView()

        val args = arguments

        args?.putBoolean(VIEW_DESTROYED, true)
    }

    companion object {

        private const val VIEW_DESTROYED = "com.tunjid.androidbootstrap.core.abstractclasses.basefragment.view.destroyed"
    }
}
