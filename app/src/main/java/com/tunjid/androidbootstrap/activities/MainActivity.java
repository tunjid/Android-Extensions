package com.tunjid.androidbootstrap.activities;

import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseActivity;
import com.tunjid.androidbootstrap.core.view.ViewHider;
import com.tunjid.androidbootstrap.fragments.RouteFragment;
import com.tunjid.androidbootstrap.view.animator.FabExtensionAnimator;
import com.tunjid.androidbootstrap.view.animator.FabExtensionAnimator.GlyphState;
import com.tunjid.androidbootstrap.view.util.ViewUtil;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import static androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener;

public class MainActivity extends BaseActivity {

    public static final int ANIMATTION_DURATION = 200;

    private static final int LEFT_INSET = 0;
    private static final int RIGHT_INSET = 2;
    public static final int TOP_INSET = 1;

    public static int topInset;

    private boolean insetsApplied;
    private int leftInset;
    private int rightInset;

    private ViewHider fabHider;
    private ViewHider toolbarHider;
    private FabExtensionAnimator fabExtensionAnimator;

    private View topInsetView;
    private View bottomInsetView;
    private Toolbar toolbar;
    private MaterialButton fab;
    private ConstraintLayout constraintLayout;

    final FragmentManager.FragmentLifecycleCallbacks fragmentViewCreatedCallback = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull androidx.fragment.app.Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
            if (isInMainFragmentContainer(v)) adjustInsetForFragment(f);
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(fragmentViewCreatedCallback, false);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) showFragment(RouteFragment.newInstance());
    }

    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);

        fab = findViewById(R.id.fab);
        toolbar = findViewById(R.id.toolbar);
        topInsetView = findViewById(R.id.top_inset);
        bottomInsetView = findViewById(R.id.bottom_inset);
        constraintLayout = findViewById(R.id.constraint_layout);

        topInsetView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        toolbarHider = ViewHider.of(toolbar).setDirection(ViewHider.TOP).build();
        fabHider = ViewHider.of(fab).setDirection(ViewHider.BOTTOM).build();
        fabExtensionAnimator = new FabExtensionAnimator(fab);
        fabExtensionAnimator.setExtended(true);

        setSupportActionBar(this.toolbar);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setOnApplyWindowInsetsListener(this.constraintLayout, (view, insets) -> consumeSystemInsets(insets));
    }

    public void toggleToolbar(boolean show) {
        if (show) this.toolbarHider.show();
        else this.toolbarHider.hide();
    }

    public void toggleFab(boolean show) {
        if (show) this.fabHider.show();
        else this.fabHider.hide();
    }

    public void setFabExtended(boolean extended) {
        fabExtensionAnimator.setExtended(extended);
    }

    public void updateFab(GlyphState glyphState) {
        if (this.fabExtensionAnimator != null) this.fabExtensionAnimator.updateGlyphs(glyphState);
    }

    public void setFabClickListener(View.OnClickListener onClickListener) {
        fab.setOnClickListener(onClickListener);
    }

    public boolean isFabExtended() {
        return fabExtensionAnimator.isExtended();
    }

    private boolean isInMainFragmentContainer(View view) {
        View parent = (View) view.getParent();
        return parent != null && parent.getId() == R.id.main_fragment_container;
    }

    private WindowInsetsCompat consumeSystemInsets(WindowInsetsCompat insets) {
        if (this.insetsApplied) return insets;

        topInset = insets.getSystemWindowInsetTop();
        this.leftInset = insets.getSystemWindowInsetLeft();
        this.rightInset = insets.getSystemWindowInsetRight();
        int bottomInset = insets.getSystemWindowInsetBottom();

        ViewUtil.getLayoutParams(this.topInsetView).height = topInset;
        ViewUtil.getLayoutParams(this.bottomInsetView).height = bottomInset;

        adjustInsetForFragment(getCurrentFragment());

        this.insetsApplied = true;
        return insets;
    }

    private void adjustInsetForFragment(Fragment fragment) {
        if (!(fragment instanceof AppBaseFragment)) {return;}

        boolean[] insetState = ((AppBaseFragment) fragment).insetState();
        ViewUtil.getLayoutParams(this.toolbar).topMargin = insetState[TOP_INSET] ? topInset : 0;

        TransitionManager.beginDelayedTransition(constraintLayout, new AutoTransition()
                .excludeChildren(RecyclerView.class, true)
                .excludeChildren(ViewPager.class, true)
                .setDuration(ANIMATTION_DURATION)
        );

        topInsetView.setVisibility(insetState[TOP_INSET] ? View.GONE : View.VISIBLE);
        constraintLayout.setPadding(insetState[LEFT_INSET] ? this.leftInset : 0, 0, insetState[RIGHT_INSET] ? this.rightInset : 0, 0);
    }
}
