package com.tunjid.androidx.recyclerview.indicators

import android.graphics.Canvas
import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.indicatorDecoration(
    horizontalOffset: Float = 0f,
    verticalOffset: Float = 0f,
    indicatorWidth: Float,
    indicatorHeight: Float,
    indicatorPadding: Float,
    indicator: PageIndicator,
    onIndicatorClicked: ((Int) -> Unit)? = null
): () -> Unit {
    val params = Params(
        horizontalOffset = horizontalOffset,
        verticalOffset = verticalOffset,
        indicatorWidth = indicatorWidth,
        indicatorHeight = indicatorHeight,
        indicatorPadding = indicatorPadding
    )

    val decoration = IndicatorDecoration(indicator, params)
    val clickListener = IndicatorClickListener(this, params, onIndicatorClicked)

    addItemDecoration(decoration)
    addOnItemTouchListener(clickListener)

    return {
        removeItemDecoration(decoration)
        removeOnItemTouchListener(clickListener)
    }
}

private class IndicatorDecoration(
    private val indicator: PageIndicator,
    private val params: Params
) : RecyclerView.ItemDecoration() {
    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(canvas, parent, state)
        val itemCount = parent.adapter?.itemCount ?: return

        // find active page (which should be highlighted)
        val layoutManager = parent.layoutManager as? LinearLayoutManager ?: return
        val activePosition = layoutManager.findFirstVisibleItemPosition()
        if (activePosition == RecyclerView.NO_POSITION) return

        // find offset of active page (if the user is scrolling)

        val activeChild = layoutManager.findViewByPosition(activePosition) ?: return
        val left = activeChild.left
        val width = activeChild.width

        // on swipe the active item will be positioned from [-width, 0]
        // interpolate offset for smooth animation

        val progress = left * -1 / width.toFloat()

        indicator.drawInActive(canvas = canvas, params = params, index = activePosition, count = itemCount, progress = progress)
        indicator.drawActive(canvas = canvas, params = params, index = activePosition, count = itemCount, progress = progress)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = params.indicatorHeight.toInt()
    }
}

private class IndicatorClickListener(
    recyclerView: RecyclerView,
    private val params: Params,
    private val onIndicatorClicked: ((Int) -> Unit)?
) : RecyclerView.SimpleOnItemTouchListener() {

    private val gestureDetector: GestureDetector = GestureDetector(recyclerView.context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent?): Boolean = true
    })

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean = params.run {
        val x = e.x
        val y = e.y

        if (y < params.verticalOffset || y > (verticalOffset + indicatorHeight)) return false

        val itemCount = rv.adapter?.itemCount ?: return false
        val start = params.start(itemCount)

        for (i in 0 until itemCount) {
            val x1 = start + (i * (indicatorWidth + indicatorPadding))
            val x2 = x1 + indicatorWidth

            if (x1 >= x || x >= x2 || !gestureDetector.onTouchEvent(e)) continue

            onIndicatorClicked?.invoke(i)
            break
        }

        return false
    }
}
