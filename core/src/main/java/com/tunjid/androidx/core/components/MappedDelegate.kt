package com.tunjid.androidx.core.components

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private class MappedDelegate<In, Out, T>(
    private val source: ReadWriteProperty<In, T>,
    private val postWrite: ((Out, In) -> Unit)? = null,
    private val mapper: (Out) -> In
) : ReadWriteProperty<Out, T> {

    override fun getValue(thisRef: Out, property: KProperty<*>): T =
        source.getValue(mapper(thisRef), property)

    override fun setValue(thisRef: Out, property: KProperty<*>, value: T) {
        val mapped = mapper(thisRef)
        source.setValue(mapped, property, value)
        postWrite?.invoke(thisRef, mapped)
    }
}

fun <In, Out, T> ReadWriteProperty<In, T>.map(
    postWrite: ((Out, In) -> Unit)? = null,
    mapper: (Out) -> In
): ReadWriteProperty<Out, T> =
    MappedDelegate(source = this, postWrite = postWrite, mapper = mapper)