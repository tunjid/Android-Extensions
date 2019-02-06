package com.tunjid.androidbootstrap.recyclerview;


import com.tunjid.androidbootstrap.functions.BiFunction;
import com.tunjid.androidbootstrap.functions.Function;
import com.tunjid.androidbootstrap.functions.Supplier;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

import static androidx.recyclerview.widget.DiffUtil.calculateDiff;

public interface Differentiable {

    String getId();

    default boolean areContentsTheSame(Differentiable other) { return getId().equals(other.getId()); }

    default Object getChangePayload(Differentiable other) {
        return null;
    }

    @NonNull
    static <T extends Differentiable> Diff<T> diff(
            List<T> destination,
            List<T> fetched,
            BiFunction<List<T>, List<T>, List<T>> accumulator) {
        return diff(destination, fetched, accumulator, identity -> identity);
    }

    @NonNull
    static <T> Diff<T> diff(
            List<T> dest,
            List<T> additions,
            BiFunction<List<T>, List<T>, List<T>> accumulator,
            Function<T, Differentiable> diffingFunction) {

        List<T> updated = accumulator.apply(new ArrayList<>(dest), new ArrayList<>(additions));
        return new Diff<>(calculateDiff(new DiffCallback<>(dest, updated, diffingFunction)), updated);
    }

    static Differentiable fromCharSequence(Supplier<CharSequence> charSequenceSupplier) {
        final String id = charSequenceSupplier.get().toString();

        //noinspection EqualsWhichDoesntCheckParameterClass
        return new Differentiable() {
            @Override
            public boolean equals(Object obj) { return id.equals(obj); }

            @Override
            public int hashCode() { return id.hashCode(); }

            @Override
            public String getId() { return id; }
        };
    }

}
