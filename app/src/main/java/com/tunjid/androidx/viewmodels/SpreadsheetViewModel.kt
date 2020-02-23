package com.tunjid.androidx.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.tunjid.androidx.model.Cell
import com.tunjid.androidx.model.Doggo
import com.tunjid.androidx.model.Row

class SpreadsheetViewModel(application: Application) : AndroidViewModel(application) {

    val rows by lazy {
        (0 until NUM_ROWS).map { rowIndex ->
            Row(rowIndex, (0 until NUM_ROWS).map { columnIndex ->
                Cell(
                        columnIndex,
                        if (columnIndex == 0) rowIndex.toString()
                        else "$columnIndex-$rowIndex: ${Doggo.doggos.map(Doggo::name).random()}"
                )
            })
        }
    }

    companion object {

        const val NUM_ROWS = 40
    }
}
