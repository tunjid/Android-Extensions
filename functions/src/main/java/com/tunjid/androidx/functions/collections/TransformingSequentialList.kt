package com.tunjid.androidx.functions.collections

import java.util.*

internal class TransformingSequentialList<F, T>(private val fromList: MutableList<F>,
                                                private val fromFunction: (F) -> T,
                                                private val toFunction: ((T) -> F)?) : AbstractSequentialList<T>() {

    override val size: Int
        get() = fromList.size

    /**
     * The default implementation inherited is based on iteration and removal of each element which
     * can be overkill. That's why we forward this call directly to the backing list.
     */
    override fun clear() = fromList.clear()

    override fun listIterator(index: Int): MutableListIterator<T> {
        val fromListIterator = fromList.listIterator(index)

        return object : TransformedListIterator<F, T>(fromListIterator, toFunction) {
            override fun transform(from: F): T = fromFunction(from)

            override fun set(element: T) =
                if (toFunction == null) throw UnsupportedOperationException()
                else fromListIterator.set(toFunction.invoke(element))

            override fun add(element: T) =
                if (toFunction == null) throw UnsupportedOperationException()
                else fromListIterator.add(toFunction.invoke(element))
        }
    }

    internal abstract class TransformedListIterator<F, T>(
        private val backingIterator: MutableListIterator<F>,
        private val toFunction: ((T) -> F)?
    ) : TransformedIterator<F, T>(backingIterator), MutableListIterator<T> {

        override fun hasPrevious(): Boolean = backingIterator.hasPrevious()

        override fun previous(): T = transform(backingIterator.previous())

        override fun nextIndex(): Int = backingIterator.nextIndex()

        override fun previousIndex(): Int = backingIterator.previousIndex()

        override fun set(element: T) =
            if (toFunction != null) backingIterator.set(toFunction.invoke(element))
            else throw UnsupportedOperationException()

        override fun add(element: T) =
            if (toFunction != null) backingIterator.set(toFunction.invoke(element))
            else throw UnsupportedOperationException()
    }

    internal abstract class TransformedIterator<F, T>(private val backingIterator: MutableIterator<F>) : MutableIterator<T> {

        internal abstract fun transform(from: F): T

        override fun hasNext(): Boolean = backingIterator.hasNext()

        override fun next(): T = transform(backingIterator.next())

        override fun remove() = backingIterator.remove()
    }
}
