package com.tunjid.androidbootstrap.baseclasses;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.Toolbar;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseActivity;
import com.tunjid.androidbootstrap.core.view.ViewHider;

/**
 * Base Activty for the app sample
 * <p>
 * Created by tj.dahunsi on 5/20/17.
 */

public class AppBaseActivity extends BaseActivity {

    private ViewHider toolbarHider;
    protected Toolbar toolbar;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarHider = new ViewHider(toolbar, ViewHider.TOP);
        setSupportActionBar(toolbar);
    }

    public void toogleToolbar(boolean show) {
        if (show) toolbarHider.show();
        else toolbarHider.hide();
    }
}
