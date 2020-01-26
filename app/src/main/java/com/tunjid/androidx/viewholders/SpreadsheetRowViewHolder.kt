package com.tunjid.androidx.viewholders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.R
import com.tunjid.androidx.model.Row
import com.tunjid.androidx.recyclerview.ExperimentalRecyclerViewMultiScrolling
import com.tunjid.androidx.recyclerview.RecyclerViewMultiScroller
import com.tunjid.androidx.recyclerview.horizontalLayoutManager
import com.tunjid.androidx.recyclerview.listAdapterOf

@UseExperimental(ExperimentalRecyclerViewMultiScrolling::class)
class SpreadsheetRowViewHolder(
        parent: ViewGroup,
        scroller: RecyclerViewMultiScroller,
        recycledViewPool: RecyclerView.RecycledViewPool
) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.viewholder_spreadsheet_row, parent, false)
) {

    private var row: Row? = null
    private val items get() = row?.items ?: listOf()
    private val refresh by lazy { setup(recycledViewPool, scroller) }

    private fun setup(
            recycledViewPool: RecyclerView.RecycledViewPool,
            scroller: RecyclerViewMultiScroller
    ): () -> Unit = itemView.findViewById<RecyclerView>(R.id.recycler_view).run {
        val adapter = listAdapterOf(
                initialItems = items,
                viewHolderCreator = { viewGroup, _ -> SpreadsheetCellViewHolder(viewGroup) },
                viewHolderBinder = { holder, item, _ -> holder.bind(item) },
                itemIdFunction = { it.index.toLong() }
        )
        this.itemAnimator = null
        this.adapter = adapter
        this.layoutManager = horizontalLayoutManager()
        setRecycledViewPool(recycledViewPool)
        scroller.add(this)

        val r = { adapter.submitList(items) }
        r
    }

    fun bind(row: Row) {
        this.row = row
        refresh()
    }
}