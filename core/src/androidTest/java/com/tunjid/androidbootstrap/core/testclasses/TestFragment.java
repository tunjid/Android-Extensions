package com.tunjid.androidbootstrap.core.testclasses;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

/**
 * Test fragment
 * <p>
 * Created by Shemanigans on 4/29/17.
 */
@VisibleForTesting
public class TestFragment extends BaseFragment {

    private static final String STRING_ARG_KEY = "STRING_ARG_KEY";

    public static TestFragment newInstance(String stringArg) {
        TestFragment testFragment = new TestFragment();
        Bundle args = new Bundle();

        args.putString(STRING_ARG_KEY, stringArg);
        testFragment.setArguments(args);

        return testFragment;
    }

    @Override
    public String getStableTag() {
        return super.getStableTag() + getArguments().getString(STRING_ARG_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new TextView(inflater.getContext());
    }
}
