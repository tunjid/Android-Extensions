package com.tunjid.androidx.recyclerview.diff


@Deprecated(
    message = "Use the better named Diffable instead",
    replaceWith = ReplaceWith(
        expression = "Diffable",
        imports = arrayOf("com.tunjid.androidx.recyclerview.diff.Diffable")
    )
)
typealias Differentiable = Diffable

interface Diffable {

    val diffId: String

    fun areContentsTheSame(other: Diffable): Boolean = this == other

    fun getChangePayload(other: Diffable): Any? = null

    companion object {
        fun fromCharSequence(charSequenceSupplier: () -> CharSequence): Diffable {
            val id = charSequenceSupplier().toString()

            return object : Diffable {
                override val diffId: String get() = id

                override fun equals(other: Any?): Boolean = id == other

                override fun hashCode(): Int = id.hashCode()
            }
        }
    }

}
