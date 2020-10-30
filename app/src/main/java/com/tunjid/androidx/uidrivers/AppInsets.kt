package com.tunjid.androidx.uidrivers


/**
 * Describes how the app insets itself relative to system UI like the status and nav bar
 */
interface InsetDescriptor {
    val hasTopInset: Boolean
    val hasBottomInset: Boolean
}

private data class DelegateInsetDescriptor(
        override val hasTopInset: Boolean,
        override val hasBottomInset: Boolean,
        val name: String
) : InsetDescriptor {
    override fun toString(): String = name
}

data class InsetFlags(
        val hasLeftInset: Boolean,
        override val hasTopInset: Boolean,
        val hasRightInset: Boolean,
        override val hasBottomInset: Boolean
) : InsetDescriptor {
    companion object {

        val ALL = InsetFlags(hasLeftInset = true, hasTopInset = true, hasRightInset = true, hasBottomInset = true)
        val NO_TOP = InsetFlags(hasLeftInset = true, hasTopInset = false, hasRightInset = true, hasBottomInset = true)
        val NONE = InsetFlags(hasLeftInset = false, hasTopInset = false, hasRightInset = false, hasBottomInset = false)
        val BOTTOM = InsetFlags(hasLeftInset = false, hasTopInset = false, hasRightInset = false, hasBottomInset = true)
        val VERTICAL = InsetFlags(hasLeftInset = false, hasTopInset = true, hasRightInset = false, hasBottomInset = true)
        val HORIZONTAL = InsetFlags(hasLeftInset = true, hasTopInset = false, hasRightInset = true, hasBottomInset = true)
        val NO_BOTTOM: InsetFlags = InsetFlags(hasLeftInset = true, hasTopInset = true, hasRightInset = true, hasBottomInset = false)
    }
}