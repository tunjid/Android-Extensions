package com.tunjid.androidbootstrap.recyclerview;

import com.tunjid.androidbootstrap.functions.BiFunction;
import com.tunjid.androidbootstrap.functions.Function;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import static androidx.recyclerview.widget.DiffUtil.calculateDiff;

public class Diff<T> {

    public final DiffUtil.DiffResult result;
    public final List<T> cumulative;

   private Diff(DiffUtil.DiffResult result, List<T> cumulative) {
        this.result = result;
        this.cumulative = cumulative;
    }

    @NonNull
    public static <T> Diff<T> calculate(
            List<T> dest,
            List<T> additions,
            BiFunction<List<T>, List<T>, List<T>> accumulator,
            Function<T, Differentiable> diffingFunction) {

        List<T> updated = accumulator.apply(new ArrayList<>(dest), new ArrayList<>(additions));
        return new Diff<>(calculateDiff(new DiffCallback<>(dest, updated, diffingFunction)), updated);
    }

    @NonNull
    public static <T extends Differentiable> Diff<T> calculate(
            List<T> destination,
            List<T> fetched,
            BiFunction<List<T>, List<T>, List<T>> accumulator) {
        return calculate(destination, fetched, accumulator, identity -> identity);
    }
}
