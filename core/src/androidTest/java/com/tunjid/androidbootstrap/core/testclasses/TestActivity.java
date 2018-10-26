package com.tunjid.androidbootstrap.core.testclasses;

import android.os.Bundle;
import androidx.annotation.IdRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.tunjid.androidbootstrap.core.R;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseActivity;

/**
 * Test Activity
 * <p>
 * Created by tj.dahunsi on 4/29/17.
 */
public class TestActivity extends BaseActivity {

    @IdRes public final int ignoredLayoutId = View.generateViewId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
    }

    private ViewGroup getContentView() {
        FrameLayout parent = new FrameLayout(this);
        FrameLayout ignored = new FrameLayout(this);
        FrameLayout inner = new FrameLayout(this);

        ignored.setId(ignoredLayoutId);
        inner.setId(R.id.main_fragment_container);

        parent.addView(ignored);
        parent.addView(inner);

        return parent;
    }
}
