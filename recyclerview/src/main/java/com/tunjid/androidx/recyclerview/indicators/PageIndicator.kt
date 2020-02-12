package com.tunjid.androidx.recyclerview.indicators

import android.graphics.Canvas

interface PageIndicator {
    fun drawActive(
            canvas: Canvas,
            left: Float,
            top: Float,
            displacement: Float,
            width: Float,
            height: Float,
            progress: Float
    )

    fun drawInActive(
            canvas: Canvas,
            left: Float,
            top: Float,
            width: Float,
            height: Float
    ) = drawActive(
            canvas = canvas,
            left = left,
            top = top,
            displacement = 0f,
            width = width,
            height = height,
            progress = 0f
    )
}