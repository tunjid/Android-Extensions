package com.tunjid.androidx.model

import com.tunjid.androidx.recyclerview.diff.Differentiable

data class Row(
        val isHeader: Boolean,
        val index: Int,
        val cells: List<Cell>
) : Differentiable {
    override val diffId: String get() = index.toString()
    override fun areContentsTheSame(other: Differentiable): Boolean =
            if (isHeader) false else super.areContentsTheSame(other)
}

data class Cell(
        val isHeader: Boolean,
        val column: Int,
        val text: String
) : Differentiable {
    override val diffId: String get() = column.toString()
    override fun areContentsTheSame(other: Differentiable): Boolean =
            if (isHeader) false else super.areContentsTheSame(other)
}