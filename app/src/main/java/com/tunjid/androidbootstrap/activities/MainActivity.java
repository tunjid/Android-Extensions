package com.tunjid.androidbootstrap.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.view.View;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.baseclasses.AppBaseActivity;
import com.tunjid.androidbootstrap.fragments.ImageDetailFragment;
import com.tunjid.androidbootstrap.fragments.RouteFragment;

public class MainActivity extends AppBaseActivity implements OnApplyWindowInsetsListener {

    private boolean insetsApplied;
    private View insetView;

    final FragmentManager.FragmentLifecycleCallbacks lifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v, Bundle savedInstanceState) {
            String tag = f.getTag();
            boolean isFullscreenFragment = tag != null && tag.contains(ImageDetailFragment.class.getSimpleName());

            insetView.setVisibility(isFullscreenFragment ? View.GONE : View.VISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int color = isFullscreenFragment ? R.color.transparent : R.color.colorPrimaryDark;
                getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity.this, color));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        insetView = findViewById(R.id.inset_view);

        getSupportFragmentManager().registerFragmentLifecycleCallbacks(lifecycleCallbacks, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.content_view), this);
        }
        if (savedInstanceState == null) {
            showFragment(RouteFragment.newInstance());
        }
    }

    @Override
    public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
        if (insetsApplied) return insets;

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) insetView.getLayoutParams();
        params.height = insets.getSystemWindowInsetTop();

        insetsApplied = true;
        return insets;
    }
}
