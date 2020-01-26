package com.tunjid.androidx.model

import com.tunjid.androidx.recyclerview.diff.Differentiable

data class Row(
        val index: Int,
        val items: List<Cell>
): Differentiable {
    override val diffId: String get() = index.toString()
}

data class Cell(
        val index: Int,
        val text: CharSequence
): Differentiable {
    override val diffId: String get() = index.toString()
}