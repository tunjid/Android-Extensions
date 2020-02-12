package com.tunjid.androidx.recyclerview.indicators

import android.graphics.Bitmap
import android.graphics.Canvas

class BitmapPageIndicator(
        private val active: Bitmap,
        private val inActive: Bitmap
) : PageIndicator {
    override fun drawInActive(
            canvas: Canvas,
            left: Float,
            top: Float,
            width: Float,
            height: Float
    ) = canvas.drawBitmap(inActive, left, top, null)

    override fun drawActive(
            canvas: Canvas,
            left: Float,
            top: Float,
            displacement: Float,
            width: Float,
            height: Float,
            progress: Float
    ) = canvas.drawBitmap(active, left + (displacement * progress), top, null)
}