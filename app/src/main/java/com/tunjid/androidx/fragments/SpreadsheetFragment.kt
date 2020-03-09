package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.doOnDetach
import androidx.core.view.get
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.core.content.colorAt
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.text.color
import com.tunjid.androidx.core.text.formatSpanned
import com.tunjid.androidx.core.text.scaleX
import com.tunjid.androidx.databinding.FragmentSpreadsheetChildBinding
import com.tunjid.androidx.databinding.ViewholderSpreadsheetCellBinding
import com.tunjid.androidx.databinding.ViewholderSpreadsheetRowBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.map
import com.tunjid.androidx.model.Cell
import com.tunjid.androidx.model.Row
import com.tunjid.androidx.recyclerview.addScrollListener
import com.tunjid.androidx.recyclerview.horizontalLayoutManager
import com.tunjid.androidx.recyclerview.listAdapterOf
import com.tunjid.androidx.recyclerview.multiscroll.DynamicCellSizer
import com.tunjid.androidx.recyclerview.multiscroll.ExperimentalRecyclerViewMultiScrolling
import com.tunjid.androidx.recyclerview.multiscroll.RecyclerViewMultiScroller
import com.tunjid.androidx.recyclerview.multiscroll.StaticCellSizer
import com.tunjid.androidx.recyclerview.verticalLayoutManager
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderFrom
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.InsetFlags.Companion.NO_BOTTOM
import com.tunjid.androidx.view.util.spring
import com.tunjid.androidx.viewmodels.Sort
import com.tunjid.androidx.viewmodels.SpreadsheetViewModel
import com.tunjid.androidx.viewmodels.routeName
import kotlin.reflect.KMutableProperty0

private typealias Var<T> = KMutableProperty0<T>

//region Parent Fragment
class SpreadSheetParentFragment : AppBaseFragment(R.layout.fragment_spreadsheet_parent) {

    override val insetFlags: InsetFlags = NO_BOTTOM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)
        val pagerAdapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int): Fragment =
                    if (position == 0) SpreadsheetFragment.newInstance(true)
                    else SpreadsheetFragment.newInstance(false)
        }

        viewPager.isUserInputEnabled = false
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(view.findViewById(R.id.tabs), viewPager) { tab, position ->
            tab.text = context?.getString(if (position == 0) R.string.dynamic_cells else R.string.static_cells)
        }.attach()

        view.doOnDetach {
            val recyclerView = viewPager[0] as RecyclerView
            pagerAdapter.onDetachedFromRecyclerView(recyclerView)
        }
    }

    companion object {
        fun newInstance(): SpreadSheetParentFragment = SpreadSheetParentFragment().apply { arguments = Bundle() }
    }
}
//endregion

//region Inner Fragment
@UseExperimental(ExperimentalRecyclerViewMultiScrolling::class)
class SpreadsheetFragment : AppBaseFragment(R.layout.fragment_spreadsheet_child) {

    private var isDynamic by args<Boolean>()

    private val viewModel by viewModels<SpreadsheetViewModel>()

    private val scroller by lazy {
        RecyclerViewMultiScroller(cellSizer = when {
            isDynamic -> DynamicCellSizer()
            else -> StaticCellSizer(sizeLookup = this@SpreadsheetFragment::staticSizeAt)
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName,
                toolbarShows = true,
                toolBarMenu = 0,
                fabShows = false,
                showsBottomNav = true,
                lightStatusBar = !requireContext().isDarkTheme,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color)
        )

        val binding = FragmentSpreadsheetChildBinding.bind(view)
        val container = binding.container
        val viewPool = RecyclerView.RecycledViewPool()

        val rowLiveData = viewModel.rows
        val visibleRows = mutableListOf<BindingViewHolder<ViewholderSpreadsheetRowBinding>>()

        scroller.addDisplacementListener { displacement -> visibleRows.forEach { it.onScrolled(displacement) } }

        val stickyHeader = container.rowViewHolder(viewPool, scroller, viewModel::sort).apply {
            container.addView(itemView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT))
            rowLiveData.map(List<Row>::header).observe(viewLifecycleOwner, this::bind)
        }

        binding.mainRows.apply {
            val verticalLayoutManager = verticalLayoutManager()
            val tableAdapter = listAdapterOf(
                    initialItems = rowLiveData.value ?: listOf(),
                    viewHolderCreator = { parent, _ -> parent.rowViewHolder(viewPool, scroller, viewModel::sort) },
                    viewHolderBinder = { viewHolder, row, _ -> viewHolder.bind(row) },
                    itemIdFunction = { it.index.toLong() },
                    onViewHolderAttached = { visibleRows.add(it) },
                    onViewHolderDetached = { visibleRows.remove(it) },
                    onViewHolderRecycled = { visibleRows.remove(it) }
            )

            itemAnimator = null
            layoutManager = verticalLayoutManager
            adapter = tableAdapter

            rowLiveData.observe(viewLifecycleOwner, tableAdapter::submitList)

            addScrollListener { _, _ ->
                stickyHeader.itemView.visibility =
                        if (verticalLayoutManager.findFirstCompletelyVisibleItemPosition() > 0) View.VISIBLE
                        else View.INVISIBLE
            }
            addItemDecoration(tableDecoration())
        }
    }

    private fun RecyclerView.tableDecoration() =
            DividerItemDecoration(context, RecyclerView.VERTICAL).apply {
                setDrawable(context.drawableAt(R.drawable.bg_cell_divider)!!)
            }

    private fun staticSizeAt(position: Int) = requireContext().resources.getDimensionPixelSize(when (position) {
        0, 1, 3 -> R.dimen.septuple_margin
        else -> R.dimen.sexdecuple_margin
    })

    override fun onDestroyView() {
        super.onDestroyView()
        scroller.clear()
    }

    companion object {
        fun newInstance(isDynamic: Boolean): SpreadsheetFragment = SpreadsheetFragment().apply { this.isDynamic = isDynamic }
    }
}
//endregion

//region Row properties
private var BindingViewHolder<ViewholderSpreadsheetRowBinding>.sort by BindingViewHolder.Prop<Var<Sort>>()
private var BindingViewHolder<ViewholderSpreadsheetRowBinding>.scroller by BindingViewHolder.Prop<RecyclerViewMultiScroller>()
private val BindingViewHolder<ViewholderSpreadsheetRowBinding>.cells get() = row.otherCells
private var BindingViewHolder<ViewholderSpreadsheetRowBinding>.row by BindingViewHolder.Prop<Row>()

private fun ViewGroup.rowViewHolder(
        recycledViewPool: RecyclerView.RecycledViewPool,
        scroller: RecyclerViewMultiScroller,
        sort: Var<Sort>
) = viewHolderFrom(ViewholderSpreadsheetRowBinding::inflate).apply {
    this.scroller = scroller
    this.sort = sort
    binding.cell.cell.setOnClickListener { if (row.isHeader) this.sort.update(row.idCell) }
    binding.recyclerView.apply {
        itemAnimator = null
        layoutManager = horizontalLayoutManager()
        setRecycledViewPool(recycledViewPool)
    }
}

private fun BindingViewHolder<ViewholderSpreadsheetRowBinding>.bind(row: Row) {
    this.row = row
    binding.cell.cell.bind(row.idCell, sort.get())
    refresh()
}

private fun BindingViewHolder<ViewholderSpreadsheetRowBinding>.onScrolled(displacement: Int) = binding.elevation.spring(DynamicAnimation.ALPHA, SpringForce.STIFFNESS_LOW)
        .animateToFinalPosition(if (displacement > 20) 1F else 0F)

private fun BindingViewHolder<ViewholderSpreadsheetRowBinding>.refresh(): Unit = binding.recyclerView.run {
    // Lazy initialize
    @Suppress("UNCHECKED_CAST")
    val columnAdapter =
            adapter as? ListAdapter<Cell, *>
                    ?: rowAdapter(cells, this@refresh.sort)
                            .also { adapter = it; scroller.add(this) }

    columnAdapter.submitList(cells)
}
//endregion

//region Cell properties
private fun rowAdapter(
        cells: List<Cell>,
        sort: Var<Sort>
) = listAdapterOf(
        initialItems = cells,
        viewHolderCreator = { viewGroup, _ -> cellViewHolder(viewGroup, sort) },
        viewHolderBinder = { holder, item, _ -> holder.bind(item, sort.get()) },
        itemIdFunction = { it.column.toLong() }
)


private var BindingViewHolder<ViewholderSpreadsheetCellBinding>.cell by BindingViewHolder.Prop<Cell>()

private fun cellViewHolder(viewGroup: ViewGroup, sort: Var<Sort>): BindingViewHolder<ViewholderSpreadsheetCellBinding> {
    val viewHolder = viewGroup.viewHolderFrom(ViewholderSpreadsheetCellBinding::inflate)
    viewHolder.itemView.setOnClickListener {
        val cell = viewHolder.cell
        if (!cell.isHeader) return@setOnClickListener
        sort.update(cell)
    }
    return viewHolder
}

private fun BindingViewHolder<ViewholderSpreadsheetCellBinding>.bind(cell: Cell, sort: Sort) {
    this.cell = cell
    binding.cell.bind(cell, sort)
}

private fun TextView.bind(cell: Cell, sort: Sort) {
    text = resources.getString(R.string.cell_formatter).formatSpanned(
            cell.text,
            if (cell.isHeader && cell.column == sort.column)
                (if (sort.ascending) UP else DOWN)
                        .scaleX(1.4f)
                        .color(context.colorAt(R.color.dark_grey))
            else ""
    )
}
//endregion

private fun Var<Sort>.update(cell: Cell) {
    val currentSort = get()
    val ascending =
            if (currentSort.column == cell.column) !currentSort.ascending
            else currentSort.ascending
    set(Sort(column = cell.column, ascending = ascending))
}

const val UP = "  ▲"
const val DOWN = "  ▼"

private val List<Row>.header get() = this.first()