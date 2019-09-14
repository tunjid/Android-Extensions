package com.tunjid.androidbootstrap.recyclerview


interface ListPlaceholder<T> {

    fun toggle(visible: Boolean)

    fun bind(data: T)
}
