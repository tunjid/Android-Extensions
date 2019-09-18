package com.tunjid.androidbootstrap.recyclerview.diff


interface Differentiable {

    val diffId: String

    fun areContentsTheSame(other: Differentiable): Boolean = diffId == other.diffId

    fun getChangePayload(other: Differentiable): Any? = null

    companion object {

        fun fromCharSequence(charSequenceSupplier: () -> CharSequence): Differentiable {
            val id = charSequenceSupplier().toString()

            return object : Differentiable {
                override val diffId: String get() = id

                override fun equals(other: Any?): Boolean = id == other

                override fun hashCode(): Int = id.hashCode()
            }
        }
    }

}
