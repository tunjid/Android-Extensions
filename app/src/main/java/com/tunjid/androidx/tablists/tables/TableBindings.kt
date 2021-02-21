package com.tunjid.androidx.tablists.tables

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.springAnimationOf
import androidx.dynamicanimation.animation.withSpringForceProperties
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.MaterialShapeDrawable
import com.squareup.picasso.Picasso
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.colorAt
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.databinding.ViewgroupTableBinding
import com.tunjid.androidx.databinding.ViewholderBadgeBinding
import com.tunjid.androidx.databinding.ViewholderHeaderCellBinding
import com.tunjid.androidx.databinding.ViewholderSpreadsheetCellBinding
import com.tunjid.androidx.databinding.ViewholderStandingsRowBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.mapDistinct
import com.tunjid.androidx.recyclerview.addScrollListener
import com.tunjid.androidx.recyclerview.horizontalLayoutManager
import com.tunjid.androidx.recyclerview.listAdapterOf
import com.tunjid.androidx.recyclerview.multiscroll.CellSizer
import com.tunjid.androidx.recyclerview.multiscroll.RecyclerViewMultiScroller
import com.tunjid.androidx.recyclerview.multiscroll.StaticCellSizer
import com.tunjid.androidx.recyclerview.verticalLayoutManager
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.recyclerview.viewbinding.typed
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderDelegate
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderFrom

fun ViewgroupTableBinding.bind(
    table: LiveData<out Table>,
    columnSizer: () -> CellSizer,
    owner: LifecycleOwner,
    onCellClicked: (Cell) -> Unit
) {
    val sideBar = table.mapDistinct(Table::sidebar)
    val header = table.mapDistinct(Table::header)
    val rows = table.mapDistinct(Table::rows)

    val viewPool = RecyclerView.RecycledViewPool()
    val rowHeight = root.context.resources.getDimensionPixelSize(R.dimen.triple_and_half_margin)
    val verticalScroller = RecyclerViewMultiScroller(
        orientation = RecyclerView.VERTICAL,
        cellSizer = StaticCellSizer(orientation = RecyclerView.VERTICAL) { rowHeight }
    )
    val horizontalScroller = RecyclerViewMultiScroller(cellSizer = columnSizer()).apply {
        val stickyRows = stickyRows
        val sidebarSpringAnimation = stickyRows.stickyElevator()

        addDisplacementListener { displacement ->
            val isStuck = displacement > 0
            sidebarSpringAnimation(isStuck)
        }
    }


    val stickyHeader = tableContainer.rowViewHolder(
        viewPool,
        horizontalScroller,
        onCellClicked
    ).apply {
        tableContainer.addView(itemView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        header.observe(owner, this::bind)
    }

    stickyRows.apply {
        val stickyViewPool = RecyclerView.RecycledViewPool()
        val stickyHorizontalScroller = RecyclerViewMultiScroller(cellSizer = columnSizer())
        val verticalLayoutManager = verticalLayoutManager()
        val tableAdapter = listAdapterOf(
            initialItems = sideBar.value ?: listOf(),
            viewHolderCreator = { parent, _ ->
                parent.rowViewHolder(
                    stickyViewPool,
                    stickyHorizontalScroller,
                    onCellClicked
                )
            },
            viewHolderBinder = { viewHolder, row, _ -> viewHolder.bind(row) },
        )

        itemAnimator = null
        layoutManager = verticalLayoutManager
        adapter = tableAdapter

        sideBar.observe(owner, tableAdapter::submitList)
        verticalScroller.add(this)

        addItemDecoration(tableDecoration())
    }

    mainRows.apply {
        val verticalLayoutManager = verticalLayoutManager()
        val tableAdapter = listAdapterOf(
            initialItems = rows.value ?: listOf(),
            viewHolderCreator = { parent, _ ->
                parent.rowViewHolder(
                    viewPool,
                    horizontalScroller,
                    onCellClicked
                )
            },
            viewHolderBinder = { viewHolder, row, _ -> viewHolder.bind(row) },
        )

        itemAnimator = null
        layoutManager = verticalLayoutManager
        adapter = tableAdapter

        rows.observe(owner, tableAdapter::submitList)
        verticalScroller.add(this)

        val stickyHeaderSpringAnimation = stickyHeader.itemView.stickyElevator()
        addScrollListener { _, _ ->
            val stickyHeaderVisible = verticalLayoutManager.findFirstCompletelyVisibleItemPosition() > 0
            stickyHeaderSpringAnimation(stickyHeaderVisible)
        }
        addItemDecoration(tableDecoration())
    }
}

//region Row properties
private var BindingViewHolder<ViewholderStandingsRowBinding>.scroller by viewHolderDelegate<RecyclerViewMultiScroller>()
private var BindingViewHolder<ViewholderStandingsRowBinding>.cellClicked by viewHolderDelegate<(Cell) -> Unit>()
private var BindingViewHolder<ViewholderStandingsRowBinding>.row by viewHolderDelegate<Row>()
private var BindingViewHolder<ViewholderHeaderCellBinding>.cell by viewHolderDelegate<Cell>()

private fun ViewGroup.rowViewHolder(
    recycledViewPool: RecyclerView.RecycledViewPool,
    scroller: RecyclerViewMultiScroller,
    onCellClicked: (Cell) -> Unit
) = viewHolderFrom(ViewholderStandingsRowBinding::inflate).apply {
    this.scroller = scroller
    this.cellClicked = onCellClicked
    binding.recyclerView.apply {
        itemAnimator = null
        layoutManager = horizontalLayoutManager()
        setRecycledViewPool(recycledViewPool)
    }
}

private fun rowAdapter(
    row: Row,
    onCellClicked: (Cell) -> Unit
) = listAdapterOf(
    initialItems = row.cells,
    viewHolderCreator = { viewGroup, viewType ->
        when (viewType) {
            Cell.Text::class.hashCode() -> viewGroup.viewHolderFrom(ViewholderSpreadsheetCellBinding::inflate)
            Cell.Header::class.hashCode() -> headerViewHolder(viewGroup, onCellClicked)
            Cell.Image::class.hashCode() -> viewGroup.viewHolderFrom(ViewholderBadgeBinding::inflate)
            else -> throw IllegalArgumentException("Unknown view type")
        }
    },
    viewHolderBinder = { holder, item, _ ->
        when (item) {
            is Cell.Text -> holder.typed<ViewholderSpreadsheetCellBinding>().bind(item)
            is Cell.Header -> holder.typed<ViewholderHeaderCellBinding>().bind(item)
            is Cell.Image -> holder.typed<ViewholderBadgeBinding>().apply {
                Picasso.get()
                    .load(item.drawableRes)
                    .into(binding.image)
            }
        }

    },
    viewTypeFunction = { it::class.hashCode() }
)

private fun BindingViewHolder<ViewholderStandingsRowBinding>.bind(row: Row) {
    this.row = row
    val recyclerView = binding.recyclerView

    @Suppress("UNCHECKED_CAST")
    val rowAdapter = when (val adapter = recyclerView.adapter as? ListAdapter<Cell, *>) {
        null -> rowAdapter(row, cellClicked).also { createdAdapter ->
            recyclerView.adapter = createdAdapter
            scroller.add(recyclerView)
        }
        else -> adapter
    }
    rowAdapter.submitList(row.cells)
}

//endregion

//region Cell properties


private fun headerViewHolder(
    viewGroup: ViewGroup,
    onCellClicked: (Cell) -> Unit
): BindingViewHolder<ViewholderHeaderCellBinding> {
    val viewHolder = viewGroup.viewHolderFrom(ViewholderHeaderCellBinding::inflate)
    viewHolder.itemView.setOnClickListener {
        val cell = viewHolder.cell
        if (cell.inHeader) onCellClicked(cell)
    }
    return viewHolder
}

private fun BindingViewHolder<ViewholderHeaderCellBinding>.bind(cell: Cell.Header) {
    this.cell = cell
    val isSelectedHeader = when (cell.content) {
        cell.selectedColumn -> cell.ascending
        else -> null
    }

    binding.cell.text = cell.text
    binding.cell.textAlignment = cell.textAlignment.textViewAlignment
    binding.up.visibility = if (isSelectedHeader == true) View.VISIBLE else View.INVISIBLE
    binding.down.visibility = if (isSelectedHeader == false) View.VISIBLE else View.INVISIBLE

    binding.cell.updateBias(cell)
    binding.up.updateBias(cell)
    binding.down.updateBias(cell)
}

private fun BindingViewHolder<ViewholderSpreadsheetCellBinding>.bind(item: Cell) {
    binding.cell.text = item.text
    binding.cell.textAlignment = item.textAlignment.textViewAlignment
}

private fun RecyclerView.tableDecoration() =
    DividerItemDecoration(context, RecyclerView.VERTICAL).apply {
        setDrawable(context.drawableAt(R.drawable.bg_cell_divider)!!)
    }

private fun View.updateBias(cell: Cell.Header) = updateLayoutParams<ConstraintLayout.LayoutParams> {
    horizontalBias = when (cell.textAlignment) {
        TextAlignment.Start -> 0F
        TextAlignment.Center -> 0.5F
    }
}

private fun View.stickyElevator(): (Boolean) -> Unit {
    val stickyHeaderElevation = context.resources.getDimensionPixelSize(R.dimen.single_margin).toFloat()
    val drawable = MaterialShapeDrawable.createWithElevationOverlay(context, 0f).apply {
        setTint(context.colorAt(R.color.colorSurface))
    }

    background = drawable
    elevation = stickyHeaderElevation

    val springAnimation = springAnimationOf(
        setter = drawable::setElevation,
        getter = drawable::getElevation,
        finalPosition = stickyHeaderElevation
    )
        .withSpringForceProperties {
            stiffness = SpringForce.STIFFNESS_VERY_LOW
            dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
        }
        .addEndListener { _, _, value, _ ->
            if (value == 0f) visibility = View.INVISIBLE
        }

    return { isElevated ->
        if (context.isDarkTheme) {
            if (isElevated) visibility = View.VISIBLE
            springAnimation.animateToFinalPosition(if (isElevated) stickyHeaderElevation else 0F)
        } else visibility = if (isElevated) View.VISIBLE else View.INVISIBLE
    }
}