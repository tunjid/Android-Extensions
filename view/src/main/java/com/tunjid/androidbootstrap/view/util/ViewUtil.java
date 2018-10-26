package com.tunjid.androidbootstrap.view.util;

import android.view.View;
import android.view.ViewGroup;

public class ViewUtil {

    public static ViewGroup.MarginLayoutParams getLayoutParams(View view) {
        return (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    }

    public static String transitionName(Object object, View view) {
        return object.hashCode() + "-" + view.getId();
    }
}
