package com.tunjid.androidbootstrap.view.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.LayoutRes;

public class ViewUtil {

    public static View getItemView(@LayoutRes int res, ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(res, parent, false);
    }

    public static ViewGroup.MarginLayoutParams getLayoutParams(View view) {
        return (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    }

    public static String transitionName(Object object, View view) {
        return object.hashCode() + "-" + view.getId();
    }

    public static void listenForLayout(View view, Runnable onLayout) {
        ViewTreeObserver observer = view.getViewTreeObserver();
        if (!observer.isAlive()) return;
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override public void onGlobalLayout() {
                if (observer.isAlive()) observer.removeOnGlobalLayoutListener(this);
                ViewTreeObserver current = view.getViewTreeObserver();
                if (current.isAlive()) current.removeOnGlobalLayoutListener(this);
                onLayout.run();
            }
        });
    }
}
