package com.tunjid.androidx.tablists.tables

import android.widget.TextView
import com.tunjid.androidx.recyclerview.diff.Diffable

enum class TextAlignment(val textViewAlignment: Int) {
    Start(TextView.TEXT_ALIGNMENT_TEXT_START),
    Center(TextView.TEXT_ALIGNMENT_CENTER);
}

interface RowSubject : Diffable {
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
        val subject: RowSubject,
        override val cells: List<Cell>
    ) : Row()

    data class Header(
        val subject: RowSubject,
        val ascending: Boolean,
        override val cells: List<Cell>
    ) : Row()

    override val diffId: String
        get() = when (this) {
            is Item -> subject.diffId
            is Header -> "Header"
        }
}

sealed class Cell(val inHeader: Boolean) : Diffable {

    data class Stat(
        val type: StatType,
        val value: Int
    ) : Cell(inHeader = false)

    data class Text(
        val text: CharSequence,
        val id: String = text.toString(),
        val alignment: TextAlignment = TextAlignment.Center
    ) : Cell(inHeader = false)

    data class Header(
        val column: RowSubject,
        val selectedColumn: RowSubject,
        val ascending: Boolean = true,
        val alignment: TextAlignment = TextAlignment.Center
    ) : Cell(inHeader = true)

    data class Image(
        val drawableRes: Int
    ) : Cell(inHeader = false)

    override val diffId: String
        get() = when (this) {
            is Stat -> type.letter
            is Text -> id
            is Header -> column.diffId
            is Image -> drawableRes.toString()
        }

    val content
        get() = when (this) {
            is Stat -> value.toString()
            is Text -> text
            is Header -> column.title
            is Image -> ""
        }

    val textAlignment
        get() = when (this) {
            is Stat -> TextAlignment.Center
            is Text -> alignment
            is Header -> alignment
            is Image -> TextAlignment.Center
        }
}