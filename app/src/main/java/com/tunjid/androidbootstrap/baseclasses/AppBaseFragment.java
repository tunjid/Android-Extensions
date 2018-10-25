package com.tunjid.androidbootstrap.baseclasses;

import android.annotation.SuppressLint;
import android.view.View;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.activities.MainActivity;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.view.animator.FabExtensionAnimator;
import com.tunjid.androidbootstrap.view.animator.FabExtensionAnimator.GlyphState;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import static androidx.core.content.ContextCompat.getDrawable;
import static com.tunjid.androidbootstrap.activities.MainActivity.ANIMATTION_DURATION;

public abstract class AppBaseFragment extends BaseFragment {

    private static final boolean[] DEFAULT = new boolean[]{true, false, true, false};
    protected static final boolean[] NONE = new boolean[]{false, true, false, false};

    public void onResume() {
        super.onResume();
        View view = getView();
        if (view != null) view.postDelayed(this::togglePersistentUi, ANIMATTION_DURATION);
    }

    public void toggleFab(boolean show) { getHostingActivity().toggleFab(show); }

    public void toggleToolbar(boolean show) { getHostingActivity().toggleToolbar(show); }

    public boolean[] insetState() { return DEFAULT; }

    public boolean showsFab() { return false; }

    public boolean showsToolBar() { return true; }

    public void togglePersistentUi() {
        toggleFab(showsFab());
        toggleToolbar(showsToolBar());

        MainActivity hostingActivity = getHostingActivity();
        hostingActivity.updateFab(getFabState());
        hostingActivity.setFabClickListener(getFabClickListener());
    }

    protected void setFabExtended(boolean extended) {
        getHostingActivity().setFabExtended(extended);
    }

    protected boolean isFabExtended() { return getHostingActivity().isFabExtended(); }

    protected GlyphState getFabState() {
        return FabExtensionAnimator.newState(getText(R.string.app_name), getDrawable(requireContext(), R.drawable.ic_circle_24dp));
    }

    protected View.OnClickListener getFabClickListener() { return view -> {}; }

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
