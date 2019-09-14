package com.tunjid.androidbootstrap.recyclerview.diff

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.calculateDiff
import java.util.*

/**
 * A POJO class containing a [androidx.recyclerview.widget.DiffUtil.DiffResult] and list
 * out out of one of the [Diff.calculate] functions.
 *
 * @param <T>
</T> */
class Diff<T> private constructor(val result: DiffUtil.DiffResult, val items: List<T>) {
    companion object {

        /**
         * Calculates the [androidx.recyclerview.widget.DiffUtil.DiffResult] between the source
         * list and the output of the combiner function applied to the source list and the additions.
         *
         * Regardless of implementation, the combiner function is pure, it does not mutate the source
         * list or the additions list, and as such is best used for diff calculations
         * in a background thread.
         *
         * @param src             The original source list in the [androidx.recyclerview.widget.RecyclerView]
         * @param additions       Input to the combiner function to mutate the source list
         * @param combiner     A function that yields a new list for which the diff will be calculated
         * @param diffingFunction A function that determines how items in the lists are differentiated
         * from each other
         * @param <T>             Type parameter of the lists
         * @return A POJO containing the result of the diff, along with the output of the combiner
         * function
        </T> */
        fun <T> calculate(
                src: List<T>,
                additions: List<T>,
                combiner: (List<T>, List<T>) -> List<T>,
                diffingFunction: (T) -> Differentiable): Diff<T> {

            val updated = combiner(ArrayList(src), ArrayList(additions))
            val result = calculateDiff(DiffCallback(ArrayList(src), updated, diffingFunction))
            return Diff(result, updated)
        }

        /**
         * @param src       The original source list in the [androidx.recyclerview.widget.RecyclerView]
         * @param additions Input to the combiner function to mutate the source list
         * @param <T>       Type parameter of the lists
         * @return A POJO containing the result of the diff, along with the output of the combiner
         * @see .calculate
        </T> */
        fun <T : Differentiable> calculate(
                src: List<T>,
                additions: List<T>,
                combiner: (List<T>, List<T>) -> List<T>): Diff<T> {
            return calculate(src, additions, combiner, { identity -> identity })
        }
    }
}
