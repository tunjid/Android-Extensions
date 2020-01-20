package com.tunjid.androidx.viewholders

import android.os.Trace
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.R
import com.tunjid.androidx.model.Row
import com.tunjid.androidx.recyclerview.RecyclerViewMultiScroller
import com.tunjid.androidx.recyclerview.horizontalLayoutManager
import com.tunjid.androidx.recyclerview.listAdapterOf

class SpreadsheetRowViewHolder(
        parent: ViewGroup,
        scroller: RecyclerViewMultiScroller,
        recycledViewPool: RecyclerView.RecycledViewPool
) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.viewholder_spreadsheet_row, parent, false)
) {

    private var row: Row? = null
    private val refresh: () -> Unit

    init {
        val adapter = listAdapterOf(
                initialItems = items(),
                viewHolderCreator = { viewGroup, _ -> SpreadsheetCellViewHolder(viewGroup) },
                viewHolderBinder = { holder, item, _ -> holder.bind(item) },
                itemIdFunction = { it.index.toLong() }
        )
        refresh = { adapter.submitList(items()) }
        itemView.findViewById<RecyclerView>(R.id.recycler_view).apply {
            this.adapter = adapter
            this.layoutManager = horizontalLayoutManager()
            setRecycledViewPool(recycledViewPool)
            scroller.add(this)
        }
    }

    private fun items() = row?.items ?: listOf()

    fun bind(row: Row) {
        Trace.beginSection("Binding Spreadheet Row")
        this.row = row
        refresh()
        Trace.endSection()
    }
}