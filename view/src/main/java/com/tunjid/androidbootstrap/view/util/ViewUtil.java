package com.tunjid.androidbootstrap.view.util;

import android.view.View;
import android.view.ViewGroup;

public class ViewUtil {

    public static ViewGroup.MarginLayoutParams getLayoutParams(View view) {
        return (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    }
}
