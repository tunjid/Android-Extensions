package com.tunjid.androidx.recyclerview


interface ListPlaceholder<T> {

    fun toggle(visible: Boolean)

    fun bind(data: T)
}
