package com.tunjid.androidx.viewholders

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.text.TextUtils.isEmpty
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import androidx.core.widget.doAfterTextChanged
import com.tunjid.androidx.R
import com.tunjid.androidx.databinding.ViewholderSimpleInputBinding
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderFrom

fun ViewGroup.inputViewHolder() = viewHolderFrom(ViewholderSimpleInputBinding::inflate).apply {
    lastLineCount = 1

    binding.input.setOnFocusChangeListener { _, hasFocus ->
        binding.tintHint(hasFocus)
        binding.scaleHint(!hasFocus && isEmpty(binding.input.text))
    }

    binding.input.doAfterTextChanged {
        val currentLineCount = binding.input.lineCount
        if (lastLineCount != currentLineCount) binding.hint.doOnNextLayout { binding.scaleHint(false) }
        lastLineCount = currentLineCount
    }
}

private var BindingViewHolder<ViewholderSimpleInputBinding>.lastLineCount by BindingViewHolder.Prop<Int>()

private fun ViewholderSimpleInputBinding.scaleHint(grow: Boolean) {
    val scale = if (grow) 1f else HINT_SHRINK_SCALE
    val translationX = if (grow) 0F else getHintLateralTranslation()
    val translationY = if (grow) 0F else getHintLongitudinalTranslation()

    hint.animate()
            .scaleX(scale)
            .scaleY(scale)
            .translationX(translationX)
            .translationY(translationY)
            .setDuration(HINT_ANIMATION_DURATION.toLong())
            .start()
}

private fun ViewholderSimpleInputBinding.tintHint(hasFocus: Boolean) {
    val start = hint.currentTextColor
    val end = ContextCompat.getColor(hint.context,
            if (hasFocus) R.color.colorAccent
            else R.color.dark_grey)

    ValueAnimator.ofObject(ArgbEvaluator(), start, end).run {
        duration = HINT_ANIMATION_DURATION.toLong()
        doOnEnd { setTintAlpha(hasFocus) }
        addUpdateListener { animation -> hint.setTextColor(animation.animatedValue as Int) }
        start()
    }
}

private fun ViewholderSimpleInputBinding.getHintLateralTranslation(): Float {
    val width = hint.width
    return -((width - HINT_SHRINK_SCALE * width) * HALF)
}

private fun ViewholderSimpleInputBinding.getHintLongitudinalTranslation(): Float {
    return -((root.height - hint.height) * HALF)
}

private fun ViewholderSimpleInputBinding.setTintAlpha(hasFocus: Boolean) {
    hint.alpha = if (!hasFocus) 0.38f else 1f
}

fun BindingViewHolder<ViewholderSimpleInputBinding>.bind(hintValue: CharSequence) = binding.apply {
    hint.text = hintValue
    setTintAlpha(input.hasFocus())
    hint.doOnNextLayout { scaleHint(isEmpty(input.text)) }
}

private const val HINT_ANIMATION_DURATION = 200
private const val HINT_SHRINK_SCALE = 0.8f
private const val HALF = 0.5f