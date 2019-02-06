package com.tunjid.androidbootstrap.functions;

import java.util.Objects;

@FunctionalInterface
public interface BiFunction<T, U, R> {
    R apply(T var1, U var2);

    default <V> BiFunction<T, U, V> andThen(Function<? super R, ? extends V> var1) {
        Objects.requireNonNull(var1);
        return (var2, var3) -> var1.apply(this.apply(var2, var3));
    }
}
