package com.tunjid.androidbootstrap.recyclerview;

import java.util.List;

import androidx.recyclerview.widget.DiffUtil;

public class Diff<T> {

    public final DiffUtil.DiffResult result;
    public final List<T> cumulative;

    Diff(DiffUtil.DiffResult result, List<T> cumulative) {
        this.result = result;
        this.cumulative = cumulative;
    }
}
