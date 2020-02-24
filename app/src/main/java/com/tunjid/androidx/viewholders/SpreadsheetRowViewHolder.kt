package com.tunjid.androidx.viewholders

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.ViewParent
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.tunjid.androidx.R
import com.tunjid.androidx.model.Row
import com.tunjid.androidx.recyclerview.horizontalLayoutManager
import com.tunjid.androidx.recyclerview.listAdapterOf
import com.tunjid.androidx.recyclerview.multiscroll.ExperimentalRecyclerViewMultiScrolling
import com.tunjid.androidx.recyclerview.multiscroll.RecyclerViewMultiScroller
import kotlin.math.absoluteValue
import kotlin.math.sign

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
        addOnItemTouchListener(NestedScrollingListener(context))

        scroller.add(this)

        val r = { adapter.submitList(items) }
        r
    }

    fun bind(row: Row) {
        this.row = row
        refresh()
    }
}

private class NestedScrollingListener(context: Context) : RecyclerView.SimpleOnItemTouchListener() {

    private var touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var initialX = 0f
    private var initialY = 0f
    private val View.parentViewPager: ViewPager2?
        get() = generateSequence(this as? ViewParent, ViewParent::getParent)
                .filterIsInstance<ViewPager2>()
                .firstOrNull()

    private fun View.canScroll(orientation: Int, delta: Float): Boolean {
        val direction = -delta.sign.toInt()
        return when (orientation) {
            ViewPager2.ORIENTATION_HORIZONTAL -> canScrollHorizontally(direction)
            ViewPager2.ORIENTATION_VERTICAL -> canScrollVertically(direction)
            else -> throw IllegalArgumentException()
        }
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        handleInterceptTouchEvent(rv, e)
        return false
    }

    private fun handleInterceptTouchEvent(view: View, e: MotionEvent) {
        val orientation = view.parentViewPager?.orientation ?: return

        // Early return if child can't scroll in same direction as parent
        if (!view.canScroll(orientation, -1f) && !view.canScroll(orientation, 1f)) {
            return
        }

        if (e.action == MotionEvent.ACTION_DOWN) {
            initialX = e.x
            initialY = e.y
            view.parent.requestDisallowInterceptTouchEvent(true)
        } else if (e.action == MotionEvent.ACTION_MOVE) {
            val dx = e.x - initialX
            val dy = e.y - initialY
            val isVpHorizontal = orientation == ViewPager2.ORIENTATION_HORIZONTAL

            // assuming ViewPager2 touch-slop is 2x touch-slop of child
            val scaledDx = dx.absoluteValue * if (isVpHorizontal) .5f else 1f
            val scaledDy = dy.absoluteValue * if (isVpHorizontal) 1f else .5f

            if (scaledDx > touchSlop || scaledDy > touchSlop) {
                if (isVpHorizontal == (scaledDy > scaledDx)) {
                    // Gesture is perpendicular, allow all parents to intercept
                    view.parent.requestDisallowInterceptTouchEvent(false)
                } else {
                    // Gesture is parallel, query child if movement in that direction is possible
                    if (view.canScroll(orientation, if (isVpHorizontal) dx else dy)) {
                        // Child can scroll, disallow all parents to intercept
                        view.parent.requestDisallowInterceptTouchEvent(true)
                    } else {
                        // Child cannot scroll, allow all parents to intercept
                        view.parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
            }
        }
    }
}
