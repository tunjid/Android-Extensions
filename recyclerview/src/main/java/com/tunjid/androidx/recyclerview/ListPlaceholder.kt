package com.tunjid.androidx.recyclerview

@Deprecated("Unnecessary abstraction, notifying that a list is empty should be left to the discretion of the client app")
interface ListPlaceholder<T> {

    fun toggle(visible: Boolean)

    fun bind(data: T)
}
