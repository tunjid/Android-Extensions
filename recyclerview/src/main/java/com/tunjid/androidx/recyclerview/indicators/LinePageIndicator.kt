package com.tunjid.androidx.recyclerview.indicators

import android.graphics.Canvas
import android.graphics.Paint

class LinePageIndicator(
    private val activePaint: Paint,
    private val inActivePaint: Paint
) : PageIndicator {
    override fun drawInActive(
        canvas: Canvas,
        params: Params,
        index: Int,
        count: Int,
        progress: Float
    ) {
        val start = params.start(count)
        for (i in 0 until count) drawLineAtIndex(start, params, i, canvas, inActivePaint)
    }

    override fun drawActive(
        canvas: Canvas,
        params: Params,
        index: Int,
        count: Int,
        progress: Float
    ) {
        val start = params.start(count)

        // no swipe, draw a normal indicator
        if (progress == 0f) return drawLineAtIndex(start, params, index, canvas, activePaint)

        val x1 = start + (params.width * index) + (params.width * progress)
        val x2 = x1 + params.indicatorWidth
        val y = params.verticalOffset

        canvas.drawLine(x1, y, x2, y, activePaint)
    }

    private fun drawLineAtIndex(start: Float, params: Params, i: Int, canvas: Canvas, paint: Paint) {
        val x1 = start + (params.width * i)
        val x2 = x1 + params.indicatorWidth
        val y = params.verticalOffset
        canvas.drawLine(x1, y, x2, y, paint)
    }
}


//var highlightStart = indicatorStartX + itemWidth * highlightPosition
//calculate partial highlight
//
//
//val partialLength = width * progress
//// draw the cut off highlight
//canvas.drawLine(
//left + partialLength,
//top,
//left + width,
//top,
//paint
//)
//
//
//draw the highlight overlapping to the next item as well
//if (highlightPosition < itemCount - 1) {
//
//    if (true) {
//        canvas.drawLine(
//                left + width,
//                top,
//                left + width + partialLength,
//                top,
//                paint
//        )
//    }
//}