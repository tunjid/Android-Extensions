package com.tunjid.androidbootstrap.functions;

import java.util.AbstractSequentialList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public final class TransformingSequentialList<F, T> extends AbstractSequentialList<T> {

    private final List<F> fromList;

    private final Function<? super T, ? extends F> toFunction;
    private final Function<? super F, ? extends T> fromFunction;

    public static <F, T> List<T> transform(List<F> fromList,
                                           Function<? super F, ? extends T> fromFunction) {
        return transform(fromList, fromFunction, null);
    }

    public static <F, T> List<T> transform(List<F> fromList,
                                           Function<? super F, ? extends T> fromFunction,
                                           Function<? super T, ? extends F> toFunction) {
        return new TransformingSequentialList<>(fromList, fromFunction, toFunction);
    }

    private TransformingSequentialList(List<F> fromList,
                                       Function<? super F, ? extends T> fromFunction,
                                       Function<? super T, ? extends F> toFunction) {
        this.fromList = fromList;
        this.toFunction = toFunction;
        this.fromFunction = fromFunction;
    }

    /**
     * The default implementation inherited is based on iteration and removal of each element which
     * can be overkill. That's why we forward this call directly to the backing list.
     */
    @Override
    public void clear() { fromList.clear(); }

    @Override
    public int size() { return fromList.size(); }

    @Override
    public ListIterator<T> listIterator(final int index) {
        final ListIterator<F> fromListIterator = fromList.listIterator(index);

        return new TransformedListIterator<F, T>(fromListIterator) {
            @Override
            T transform(F from) { return fromFunction.apply(from); }

            @Override
            public void set(T element) {
                if (toFunction == null) throw new UnsupportedOperationException();
                else fromListIterator.set(toFunction.apply(element));
            }

            @Override
            public void add(T element) {
                if (toFunction == null) throw new UnsupportedOperationException();
                else fromListIterator.add(toFunction.apply(element));
            }
        };
    }

    abstract static class TransformedListIterator<F, T> extends TransformedIterator<F, T>
            implements ListIterator<T> {

        TransformedListIterator(ListIterator<? extends F> backingIterator) {
            super(backingIterator);
        }

        private ListIterator<? extends F> backingIterator() {
            return cast(backingIterator);
        }

        @Override
        public final boolean hasPrevious() { return backingIterator().hasPrevious(); }

        @Override
        public final T previous() { return transform(backingIterator().previous()); }

        @Override
        public final int nextIndex() { return backingIterator().nextIndex(); }

        @Override
        public final int previousIndex() { return backingIterator().previousIndex(); }

        @Override
        public void set(T element) { throw new UnsupportedOperationException(); }

        @Override
        public void add(T element) { throw new UnsupportedOperationException(); }

        private static <T> ListIterator<T> cast(Iterator<T> iterator) {
            return (ListIterator<T>) iterator;
        }
    }

    abstract static class TransformedIterator<F, T> implements Iterator<T> {
        final Iterator<? extends F> backingIterator;

        TransformedIterator(Iterator<? extends F> backingIterator) {
            this.backingIterator = backingIterator;
        }

        abstract T transform(F from);

        @Override
        public final boolean hasNext() {
            return backingIterator.hasNext();
        }

        @Override
        public final T next() {
            return transform(backingIterator.next());
        }

        @Override
        public final void remove() {
            backingIterator.remove();
        }
    }
}
