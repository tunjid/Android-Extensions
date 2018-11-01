package com.tunjid.androidbootstrap.baseclasses;

import android.annotation.SuppressLint;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.View;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.activities.MainActivity;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.view.animator.FabExtensionAnimator;
import com.tunjid.androidbootstrap.view.animator.FabExtensionAnimator.GlyphState;
import com.tunjid.androidbootstrap.view.util.InsetFlags;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import static androidx.core.content.ContextCompat.getDrawable;
import static com.tunjid.androidbootstrap.activities.MainActivity.ANIMATION_DURATION;

public abstract class AppBaseFragment extends BaseFragment {

    public void onResume() {
        super.onResume();
        View view = getView();
        if (view != null) view.postDelayed(this::togglePersistentUi, ANIMATION_DURATION);
    }

    public void toggleFab(boolean show) { getHostingActivity().toggleFab(show); }

    public void toggleToolbar(boolean show) { getHostingActivity().toggleToolbar(show); }

    public InsetFlags insetFlags() { return InsetFlags.ALL; }

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

    protected Transition baseTransition() {return new Fade();}

    protected Transition baseSharedTransition() {
        return new TransitionSet()
                .setOrdering(TransitionSet.ORDERING_TOGETHER)
                .addTransition(new ChangeBounds())
                .addTransition(new ChangeTransform())
                .addTransition(new ChangeImageTransform());
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
