package com.tunjid.androidbootstrap.baseclasses;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.activities.MainActivity;
import com.tunjid.androidbootstrap.communications.nsd.NsdHelper;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.functions.Consumer;
import com.tunjid.androidbootstrap.material.animator.FabExtensionAnimator;
import com.tunjid.androidbootstrap.material.animator.FabExtensionAnimator.GlyphState;
import com.tunjid.androidbootstrap.view.util.InsetFlags;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import io.reactivex.disposables.CompositeDisposable;

import static androidx.core.content.ContextCompat.getDrawable;
import static com.tunjid.androidbootstrap.activities.MainActivity.ANIMATION_DURATION;

public abstract class AppBaseFragment extends BaseFragment {

    private static final int BACKGROUND_TINT_DURATION = 1200;

    protected CompositeDisposable disposables = new CompositeDisposable();

    public void onResume() {
        super.onResume();
        View view = getView();
        if (view != null) view.postDelayed(this::togglePersistentUi, ANIMATION_DURATION);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
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

    protected void showSnackbar(Consumer<Snackbar> consumer) { getHostingActivity().showSnackBar(consumer); }

    protected Transition baseTransition() {return new Fade();}

    protected Transition baseSharedTransition() {
        return new TransitionSet()
                .setOrdering(TransitionSet.ORDERING_TOGETHER)
                .addTransition(new ChangeBounds())
                .addTransition(new ChangeTransform())
                .addTransition(new ChangeImageTransform());
    }

    protected <T extends View> void tintView(@ColorRes int colorRes, T view, NsdHelper.BiConsumer<Integer, T> biConsumer) {
        final int endColor = ContextCompat.getColor(requireContext(), colorRes);
        final int startColor = Color.TRANSPARENT;

        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
        animator.setDuration(BACKGROUND_TINT_DURATION);
        animator.addUpdateListener(animation -> {
            Integer color = (Integer) animation.getAnimatedValue();
            if (color == null) return;
            biConsumer.accept(color, view);
        });
        animator.start();
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
