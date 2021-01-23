package com.tunjid.androidx.functions

fun interface Mutation<T> {
    fun change(original: T): T
}

fun <T> T.mutate(mutation: Mutation<T>) = mutation.change(this)

fun <In, Out : Any> In.mutation(call: Out.(cause: In) -> Out) =
    Mutation<Out> { original -> call(original, this@mutation) }
