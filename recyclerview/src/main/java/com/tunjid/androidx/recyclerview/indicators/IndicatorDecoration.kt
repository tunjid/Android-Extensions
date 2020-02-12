package com.tunjid.androidx.recyclerview.indicators

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

class IndicatorDecoration(
        private val horizontalOffset: Float = 0f,
        private val verticalOffset: Float = 0f,
        private val indicatorWidth: Float,
        private val indicatorHeight: Float,
        private val indicatorPadding: Float,
        private val indicator: PageIndicator
) : RecyclerView.ItemDecoration() {

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(canvas, parent, state)
        val itemCount = parent.adapter?.itemCount ?: return

        // center horizontally, calculate width and subtract half from center
        val totalLength = indicatorWidth * itemCount
        val paddingBetweenItems = max(0, itemCount - 1) * indicatorPadding
        val indicatorTotalWidth = totalLength + paddingBetweenItems
        val start = horizontalOffset + ((parent.width - indicatorTotalWidth) / 2f)

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

    private fun drawInactiveIndicators(
            canvas: Canvas,
            indicatorStartX: Float,
            indicatorPosY: Float,
            itemCount: Int
    ) {
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
    ) {
        val itemWidth = indicatorWidth + indicatorPadding

        indicator.drawActive(
                canvas = canvas,
                left = indicatorStartX + itemWidth * highlightPosition,
                top = indicatorPosY,
                displacement = itemWidth,
                width = indicatorWidth,
                height = indicatorPosY,
                progress = progress
        )
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = indicatorHeight.toInt()
    }

    companion object {
        private val DP = Resources.getSystem().displayMetrics.density
    }
}

