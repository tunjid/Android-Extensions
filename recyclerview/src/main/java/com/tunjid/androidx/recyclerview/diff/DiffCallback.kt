package com.tunjid.androidx.recyclerview.diff

import androidx.recyclerview.widget.DiffUtil

internal class DiffCallback<T>(
        private val stale: List<T>,
        private val updated: List<T>,
        private val diffFunction: (T) -> Diffable
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = stale.size

    override fun getNewListSize(): Int = updated.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            using(oldItemPosition, newItemPosition) { stale, current -> stale.diffId == current.diffId }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            using(oldItemPosition, newItemPosition, Diffable::areContentsTheSame)

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? =
            using(oldItemPosition, newItemPosition, Diffable::getChangePayload)

    private fun <S> using(oldPosition: Int, newPosition: Int, function: (Diffable, Diffable) -> S): S =
            function(diffFunction(stale[oldPosition]), diffFunction(updated[newPosition]))
}
