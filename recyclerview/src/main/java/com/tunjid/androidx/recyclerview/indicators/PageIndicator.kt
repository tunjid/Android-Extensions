package com.tunjid.androidx.recyclerview.indicators

import android.graphics.Canvas
import kotlin.math.max

interface PageIndicator {
    fun drawActive(
            canvas: Canvas,
            params: Params,
            index: Int,
            count: Int,
            progress: Float
    )

    fun drawInActive(
            canvas: Canvas,
            params: Params,
            index: Int,
            count: Int,
            progress: Float
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

fun Params.totalWidth(itemCount: Int): Float {
    val totalLength = indicatorWidth * itemCount
    val paddingBetweenItems = max(0, itemCount - 1) * indicatorPadding
    return totalLength + paddingBetweenItems
}

fun Params.start(itemCount: Int): Float = horizontalOffset - (totalWidth(itemCount) / 2f)