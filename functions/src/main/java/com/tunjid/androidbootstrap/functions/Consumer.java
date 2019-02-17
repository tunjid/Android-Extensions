package com.tunjid.androidbootstrap.functions;

import java.util.Objects;

public interface Consumer<T> {
    void accept(T var1);

    default Consumer<T> andThen(Consumer<? super T> var1) {
        Objects.requireNonNull(var1);
        return (var2) -> {
            this.accept(var2);
            var1.accept(var2);
        };
    }}
