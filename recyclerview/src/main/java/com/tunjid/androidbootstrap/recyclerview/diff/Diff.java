package com.tunjid.androidbootstrap.recyclerview.diff;

import com.tunjid.androidbootstrap.functions.BiFunction;
import com.tunjid.androidbootstrap.functions.Function;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import static androidx.recyclerview.widget.DiffUtil.calculateDiff;

/**
 * A POJO class containing a {@link androidx.recyclerview.widget.DiffUtil.DiffResult} and list
 * out out of one of the {@link Diff#calculate(List, List, BiFunction, Function)} functions.
 *
 * @param <T>
 */
public class Diff<T> {

    public final DiffUtil.DiffResult result;
    public final List<T> cumulative;

    private Diff(DiffUtil.DiffResult result, List<T> cumulative) {
        this.result = result;
        this.cumulative = cumulative;
    }

    /**
     * Calculates the {@link androidx.recyclerview.widget.DiffUtil.DiffResult} between the source
     * list and the output of the accumulator function applied to the source list and the additions.
     * </p>
     * Regardless of implementation, the accumulator function is pure, it does not mutate the source
     * list or the additions list, and as such is best used for diff calculations
     * in a background thread.
     *
     * @param src             The original source list in the {@link androidx.recyclerview.widget.RecyclerView}
     * @param additions       Input to the accumulator function to mutate the source list
     * @param accumulator     A function that yields a new list for which the diff will be calculated
     * @param diffingFunction A function that determines how items in the lists are differentiated
     *                        from each other
     * @param <T>             Type parameter of the lists
     * @return A POJO containing the result of the diff, along with the output of the accumulator
     * function
     */
    @NonNull
    public static <T> Diff<T> calculate(
            List<T> src,
            List<T> additions,
            BiFunction<List<T>, List<T>, List<T>> accumulator,
            Function<T, Differentiable> diffingFunction) {

        List<T> updated = accumulator.apply(new ArrayList<>(src), new ArrayList<>(additions));
        DiffUtil.DiffResult result = calculateDiff(new DiffCallback<>(new ArrayList<>(src), updated, diffingFunction));
        return new Diff<>(result, updated);
    }

    /**
     * @param src       The original source list in the {@link androidx.recyclerview.widget.RecyclerView}
     * @param additions Input to the accumulator function to mutate the source list
     * @param <T>       Type parameter of the lists
     * @return A POJO containing the result of the diff, along with the output of the accumulator
     * @see #calculate(List, List, BiFunction, Function)
     */
    @NonNull
    public static <T extends Differentiable> Diff<T> calculate(
            List<T> src,
            List<T> additions,
            BiFunction<List<T>, List<T>, List<T>> accumulator) {
        return calculate(src, additions, accumulator, identity -> identity);
    }
}
