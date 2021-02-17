package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.springAnimationOf
import androidx.dynamicanimation.animation.withSpringForceProperties
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.MaterialShapeDrawable
import com.squareup.picasso.Picasso
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.colorAt
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.databinding.FragmentStandingsBinding
import com.tunjid.androidx.databinding.ViewholderBadgeBinding
import com.tunjid.androidx.databinding.ViewholderHeaderCellBinding
import com.tunjid.androidx.databinding.ViewholderSpreadsheetCellBinding
import com.tunjid.androidx.databinding.ViewholderStandingsRowBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.mapDistinct
import com.tunjid.androidx.recyclerview.addScrollListener
import com.tunjid.androidx.recyclerview.horizontalLayoutManager
import com.tunjid.androidx.recyclerview.listAdapterOf
import com.tunjid.androidx.recyclerview.multiscroll.DynamicCellSizer
import com.tunjid.androidx.recyclerview.multiscroll.RecyclerViewMultiScroller
import com.tunjid.androidx.recyclerview.multiscroll.StaticCellSizer
import com.tunjid.androidx.recyclerview.verticalLayoutManager
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.recyclerview.viewbinding.typed
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderDelegate
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderFrom
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.uiState
import com.tunjid.androidx.viewmodels.Cell
import com.tunjid.androidx.viewmodels.GameFilter
import com.tunjid.androidx.viewmodels.Row
import com.tunjid.androidx.viewmodels.StandingInput
import com.tunjid.androidx.viewmodels.Standings
import com.tunjid.androidx.viewmodels.StandingsViewModel
import com.tunjid.androidx.viewmodels.routeName

class StandingsFragment : Fragment(R.layout.fragment_standings) {

    private val viewModel by viewModels<StandingsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = UiState(
            toolbarTitle = this::class.java.routeName,
            toolbarShows = true,
            toolbarOverlaps = false,
            toolbarMenuRes = 0,
            fabShows = false,
            showsBottomNav = false,
            lightStatusBar = !requireContext().isDarkTheme,
            navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color)
        )

        val sideBar = viewModel.state.mapDistinct(Standings::sidebar)
        val header = viewModel.state.mapDistinct(Standings::header)
        val rows = viewModel.state.mapDistinct(Standings::rows)

        val binding = FragmentStandingsBinding.bind(view)
        val container = binding.container
        val viewPool = RecyclerView.RecycledViewPool()
        val rowHeight = view.context.resources.getDimensionPixelSize(R.dimen.triple_and_half_margin)
        val verticalScroller = RecyclerViewMultiScroller(
            orientation = RecyclerView.VERTICAL,
            cellSizer = StaticCellSizer(orientation = RecyclerView.VERTICAL) { rowHeight }
        )
        val horizontalScroller = RecyclerViewMultiScroller(cellSizer = DynamicCellSizer()).apply {
            val stickyRows = binding.stickyRows
            val sidebarSpringAnimation = stickyRows.stickyElevator()

            addDisplacementListener { displacement ->
                val isStuck = displacement > 0
                sidebarSpringAnimation(isStuck)
            }
        }

        binding.tabs.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && checkedId != View.NO_ID && host != null && !childFragmentManager.isStateSaved) when (checkedId) {
                R.id.all -> GameFilter.All
                R.id.home -> GameFilter.Home
                R.id.away -> GameFilter.Away
                else -> null
            }
                ?.let(StandingInput::Filter)
                ?.let(viewModel::accept)
        }


        val stickyHeader = container.rowViewHolder(viewPool, horizontalScroller, ::onCellClicked).apply {
            container.addView(itemView, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                topMargin = view.context.resources.getDimensionPixelSize(R.dimen.triple_and_half_margin)
            })
            header.observe(viewLifecycleOwner, this::bind)
        }

        binding.stickyRows.apply {
            val stickyViewPool = RecyclerView.RecycledViewPool()
            val stickyHorizontalScroller = RecyclerViewMultiScroller(cellSizer = DynamicCellSizer())
            val verticalLayoutManager = verticalLayoutManager()
            val tableAdapter = listAdapterOf(
                initialItems = sideBar.value ?: listOf(),
                viewHolderCreator = { parent, _ -> parent.rowViewHolder(stickyViewPool, stickyHorizontalScroller, ::onCellClicked) },
                viewHolderBinder = { viewHolder, row, _ -> viewHolder.bind(row) },
            )

            itemAnimator = null
            layoutManager = verticalLayoutManager
            adapter = tableAdapter

            sideBar.observe(viewLifecycleOwner, tableAdapter::submitList)
            verticalScroller.add(this)

            addItemDecoration(tableDecoration())
        }

        binding.mainRows.apply {
            val verticalLayoutManager = verticalLayoutManager()
            val tableAdapter = listAdapterOf(
                initialItems = rows.value ?: listOf(),
                viewHolderCreator = { parent, _ -> parent.rowViewHolder(viewPool, horizontalScroller, ::onCellClicked) },
                viewHolderBinder = { viewHolder, row, _ -> viewHolder.bind(row) },
            )

            itemAnimator = null
            layoutManager = verticalLayoutManager
            adapter = tableAdapter

            rows.observe(viewLifecycleOwner, tableAdapter::submitList)
            verticalScroller.add(this)

            val stickyHeaderSpringAnimation = stickyHeader.itemView.stickyElevator()
            addScrollListener { _, _ ->
                val stickyHeaderVisible = verticalLayoutManager.findFirstCompletelyVisibleItemPosition() > 0
                stickyHeaderSpringAnimation(stickyHeaderVisible)
            }
            addItemDecoration(tableDecoration())
        }
    }

    private fun onCellClicked(cell: Cell) = when (cell) {
        is Cell.Stat -> Unit
        is Cell.Text -> Unit
        is Cell.Image -> Unit
        is Cell.StatHeader -> viewModel.accept(StandingInput.Sort(cell.copy(ascending = !cell.ascending)))
    }

    private fun RecyclerView.tableDecoration() =
        DividerItemDecoration(context, RecyclerView.VERTICAL).apply {
            setDrawable(context.drawableAt(R.drawable.bg_cell_divider)!!)
        }

    companion object {
        fun newInstance(): StandingsFragment = StandingsFragment()
    }
}
//endregion

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

private fun BindingViewHolder<ViewholderStandingsRowBinding>.bind(row: Row) {
    this.row = row
    refresh()
}

private fun BindingViewHolder<ViewholderStandingsRowBinding>.refresh(): Unit = binding.recyclerView.run {
    // Lazy initialize
    @Suppress("UNCHECKED_CAST")
    val columnAdapter = adapter as? ListAdapter<Cell, *>
        ?: rowAdapter(row, cellClicked).also { adapter = it; scroller.add(this) }

    columnAdapter.submitList(row.cells)
}
//endregion

//region Cell properties
private fun rowAdapter(
    row: Row,
    onCellClicked: (Cell) -> Unit
) = listAdapterOf(
    initialItems = row.cells,
    viewHolderCreator = { viewGroup, viewType ->
        when (viewType) {
            Cell.Stat::class.hashCode() -> viewGroup.viewHolderFrom(ViewholderSpreadsheetCellBinding::inflate)
            Cell.Text::class.hashCode() -> viewGroup.viewHolderFrom(ViewholderSpreadsheetCellBinding::inflate)
            Cell.StatHeader::class.hashCode() -> headerViewHolder(viewGroup, onCellClicked)
            Cell.Image::class.hashCode() -> viewGroup.viewHolderFrom(ViewholderBadgeBinding::inflate)
            else -> throw IllegalArgumentException("Unknown view type")
        }
    },
    viewHolderBinder = { holder, item, _ ->
        when (item) {
            is Cell.Stat -> holder.typed<ViewholderSpreadsheetCellBinding>().bind(item)
            is Cell.Text -> holder.typed<ViewholderSpreadsheetCellBinding>().bind(item)
            is Cell.StatHeader -> holder.typed<ViewholderHeaderCellBinding>().bind(item)
            is Cell.Image -> holder.typed<ViewholderBadgeBinding>().apply {
                Picasso.get()
                    .load(item.drawableRes)
                    .into(binding.image)
            }
        }

    },
    viewTypeFunction = { it::class.hashCode() }
)

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

private fun BindingViewHolder<ViewholderHeaderCellBinding>.bind(cell: Cell.StatHeader) {
    this.cell = cell
    val isSelectedHeader = when (cell.type) {
        cell.selectedType -> cell.ascending
        else -> null
    }

    binding.cell.text = cell.content
    binding.up.visibility = if (isSelectedHeader == true) View.VISIBLE else View.INVISIBLE
    binding.down.visibility = if (isSelectedHeader == false) View.VISIBLE else View.INVISIBLE
}

private fun BindingViewHolder<ViewholderSpreadsheetCellBinding>.bind(item: Cell) {
    binding.cell.text = item.content
    binding.cell.textAlignment = item.textAlignment
}

//endregion

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