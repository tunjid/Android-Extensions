package com.tunjid.androidbootstrap.fragments;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.DoggoPagerAdapter;
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.model.Doggo;
import com.tunjid.androidbootstrap.view.animator.FabExtensionAnimator.GlyphState;
import com.tunjid.androidbootstrap.view.animator.ViewPagerIndicatorAnimator;
import com.tunjid.androidbootstrap.view.util.InsetFlags;
import com.tunjid.androidbootstrap.view.util.ViewUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.SharedElementCallback;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
        Resources resources = getResources();
        int indicatorSize = resources.getDimensionPixelSize(R.dimen.single_and_half_margin);

        viewPager = root.findViewById(R.id.view_pager);
        viewPager.setAdapter(new DoggoPagerAdapter(Doggo.doggos, getChildFragmentManager()));
        viewPager.setCurrentItem(Doggo.getTransitionIndex());
        viewPager.addOnPageChangeListener(new SimpleOnPageChangeListener() {
            public void onPageSelected(int position) { onDoggoSwiped(position); }
        });

        ViewPagerIndicatorAnimator indicatorAnimator = ViewPagerIndicatorAnimator.builder()
                .setIndicatorWidth(indicatorSize)
                .setIndicatorHeight(indicatorSize)
                .setIndicatorPadding(resources.getDimensionPixelSize(R.dimen.half_margin))
                .setInActiveDrawable(R.drawable.ic_circle_24dp)
                .setActiveDrawable(R.drawable.ic_doggo_24dp)
                .setGuideLine(root.findViewById(R.id.guide))
                .setContainer((ConstraintLayout) root)
                .setViewPager(viewPager)
                .build();

        indicatorAnimator.addIndicatorWatcher((indicator, position, fraction, totalTranslation) -> {
            double radians = Math.PI * fraction;
            float sine = (float) -Math.sin(radians);
            float cosine = (float) Math.cos(radians);
            float maxScale = Math.max(Math.abs(cosine), 0.4F);

            ImageView currentIndicator = indicatorAnimator.getIndicatorAt(position);
            currentIndicator.setScaleX(maxScale);
            currentIndicator.setScaleY(maxScale);
            indicator.setTranslationY(indicatorSize * sine);
        });

        prepareSharedElementTransition();
        tintView(R.color.black, root, (color, view) -> view.setBackgroundColor(color));
        if (savedInstanceState == null) postponeEnterTransition();
        return root;
    }

    private void onDoggoSwiped(int position) {
        Doggo.setTransitionDoggo(Doggo.doggos.get(position));
        togglePersistentUi();
    }

    public InsetFlags insetFlags() { return InsetFlags.NONE; }

    public boolean showsToolBar() { return false; }

    public boolean showsFab() { return true; }

    public GlyphState getFabState() {
        return newState(getDogName(), ContextCompat.getDrawable(requireContext(), R.drawable.ic_hug_24dp));
    }

    @Override
    protected View.OnClickListener getFabClickListener() {
        return view -> showFragment(AdoptDoggoFragment.newInstance(Doggo.getTransitionDoggo()));
    }

    @Nullable
    @SuppressLint({"CommitTransaction"})
    public FragmentTransaction provideFragmentTransaction(BaseFragment to) {
        if (!to.getStableTag().contains(AdoptDoggoFragment.class.getSimpleName())) return null;

        View root = getView();
        if (root == null) return null;

        Doggo doggo = Doggo.getTransitionDoggo();
        if (doggo == null) return null;

        View childRoot = root.findViewWithTag(doggo);
        if (childRoot == null) return null;

        ImageView imageView = childRoot.findViewById(R.id.doggo_image);
        if (imageView == null) return null;

        return requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .addSharedElement(imageView, ViewUtil.transitionName(doggo, imageView));
    }

    private String getDogName() {
        Doggo doggo = Doggo.getTransitionDoggo();
        if (doggo == null) return "";

        String name = doggo.getName().replace(" ", "");
        return getString(R.string.adopt_doggo, name);
    }

    private void prepareSharedElementTransition() {
        Transition baseTransition = baseTransition();
        Transition baseSharedTransition = baseSharedTransition();

        setEnterTransition(baseTransition);
        setExitTransition(baseTransition);
        setSharedElementEnterTransition(baseSharedTransition);
        setSharedElementReturnTransition(baseSharedTransition);

        setEnterSharedElementCallback(new SharedElementCallback() {
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                Fragment currentFragment = (Fragment) Objects.requireNonNull(viewPager.getAdapter()).instantiateItem(viewPager, Doggo.getTransitionIndex());
                View view = currentFragment.getView();
                if (view == null) return;

                sharedElements.put(names.get(0), view.findViewById(R.id.doggo_image));
            }
        });
    }
}
