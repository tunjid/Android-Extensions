package com.tunjid.androidx.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.opencsv.CSVReader
import com.tunjid.androidx.R
import com.tunjid.androidx.model.Cell
import com.tunjid.androidx.model.Row
import com.tunjid.androidx.toLiveData
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset


class SpreadsheetViewModel(application: Application) : AndroidViewModel(application) {

    val rows = Flowable.fromCallable { readData() }
            .subscribeOn(Schedulers.io())
            .toLiveData()

    private fun readData(): List<Row> {
        val inputStream = getApplication<Application>().resources.openRawResource(R.raw.spreadsheet_data)
        val reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
        val csvReader = CSVReader(reader)
        return try {
            generateSequence { csvReader.readNext() }
                    .mapIndexed { index: Int, list: Array<String> -> Row(index, list.mapIndexed(::Cell)) }
                    .toList()
        } catch (e1: IOException) {
            listOf()
        }
    }
}
