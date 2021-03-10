package com.tunjid.androidx.tablists.tables

import android.widget.TextView
import com.tunjid.androidx.recyclerview.diff.Diffable

enum class TextAlignment(val textViewAlignment: Int) {
    Start(TextView.TEXT_ALIGNMENT_TEXT_START),
    Center(TextView.TEXT_ALIGNMENT_CENTER);
}

interface TitledDiffable : Diffable {
    val title: CharSequence
}

interface Table {
    val sortHeader: Cell.Header
    val header: Row
    val rows: List<Row>
    val sidebar: List<Row>
}

sealed class Row : Diffable {
    abstract val cells: List<Cell>

    data class Item(
        val content: TitledDiffable,
        override val cells: List<Cell>
    ) : Row()

    data class Header(
        val content: TitledDiffable,
        val ascending: Boolean,
        override val cells: List<Cell>
    ) : Row()

    override val diffId: String
        get() = when (this) {
            is Item -> content.diffId
            is Header -> "Header"
        }
}

sealed class Cell(val inHeader: Boolean) : Diffable {

    data class Text(
        val content: TitledDiffable,
        val alignment: TextAlignment = TextAlignment.Center
    ) : Cell(inHeader = false)

    data class Header(
        val content: TitledDiffable,
        val selectedColumn: TitledDiffable,
        val ascending: Boolean = true,
        val alignment: TextAlignment = TextAlignment.Center
    ) : Cell(inHeader = true)

    data class Image(
        val drawableRes: Int
    ) : Cell(inHeader = false)

    override val diffId: String
        get() = when (this) {
            is Text -> content.diffId
            is Header -> content.diffId
            is Image -> drawableRes.toString()
        }

    val text
        get() = when (this) {
            is Text -> content.title
            is Header -> content.title
            is Image -> ""
        }

    val textAlignment
        get() = when (this) {
            is Text -> alignment
            is Header -> alignment
            is Image -> TextAlignment.Center
        }
}

fun String.toTitledDiffable() = object : TitledDiffable {
    override val title: CharSequence
        get() = this@toTitledDiffable
    override val diffId: String
        get() = this@toTitledDiffable
}