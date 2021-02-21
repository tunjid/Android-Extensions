package com.tunjid.androidx.tablists.tables

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.opencsv.CSVReader
import com.tunjid.androidx.R
import com.tunjid.androidx.toLiveData
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import kotlin.math.max


class SpreadsheetViewModel(application: Application) : AndroidViewModel(application) {

    private val processor = BehaviorProcessor.create<Cell.Header>()

    val state = Single.fromCallable { readData() }
        .subscribeOn(Schedulers.io())
        .flatMapPublisher { personnel ->
            processor.scan(personnel) { next, header ->
                next.copy(sortHeader = next.sortHeader.copy(
                    selectedColumn = header.content,
                    ascending = header.ascending
                ))
            }
        }
        .toLiveData()

    fun accept(input: Cell.Header) = processor.onNext(input)

    private fun readData(): Personnel {
        val inputStream = getApplication<Application>().resources.openRawResource(R.raw.spreadsheet_data)
        val reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
        val csvReader = CSVReader(reader)
        return Personnel(
            try {
                generateSequence(csvReader::readNext).toList()
            } catch (e1: IOException) {
                listOf()
            }
        )
    }
}

data class Personnel(
    private val personnel: List<Array<String>>,
    override val sortHeader: Cell.Header = Cell.Header(
        content = personnel.first().first().toTitledDiffable(),
        selectedColumn = personnel.first().first().toTitledDiffable(),
        ascending = true
    ),
) : Table {

    private val comparator by lazy {
        val sortColumn = max(personnel.first().indexOf(sortHeader.selectedColumn.diffId), 0)
        object : Comparator<Array<String>> {
            private val delegate = compareBy<Array<String>>(
                { columns -> columns[sortColumn].toIntOrNull() ?: 0 },
                { columns -> columns[sortColumn] },
            )

            override fun compare(a: Array<String>, b: Array<String>): Int {
                val comparison = delegate.compare(a, b)
                return if (sortHeader.ascending) comparison else -comparison
            }
        }
    }

    override val header: Row = Row.Header(
        content = Id(0),
        ascending = sortHeader.ascending,
        cells = personnel.first().map { column ->
            Cell.Header(
                content = column.toTitledDiffable(),
                ascending = sortHeader.ascending,
                selectedColumn = sortHeader.selectedColumn,
                alignment = TextAlignment.Start
            )
        }
    )

    override val rows: List<Row> = listOf(header) + personnel.drop(1)
        .sortedWith(comparator)
        .map { columns ->
            Row.Item(
                content = Id(id = columns.first().toInt()),
                cells = columns.map { text ->
                    Cell.Text(
                        content = text.toTitledDiffable(),
                        alignment = TextAlignment.Start
                    )
                }
            )
        }

    override val sidebar = rows.map {
        when (it) {
            is Row.Item -> it.copy(cells = it.cells.take(1))
            is Row.Header -> it.copy(cells = it.cells.take(1))
        }
    }
}

private data class Id(val id: Int) : TitledDiffable {
    override val title: CharSequence get() = id.toString()
    override val diffId: String get() = id.toString()
}
