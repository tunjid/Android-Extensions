package com.tunjid.androidx.recyclerview.indicators

import android.graphics.Canvas
import kotlin.math.max

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

fun Params.totalWidth(itemCount: Int): Float {
    val totalLength = indicatorWidth * itemCount
    val paddingBetweenItems = max(0, itemCount - 1) * indicatorPadding
    return totalLength + paddingBetweenItems
}

fun Params.start(itemCount: Int): Float {
    return horizontalOffset - (totalWidth(itemCount) / 2f)
}