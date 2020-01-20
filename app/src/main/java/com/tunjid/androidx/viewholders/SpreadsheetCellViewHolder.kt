package com.tunjid.androidx.viewholders

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.R
import com.tunjid.androidx.model.Cell

class SpreadsheetCellViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.viewholder_spreadsheet_cell, parent, false)
) {

    private var cell: Cell? = null
    private val editText = itemView.findViewById<TextView>(R.id.cell)

    fun bind(cell: Cell) {
        this.cell = cell
        editText.text = cell.text
    }
}