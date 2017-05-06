package com.tunjid.androidbootstrap.core.testclasses;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
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

    private static final String TAG = TestFragment.class.getSimpleName();
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Created TestFragment with tag: " + getStableTag());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "Destroying view of TestFragment with tag: " + getStableTag());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new TextView(inflater.getContext());
    }
}
