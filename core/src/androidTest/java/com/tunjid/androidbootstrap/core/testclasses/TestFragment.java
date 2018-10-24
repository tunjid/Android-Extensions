package com.tunjid.androidbootstrap.core.testclasses;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
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
    private static final String BOOLEAN_ARG_KEY = "BOOLEAN_ARG_KEY";

    public static TestFragment newInstance(String stringArg) {
        return newInstance(stringArg, false);
    }

    public static TestFragment newInstance(String stringArg, boolean handlesBackPress) {
        TestFragment testFragment = new TestFragment();
        Bundle args = new Bundle();

        args.putString(STRING_ARG_KEY, stringArg);
        args.putBoolean(BOOLEAN_ARG_KEY, handlesBackPress);

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

    @Override
    public boolean handledBackPress() {
        return getArguments().getBoolean(BOOLEAN_ARG_KEY);
    }
}
