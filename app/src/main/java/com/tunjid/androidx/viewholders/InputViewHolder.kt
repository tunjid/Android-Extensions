package com.tunjid.androidx.viewholders

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.text.TextUtils.isEmpty
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.core.view.doOnNextLayout
import androidx.core.widget.doAfterTextChanged
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.colorAt
import com.tunjid.androidx.databinding.ViewholderSimpleInputBinding
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderDelegate
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

private var BindingViewHolder<ViewholderSimpleInputBinding>.lastLineCount by viewHolderDelegate<Int>()

private val ViewholderSimpleInputBinding.hintLateralTranslation: Float
    get() = hint.width.let { -((it - HINT_SHRINK_SCALE * it) * HALF) }

private val ViewholderSimpleInputBinding.hintLongitudinalTranslation: Float
    get() = -((root.height - hint.height) * HALF)

private fun ViewholderSimpleInputBinding.scaleHint(grow: Boolean) {
    val scale = if (grow) 1f else HINT_SHRINK_SCALE
    hint.animate()
            .scaleX(scale)
            .scaleY(scale)
            .translationX(if (grow) 0F else hintLateralTranslation)
            .translationY(if (grow) 0F else hintLongitudinalTranslation)
            .setDuration(HINT_ANIMATION_DURATION.toLong())
            .start()
}

private fun ViewholderSimpleInputBinding.tintHint(hasFocus: Boolean) {
    val start = hint.currentTextColor
    val end = hint.context.colorAt(if (hasFocus) R.color.colorAccent else R.color.dark_grey)

    ValueAnimator.ofObject(ArgbEvaluator(), start, end).run {
        duration = HINT_ANIMATION_DURATION.toLong()
        doOnEnd { setTintAlpha(hasFocus) }
        addUpdateListener { animation -> hint.setTextColor(animation.animatedValue as Int) }
        start()
    }
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