package com.tunjid.androidbootstrap.core.abstractclasses;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.core.R;
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
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);

        // Check if this activity has a main fragment container viewgroup
        View mainFragmentContainer = findViewById(R.id.main_fragment_container);

        if (mainFragmentContainer == null || !(mainFragmentContainer instanceof ViewGroup)) {
            throw new IllegalArgumentException("This activity must include a ViewGroup " +
                    "with id 'main_fragment_container' for dynamically added fragments");
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

        return fragmentStateManager.showFragment(transaction, fragment, fragment.getStableTag());
    }
}
