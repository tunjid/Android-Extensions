package com.tunjid.androidx.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.opencsv.CSVReader
import com.tunjid.androidx.R
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
    { rows, sort -> rows.sortedBy(sort) }
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
                    .mapIndexed { index, columns -> Row(index, columns.asList()) }
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

private fun Row.byNumber(sort: Sort) =
        items[sort.column].toIntOrNull() ?: 0

private fun Row.byText(sort: Sort) =
        items[sort.column]

private fun List<Row>.sortedBy(sort: Sort): List<Row> {
    val header = first()
    val body = (this - header).sortedWith(compareBy(
            { it.byNumber(sort) },
            { it.byText(sort) },
            { it.idCell.text.toIntOrNull() ?: 0 }
    ))
    return listOf(header) + if (sort.ascending) body else body.reversed()
}
