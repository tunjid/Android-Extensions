package com.tunjid.androidbootstrap.fragments;

import android.content.res.Resources;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.adapters.DoggoPagerAdapter;
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment;
import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.model.Doggo;
import com.tunjid.androidbootstrap.view.animator.FabExtensionAnimator.State;
import com.tunjid.androidbootstrap.view.animator.ViewPagerIndicatorAnimator;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.SharedElementCallback;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener;

import static com.tunjid.androidbootstrap.view.animator.FabExtensionAnimator.newState;

public class DoggoPagerFragment extends AppBaseFragment {

    private ViewPager viewPager;

    public static DoggoPagerFragment newInstance() {
        DoggoPagerFragment fragment = new DoggoPagerFragment();
        fragment.setArguments(new Bundle());
        fragment.prepareSharedElementTransition();
        return fragment;
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_doggo_pager, container, false);
        this.viewPager = root.findViewById(R.id.view_pager);
        this.viewPager.setAdapter(new DoggoPagerAdapter(Doggo.doggos, getChildFragmentManager()));
        this.viewPager.setCurrentItem(Doggo.getTransitionIndex());
        this.viewPager.addOnPageChangeListener(new SimpleOnPageChangeListener() {
            public void onPageSelected(int position) { onDoggoSwiped(position); }
        });

        Resources resources = getResources();
        ViewPagerIndicatorAnimator.builder()
                .setIndicatorWidth(resources.getDimensionPixelSize(R.dimen.single_and_half_margin))
                .setIndicatorHeight(resources.getDimensionPixelSize(R.dimen.single_and_half_margin))
                .setIndicatorPadding(resources.getDimensionPixelSize(R.dimen.half_margin))
                .setInActiveDrawable(R.drawable.ic_circle_24dp)
                .setActiveDrawable(R.drawable.ic_doggo_24dp)
                .setGuideLine(root.findViewById(R.id.guide))
                .setContainer((ConstraintLayout) root)
                .setViewPager(viewPager)
                .build();

        prepareSharedElementTransition();

        if (savedInstanceState == null) postponeEnterTransition();
        return root;
    }

    private void onDoggoSwiped(int position) {
        Doggo.setTransitionDoggo(Doggo.doggos.get(position));
        togglePersistentUi();
    }

    public boolean[] insetState() { return NONE; }

    public boolean showsToolBar() { return false; }

    public boolean showsFab() { return true; }

    public State getFabState() {
        return newState(Doggo.getTransitionDoggo().getName(), ContextCompat.getDrawable(requireContext(), R.drawable.ic_doggo_24dp));
    }

    public static Transition getTransition() {
        TransitionSet result = null;
        if (VERSION.SDK_INT >= 21) {
            result = new TransitionSet();
            result.setOrdering(TransitionSet.ORDERING_TOGETHER)
                    .addTransition(new ChangeBounds())
                    .addTransition(new ChangeTransform())
                    .addTransition(new ChangeImageTransform());
        }
        return result;
    }

    private void prepareSharedElementTransition() {
        Transition baseTransition = new Fade();
        Transition baseSharedTransition = getTransition();
//        baseTransition.excludeTarget(2131230815, true);

        setEnterTransition(baseTransition);
        setExitTransition(baseTransition);
        setSharedElementEnterTransition(baseSharedTransition);
        setSharedElementReturnTransition(baseSharedTransition);

        setEnterSharedElementCallback(new SharedElementCallback() {
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                Fragment currentFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, Doggo.getTransitionIndex());
                View view = currentFragment.getView();
                if (view == null) return;

                sharedElements.put(names.get(0), view.findViewById(R.id.doggo_image));
            }
        });
    }
}
