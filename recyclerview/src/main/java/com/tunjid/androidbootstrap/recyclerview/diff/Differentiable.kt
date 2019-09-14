package com.tunjid.androidbootstrap.recyclerview.diff


interface Differentiable {

    val id: String

    fun areContentsTheSame(other: Differentiable): Boolean = id == other.id

    fun getChangePayload(other: Differentiable): Any? = null

    companion object {

        fun fromCharSequence(charSequenceSupplier: () -> CharSequence): Differentiable {
            val id = charSequenceSupplier().toString()

            return object : Differentiable {
                override val id: String get() = id

                override fun equals(other: Any?): Boolean = id == other

                override fun hashCode(): Int = id.hashCode()
            }
        }
    }

}
