package com.tunjid.androidbootstrap.core.abstractclasses;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.core.components.FragmentStateManager;

/**
 * Base Activity class
 */
public abstract class BaseActivity extends AppCompatActivity {

    private FragmentStateManager fragmentStateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentStateManager = new FragmentStateManager(getSupportFragmentManager());
        fragmentStateManager.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);

        @IdRes int mainContainerId = fragmentStateManager.getIdResource();
        // Check if this activity has a main fragment container viewgroup
        View mainFragmentContainer = findViewById(mainContainerId);

        if (mainFragmentContainer == null || !(mainFragmentContainer instanceof ViewGroup)) {
            throw new IllegalArgumentException("This activity must include a ViewGroup with id '" +
                    getResources().getResourceName(mainContainerId) +
                    "' for dynamically added fragments");
        }
    }

    @Override
    public void onBackPressed() {
        BaseFragment fragment = getCurrentFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Check if fragment handled back press
        if (fragment != null && fragment.isVisible() && fragment.handledBackPress()) return;

        if (fragmentManager.getBackStackEntryCount() > 1) fragmentManager.popBackStack();
        else finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        fragmentStateManager.onSaveInstanceState(outState);
    }

    /**
     * Convenience method for {@link FragmentStateManager#getCurrentFragment()}
     */
    public BaseFragment getCurrentFragment() {
        return (BaseFragment) fragmentStateManager.getCurrentFragment();
    }

    /**
     * Convenience method for {@link FragmentStateManager#showFragment(FragmentTransaction,
     * Fragment, String)}
     */
    public boolean showFragment(BaseFragment fragment) {
        BaseFragment currentFragment = getCurrentFragment();

        FragmentTransaction providedTransaction = currentFragment != null
                ? currentFragment.provideFragmentTransaction(fragment)
                : null;

        @SuppressLint("CommitTransaction")
        FragmentTransaction transaction = providedTransaction != null
                ? providedTransaction
                : getSupportFragmentManager().beginTransaction();

        return fragmentStateManager.showFragment(transaction, fragment);
    }
}
