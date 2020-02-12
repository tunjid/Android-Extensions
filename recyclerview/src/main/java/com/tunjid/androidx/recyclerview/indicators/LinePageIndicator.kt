package com.tunjid.androidx.recyclerview.indicators

import android.graphics.Canvas
import android.graphics.Paint

class LinePageIndicator(
       private val activePaint: Paint,
       private val inActivePaint: Paint
) : PageIndicator {
    override fun drawInActive(
            canvas: Canvas,
            left: Float,
            top: Float,
            width: Float,
            height: Float
    ) = canvas.drawLine(left, top, left + width, top, inActivePaint)

    override fun drawActive(
            canvas: Canvas,
            left: Float,
            top: Float,
            displacement: Float,
            width: Float,
            height: Float,
            progress: Float
    ) {
        // no swipe, draw a normal indicator
        if (progress == 0f) return drawInActive(canvas, left, top, width, height)



val offset = left + (displacement * progress)

        canvas.drawLine(
                offset,
                top,
                offset + width,
                top,
                activePaint
        )





//        var highlightStart = indicatorStartX + itemWidth * highlightPosition
        // calculate partial highlight






//        val partialLength = width * progress
//        // draw the cut off highlight
//        canvas.drawLine(
//                left + partialLength,
//                top,
//                left + width,
//                top,
//                paint
//        )


        // draw the highlight overlapping to the next item as well
//        if (highlightPosition < itemCount - 1) {

//        if (true) {
//            canvas.drawLine(
//                    left + width,
//                    top,
//                    left + width + partialLength,
//                    top,
//                    paint
//            )
//        }
    }
}