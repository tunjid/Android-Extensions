package com.tunjid.androidbootstrap.recyclerview.diff

import androidx.recyclerview.widget.DiffUtil

internal class DiffCallback<T>(
        private val stale: List<T>,
        private val updated: List<T>,
        private val diffFunction: (T) -> Differentiable
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = stale.size

    override fun getNewListSize(): Int = updated.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            using(oldItemPosition, newItemPosition) { stale, current -> stale.diffId == current.diffId }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            using(oldItemPosition, newItemPosition, Differentiable::areContentsTheSame)

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? =
            using(oldItemPosition, newItemPosition, Differentiable::getChangePayload)

    private fun <S> using(oldPosition: Int, newPosition: Int, function: (Differentiable, Differentiable) -> S): S =
            function(diffFunction(stale[oldPosition]), diffFunction(updated[newPosition]))
}
