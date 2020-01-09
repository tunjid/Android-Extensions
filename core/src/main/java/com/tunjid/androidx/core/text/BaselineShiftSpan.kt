package com.tunjid.androidx.core.text

import android.text.TextPaint
import android.text.style.MetricAffectingSpan

/**
 * A span that shifts the baseline of the text it's applied to by the specified ratio.
 */
class BaselineShiftSpan(private val ratio: Float) : MetricAffectingSpan() {

    override fun updateDrawState(textPaint: TextPaint) {
        textPaint.baselineShift = textPaint.shift(ratio)
    }

    override fun updateMeasureState(textPaint: TextPaint) {
        textPaint.baselineShift = textPaint.shift(ratio)
    }
}

private fun TextPaint.shift(ratio: Float): Int = baselineShift + (ascent() * ratio).toInt()