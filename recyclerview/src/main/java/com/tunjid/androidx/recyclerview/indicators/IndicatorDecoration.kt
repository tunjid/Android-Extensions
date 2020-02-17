package com.tunjid.androidx.recyclerview.indicators

import android.graphics.Canvas
import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

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
            horizontalOffset,
            verticalOffset,
            indicatorWidth,
            indicatorHeight,
            indicatorPadding
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
    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) = params.run {
        super.onDrawOver(canvas, parent, state)
        val itemCount = parent.adapter?.itemCount ?: return

        val start = params.start(itemCount)

        // center vertically in the allotted space
        drawInactiveIndicators(canvas, start, verticalOffset, itemCount)

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
        drawHighlights(canvas, start, verticalOffset, activePosition, progress)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = params.indicatorHeight.toInt()
    }

    private fun drawInactiveIndicators(
            canvas: Canvas,
            indicatorStartX: Float,
            indicatorPosY: Float,
            itemCount: Int
    ) = params.run {
        // width of item indicator including padding
        val itemWidth = indicatorWidth + indicatorPadding
        var start = indicatorStartX
        repeat(itemCount) {
            indicator.drawInActive(canvas, start, indicatorPosY, indicatorWidth, indicatorPosY)
            start += itemWidth
        }
    }

    private fun drawHighlights(
            canvas: Canvas,
            indicatorStartX: Float,
            indicatorPosY: Float,
            highlightPosition: Int,
            progress: Float
    ) = params.run {
        val itemWidth = indicatorWidth + indicatorPadding

        indicator.drawActive(
                canvas = canvas,
                left = indicatorStartX + itemWidth * highlightPosition,
                top = indicatorPosY,
                displacement = itemWidth,
                width = indicatorWidth,
                height = indicatorHeight,
                progress = progress
        )
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

data class Params(
        val horizontalOffset: Float,
        val verticalOffset: Float,
        val indicatorWidth: Float,
        val indicatorHeight: Float,
        val indicatorPadding: Float
)

val Params.width get() = indicatorWidth + indicatorPadding
val Params.left get() = indicatorWidth + indicatorPadding
val Params.top get() = indicatorWidth + indicatorPadding

private fun Params.start(itemCount: Int): Float{
    val totalLength = indicatorWidth * itemCount
    val paddingBetweenItems = max(0, itemCount - 1) * indicatorPadding
    val indicatorTotalWidth = totalLength + paddingBetweenItems

    return horizontalOffset + ((width - indicatorTotalWidth) / 2f)
}