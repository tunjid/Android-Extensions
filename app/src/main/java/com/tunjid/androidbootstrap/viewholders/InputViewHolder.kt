package com.tunjid.androidbootstrap.viewholders

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.text.Editable
import android.text.TextUtils.isEmpty
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder

class InputViewHolder(itemView: View)
    : InteractiveViewHolder<Unit>(itemView, Unit), TextWatcher {

    private var lastLineCount = 1

    private val hint: TextView = itemView.findViewById(R.id.hint)
    val text: EditText = itemView.findViewById(R.id.input)

    init {
        text.setOnFocusChangeListener { _, hasFocus ->
            tintHint(hasFocus)
            scaleHint(!hasFocus && isEmpty(text.text))
        }
    }

    fun bind(hintValue: String) {
        hint.text = hintValue
        text.addTextChangedListener(this)
        setTintAlpha(text.hasFocus())
        hint.doOnNextLayout { scaleHint(isEmpty(text.text)) }
    }

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {/* Nothing */
    }

    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {/* Nothing */
    }

    override fun afterTextChanged(editable: Editable) {
        val currentLineCount = text.lineCount
        if (lastLineCount != currentLineCount) hint.doOnNextLayout { scaleHint(false) }
        lastLineCount = currentLineCount
    }

    private fun scaleHint(grow: Boolean) {
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

    private fun tintHint(hasFocus: Boolean) {
        val start = hint.currentTextColor
        val end = ContextCompat.getColor(hint.context,
                if (hasFocus) R.color.colorAccent
                else R.color.dark_grey)

        val animator = ValueAnimator.ofObject(ArgbEvaluator(), start, end)
        animator.duration = HINT_ANIMATION_DURATION.toLong()
        animator.addUpdateListener { animation -> hint.setTextColor(animation.animatedValue as Int) }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) = setTintAlpha(hasFocus)
        })

        animator.start()
    }

    private fun getHintLateralTranslation(): Float {
        val width = hint.width
        return -((width - HINT_SHRINK_SCALE * width) * HALF)
    }

    private fun getHintLongitudinalTranslation(): Float {
        return -((itemView.height - hint.height) * HALF)
    }

    private fun setTintAlpha(hasFocus: Boolean) {
        hint.alpha = if (!hasFocus) 0.38f else 1f
    }

    companion object {

        private const val HINT_ANIMATION_DURATION = 200
        private const val HINT_SHRINK_SCALE = 0.8f
        private const val HALF = 0.5f
    }
}
