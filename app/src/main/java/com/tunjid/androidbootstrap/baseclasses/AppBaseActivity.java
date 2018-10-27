package com.tunjid.androidbootstrap.baseclasses;

import androidx.annotation.LayoutRes;
import androidx.appcompat.widget.Toolbar;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseActivity;
import com.tunjid.androidbootstrap.view.animator.ViewHider;

/**
 * Base Activty for the app sample
 * <p>
 * Created by tj.dahunsi on 5/20/17.
 */

public abstract class AppBaseActivity extends BaseActivity {

    private ViewHider toolbarHider;
    protected Toolbar toolbar;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        toolbar = findViewById(R.id.toolbar);
        toolbarHider =  ViewHider.of(toolbar).setDirection(ViewHider.TOP).build();
        setSupportActionBar(toolbar);
    }

    public void toggleToolbar(boolean show) {
        if (show) toolbarHider.show();
        else toolbarHider.hide();
    }
}
