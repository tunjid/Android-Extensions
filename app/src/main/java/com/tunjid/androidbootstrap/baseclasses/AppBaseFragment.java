package com.tunjid.androidbootstrap.baseclasses;

import android.annotation.SuppressLint;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.activities.MainActivity;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.view.animator.FabExtensionAnimator;
import com.tunjid.androidbootstrap.view.animator.FabExtensionAnimator.State;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import static androidx.core.content.ContextCompat.getDrawable;

public abstract class AppBaseFragment extends BaseFragment {

    public static final boolean[] DEFAULT = new boolean[]{true, false, true, false};
    public static final boolean[] NONE = new boolean[]{false, true, false, false};

    public void onResume() {
        super.onResume();
        if (getView() != null) togglePersistentUi();
    }

    public void toggleFab(boolean show) { getHostingActivity().toggleFab(show); }

    public void toggleToolbar(boolean show) { getHostingActivity().toggleToolbar(show); }

    public boolean[] insetState() { return DEFAULT; }

    public boolean showsFab() { return false; }

    public boolean showsToolBar() { return true; }

    public void togglePersistentUi() {
        toggleFab(showsFab());
        toggleToolbar(showsToolBar());
        getHostingActivity().updateFab(getFabState());
    }

    public State getFabState() {
        return FabExtensionAnimator.newState("", getDrawable(requireContext(), R.drawable.ic_circle_24dp));
    }

    private MainActivity getHostingActivity() {return (MainActivity) requireActivity(); }

    @Nullable
    @Override
    @SuppressLint("CommitTransaction")
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        return requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
