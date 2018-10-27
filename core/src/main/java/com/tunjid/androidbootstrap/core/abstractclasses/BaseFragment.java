package com.tunjid.androidbootstrap.core.abstractclasses;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.tunjid.androidbootstrap.core.components.FragmentStateManager;

/**
 * Base fragment
 */
public abstract class BaseFragment extends Fragment
        implements
        BackPressInterceptor,
        FragmentStateManager.FragmentTagProvider {

    private static final String VIEW_DESTROYED = "com.tunjid.androidbootstrap.core.abstractclasses.basefragment.view.destroyed";

    public boolean showFragment(BaseFragment fragment) {
        return ((BaseActivity) requireActivity()).showFragment(fragment);
    }

    /**
     * Checks whether this fragment was shown before and it's view subsequently
     * destroyed by placing it in the back stack
     */
    public boolean restoredFromBackStack() {
        Bundle args = getArguments();
        return args != null && args.containsKey(VIEW_DESTROYED)
                && args.getBoolean(VIEW_DESTROYED);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragmentActivity activity = getActivity();
        if (activity != null && !(activity instanceof BaseActivity)) {
            throw new IllegalStateException("This fragment may only be used with a "
                    + BaseActivity.class.getName());
        }
    }

    /**
     * Allows the providing of a {@link FragmentTransaction} for a particular fragment to allow for
     * adding shared transitions or other custom attributes for the transaction.
     *
     * @param fragmentTo The fragment about to be shown
     * @return A custom {@link FragmentTransaction} or null to use the default
     */
    @Nullable
    @SuppressWarnings("unused")
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Bundle args = getArguments();

        if (args != null) {
            args.putBoolean(VIEW_DESTROYED, true);
        }
    }

    @Override
    public boolean handledBackPress() {
        return false;
    }

    @Override
    public String getStableTag() {
        return getClass().getSimpleName();
    }

    @Override
    public Fragment getFragment() {
        return this;
    }
}
