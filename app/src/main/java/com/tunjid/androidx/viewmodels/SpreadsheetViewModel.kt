package com.tunjid.androidx.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.opencsv.CSVReader
import com.tunjid.androidx.R
import com.tunjid.androidx.model.Cell
import com.tunjid.androidx.model.Row
import com.tunjid.androidx.toLiveData
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.rxkotlin.Flowables
import io.reactivex.schedulers.Schedulers
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset


class SpreadsheetViewModel(application: Application) : AndroidViewModel(application) {

    private val sortProcessor = BehaviorProcessor.create<Sort>().apply {
        onNext(Sort(column = 0, ascending = true))
    }

    val rows = Flowables.combineLatest(Flowable.fromCallable { readData() }, sortProcessor)
    { rows, sort -> rows.sortedWith(sort) }
            .subscribeOn(Schedulers.io())
            .toLiveData()

    var sort
        get() = sortProcessor.value!!
        set(value) = sortProcessor.onNext(value)

    private fun readData(): List<Row> {
        val inputStream = getApplication<Application>().resources.openRawResource(R.raw.spreadsheet_data)
        val reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
        val csvReader = CSVReader(reader)
        return try {
            generateSequence { csvReader.readNext() }
                    .mapIndexed { index, columns ->
                        val isHeader = columns.isHeader
                        Row(isHeader, index, columns.mapIndexed { column, text ->
                            Cell(isHeader, column, text)
                        })
                    }
                    .toList()
        } catch (e1: IOException) {
            listOf()
        }
    }
}

data class Sort(
        val column: Int,
        val ascending: Boolean
)

private val Array<String>.isHeader get() = first() == "Id"

private val Row.headerValue
    get() =
        if (cells.first().isHeader) -1
        else 1

private fun Row.byNumber(sort: Sort) =
        cells[sort.column].text.toIntOrNull() ?: 0

private fun Row.byText(sort: Sort) =
        cells[sort.column].text

private inline fun <T : Comparable<T>> Sort.check(
        invert: Boolean = false,
        left: Row,
        right: Row,
        selector: Row.(sort: Sort) -> T
): Int? {
    val leftValue = selector(left, this)
    val rightValue = selector(right, this)
    return (leftValue.compareTo(rightValue) * if (invert && !this.ascending) -1 else 1)
            .takeIf { it != 0 }
}

private fun List<Row>.sortedWith(sort: Sort) = this.sortedWith(Comparator { row1: Row, row2: Row ->
    return@Comparator sort.check(left = row1, right = row2) { headerValue }
            ?: sort.check(invert = true, left = row1, right = row2, selector = Row::byNumber)
            ?: sort.check(invert = true, left = row1, right = row2, selector = Row::byText)
            ?: 0
})
