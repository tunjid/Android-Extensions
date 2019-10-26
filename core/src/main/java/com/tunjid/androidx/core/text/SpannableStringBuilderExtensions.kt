package com.tunjid.androidx.core.text

import android.text.SpannableStringBuilder
import android.text.TextPaint
import androidx.annotation.ColorInt

fun SpannableStringBuilder.color(@ColorInt color: Int) = apply {
    replace(0, length, SpanBuilder(this).color(color).build())
}

fun SpannableStringBuilder.scale(scale: Float) = apply {
    replace(0, length, SpanBuilder(this).scale(scale).build())
}

fun SpannableStringBuilder.bold() = apply {
    replace(0, length, SpanBuilder(this).bold().build())
}

fun SpannableStringBuilder.italic() = apply {
    replace(0, length, SpanBuilder(this).italic().build())
}

fun SpannableStringBuilder.underline() = apply {
    replace(0, length, SpanBuilder(this).underline().build())
}

fun SpannableStringBuilder.click(paintConsumer: (TextPaint) -> Unit = {}, clickAction: () -> Unit) = apply {
    replace(0, length, SpanBuilder(this).click(paintConsumer, clickAction).build())
}

fun SpannableStringBuilder.appendNewLine() = apply {
    replace(0, length, SpanBuilder(this).appendNewLine().build())
}

fun SpannableStringBuilder.format(vararg args: Any) = apply {
    replace(0, length, SpanBuilder.format(this, *args))
}