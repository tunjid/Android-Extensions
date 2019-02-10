package com.tunjid.androidbootstrap.functions;

import java.util.Objects;

public interface Predicate<T> {
    boolean test(T var1);

    default Predicate<T> and(Predicate<? super T> var1) {
        Objects.requireNonNull(var1);
        return (var2) -> {
            return this.test(var2) && var1.test(var2);
        };
    }

    default Predicate<T> negate() {
        return (var1) -> {
            return !this.test(var1);
        };
    }

    default Predicate<T> or(Predicate<? super T> var1) {
        Objects.requireNonNull(var1);
        return (var2) -> {
            return this.test(var2) || var1.test(var2);
        };
    }

    static <T> Predicate<T> isEqual(Object var0) {
        return null == var0 ? Objects::isNull : (var1) -> {
            return var0.equals(var1);
        };
    }
}
