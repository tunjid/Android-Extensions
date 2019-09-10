package com.tunjid.androidbootstrap.view.util

data class InsetFlags(
        val hasLeftInset: Boolean,
        val hasTopInset: Boolean,
        val hasRightInset: Boolean,
        val hasBottomInset: Boolean
) {
    companion object {

        val ALL = InsetFlags(hasLeftInset = true, hasTopInset = true, hasRightInset = true, hasBottomInset = true)
        val NO_TOP = InsetFlags(hasLeftInset = true, hasTopInset = false, hasRightInset = true, hasBottomInset = true)
        val NONE = InsetFlags(hasLeftInset = false, hasTopInset = false, hasRightInset = false, hasBottomInset = false)
        val BOTTOM = InsetFlags(hasLeftInset = false, hasTopInset = false, hasRightInset = false, hasBottomInset = true)
        val VERTICAL = InsetFlags(hasLeftInset = false, hasTopInset = true, hasRightInset = false, hasBottomInset = true)
        val HORIZONTAL = InsetFlags(hasLeftInset = true, hasTopInset = false, hasRightInset = true, hasBottomInset = true)
    }
}
