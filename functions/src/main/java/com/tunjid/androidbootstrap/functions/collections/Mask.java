package com.tunjid.androidbootstrap.functions.collections;

import com.tunjid.androidbootstrap.functions.Function;

class Mask<T, R> {

    private final T item;
    private final Function<T, R> mappingFunction;

    Mask(T item, Function<T, R> mappingFunction) {
        this.item = item;
        this.mappingFunction = mappingFunction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mask<?, ?> other = (Mask<?, ?>) o;
        if (!item.getClass().equals(other.item.getClass())) return false;

        @SuppressWarnings("unchecked")
        R otherUnMasked = mappingFunction.apply((T) other.item);
        R unMasked = mappingFunction.apply(item);

        return unMasked.equals(otherUnMasked);
    }

    @Override
    public int hashCode() { return mappingFunction.apply(item).hashCode(); }

    T getItem() { return item; }
}
