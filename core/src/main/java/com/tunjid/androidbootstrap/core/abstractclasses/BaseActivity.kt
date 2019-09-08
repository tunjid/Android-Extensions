package com.tunjid.androidbootstrap.core.abstractclasses

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.tunjid.androidbootstrap.core.components.FragmentStateManager

/**
 * Base Activity class
 */
abstract class BaseActivity(@LayoutRes contentLayoutId: Int = 0) : AppCompatActivity(contentLayoutId) {

    private lateinit var fragmentStateManager: FragmentStateManager

    /**
     * Convenience method for [FragmentStateManager.currentFragment]
     */
    open val currentFragment: BaseFragment?
        get() = fragmentStateManager.currentFragment as BaseFragment?

    override fun onCreate(savedInstanceState: Bundle?) {
        fragmentStateManager = FragmentStateManager(supportFragmentManager)
        fragmentStateManager.onRestoreInstanceState(savedInstanceState)
        super.onCreate(savedInstanceState)
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        super.setContentView(layoutResID)

        @IdRes val mainContainerId = fragmentStateManager.idResource
        // Check if this activity has a main fragment container viewgroup
        val mainFragmentContainer = findViewById<View>(mainContainerId)

        require(mainFragmentContainer is ViewGroup) {
            "This activity must include a ViewGroup with id '" +
                    resources.getResourceName(mainContainerId) +
                    "' for dynamically added fragments"
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragmentStateManager.onSaveInstanceState(outState)
    }

    /**
     * Convenience method for [FragmentStateManager.showFragment]
     */
    fun showFragment(fragment: BaseFragment): Boolean {
        val currentFragment = currentFragment

        val providedTransaction = currentFragment?.provideFragmentTransaction(fragment)

        @SuppressLint("CommitTransaction")
        val transaction = providedTransaction ?: supportFragmentManager.beginTransaction()

        return fragmentStateManager.showFragment(transaction, fragment)
    }
}
