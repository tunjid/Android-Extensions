package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnDetach
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.core.content.themeColorAt
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
import com.tunjid.androidx.viewmodels.SpreadsheetViewModel
import com.tunjid.androidx.viewmodels.routeName

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
        val viewPool = RecyclerView.RecycledViewPool()

        val rowLiveData = viewModel.rows

        binding.stickyHeaderRow.apply {
            val cellAdapter = cellAdapter(rowLiveData.value.headers)

            layoutManager = horizontalLayoutManager()
            adapter = cellAdapter

            scroller.add(this)
            rowLiveData.map(List<Row>::headers).observe(viewLifecycleOwner, cellAdapter::submitList)
        }

        binding.mainRows.apply {
            val verticalLayoutManager = verticalLayoutManager()
            val rowAdapter = listAdapterOf(
                    initialItems = rowLiveData.value ?: listOf(),
                    viewHolderCreator = { parent, _ -> parent.spreadSheetRow(viewPool, scroller) },
                    viewHolderBinder = { viewHolder, row, _ -> viewHolder.bind(row) },
                    itemIdFunction = { it.index.toLong() }
            )

            layoutManager = verticalLayoutManager
            adapter = rowAdapter

            rowLiveData.observe(viewLifecycleOwner, rowAdapter::submitList)

            addScrollListener { _, _ ->
                binding.stickyHeaderRow.visibility =
                        if (verticalLayoutManager.findFirstCompletelyVisibleItemPosition() > 0) View.VISIBLE
                        else View.INVISIBLE
            }
        }
    }

    private fun staticSizeAt(position: Int) = requireContext().resources.getDimensionPixelSize(when (position) {
        0 -> R.dimen.triple_and_half_margin
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

private fun ViewGroup.spreadSheetRow(
        recycledViewPool: RecyclerView.RecycledViewPool,
        scroller: RecyclerViewMultiScroller
) = viewHolderFrom(ViewholderSpreadsheetRowBinding::inflate).apply {
    this.scroller = scroller
    binding.recyclerView.apply {
        itemAnimator = null
        layoutManager = horizontalLayoutManager()
        setRecycledViewPool(recycledViewPool)
    }
}

private var BindingViewHolder<ViewholderSpreadsheetRowBinding>.scroller by BindingViewHolder.Prop<RecyclerViewMultiScroller>()
private var BindingViewHolder<ViewholderSpreadsheetRowBinding>.row by BindingViewHolder.Prop<Row?>()
private val BindingViewHolder<ViewholderSpreadsheetRowBinding>.cells get() = row?.cells ?: listOf()

private fun BindingViewHolder<ViewholderSpreadsheetRowBinding>.refresh(): Unit = binding.recyclerView.run {
    // Lazy initialize
    @Suppress("UNCHECKED_CAST")
    val columnAdapter =
            adapter as? ListAdapter<Cell, *>
                    ?: cellAdapter(cells).also { adapter = it; scroller.add(this) }

    columnAdapter.submitList(cells)
}

private fun BindingViewHolder<ViewholderSpreadsheetRowBinding>.bind(row: Row) {
    this.row = row
    refresh()
}

private fun ViewholderSpreadsheetCellBinding.bind(cell: Cell) {
    this.cell.text = cell.text
}

private fun cellAdapter(cells: List<Cell>) = listAdapterOf(
        initialItems = cells,
        viewHolderCreator = { viewGroup, _ -> viewGroup.viewHolderFrom(ViewholderSpreadsheetCellBinding::inflate) },
        viewHolderBinder = { holder, item, _ -> holder.binding.bind(item) },
        itemIdFunction = { it.index.toLong() }
)

private val List<Row>?.headers get() = this?.firstOrNull()?.cells ?: listOf()