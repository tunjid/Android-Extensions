package com.tunjid.androidx.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.tunjid.androidx.model.Cell
import com.tunjid.androidx.model.Row

class SpreadsheetViewModel(application: Application) : AndroidViewModel(application) {

    val rows by lazy {
        (0 until NUM_ROWS).map { rowIndex ->
            Row(rowIndex, (0 until NUM_ROWS).map { columnIndex -> Cell(columnIndex, "$rowIndex-$columnIndex") })
        }
    }

    companion object {

        const val NUM_ROWS = 30
    }
}
