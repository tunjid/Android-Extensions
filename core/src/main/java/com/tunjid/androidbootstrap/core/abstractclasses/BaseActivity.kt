package com.tunjid.androidbootstrap.core.abstractclasses

import android.annotation.SuppressLint
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import com.tunjid.androidbootstrap.core.R
import com.tunjid.androidbootstrap.core.components.FragmentStateViewModel
import com.tunjid.androidbootstrap.core.components.fragmentStateViewModelFactory

/**
 * Base Activity class
 */
abstract class BaseActivity(@LayoutRes contentLayoutId: Int = 0) : AppCompatActivity(contentLayoutId) {

    private val fragmentStateViewModel: FragmentStateViewModel by fragmentStateViewModelFactory(R.id.main_fragment_container)

    /**
     * Convenience method for [FragmentStateViewModel.currentFragment]
     */
    open val currentFragment: BaseFragment?
        get() = fragmentStateViewModel.currentFragment as BaseFragment?

    override fun setContentView(@LayoutRes layoutResID: Int) {
        super.setContentView(layoutResID)

        // Check if this activity has a main fragment container viewgroup

        require(findViewById<View>(fragmentStateViewModel.idResource) is FragmentContainerView) {
            "This activity must include a FragmentContainerView with id '" +
                    resources.getResourceName(fragmentStateViewModel.idResource) +
                    "' for dynamically added fragments"
        }
    }

    /**
     * Convenience method for [FragmentStateViewModel.showFragment]
     */
    fun showFragment(fragment: BaseFragment): Boolean {
        val currentFragment = currentFragment

        val providedTransaction = currentFragment?.provideFragmentTransaction(fragment)

        @SuppressLint("CommitTransaction")
        val transaction = providedTransaction ?: supportFragmentManager.beginTransaction()

        return fragmentStateViewModel.showFragment(transaction, fragment)
    }
}
