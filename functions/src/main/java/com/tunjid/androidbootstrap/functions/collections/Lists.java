package com.tunjid.androidbootstrap.functions.collections;

import com.tunjid.androidbootstrap.functions.Function;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class Lists {

    public static <T> void replace(Collection<T> dest, Collection<T> src) {
        dest.clear();
        dest.addAll(src);
    }

    public static <F, T> List<T> transform(List<F> fromList,
                                           Function<? super F, ? extends T> fromFunction) {
        return transform(fromList, fromFunction, null);
    }

    @SuppressWarnings("WeakerAccess")
    public static <F, T> List<T> transform(List<F> fromList,
                                           Function<? super F, ? extends T> fromFunction,
                                           Function<? super T, ? extends F> toFunction) {
        return new TransformingSequentialList<>(fromList, fromFunction, toFunction);
    }

    @Nullable
    @SuppressWarnings({"unused", "unchecked"})
    public static <T> T findFirst(List<?> list, Class<T> typeClass) {
        for (Object item : list) if (typeClass.isAssignableFrom(item.getClass())) return (T) item;
        return null;
    }

    @Nullable
    @SuppressWarnings({"unused", "unchecked"})
    public static <T> T findLast(List<?> list, Class<T> typeClass) {
        ListIterator<?> li = list.listIterator(list.size());
        while (li.hasPrevious()) {
            Object item = li.previous();
            if (typeClass.isAssignableFrom(item.getClass())) return ((T) item);
        }
        return null;
    }

    public static <T, R> List<T> union(
            List<T> source,
            List<T> additions,
            Function<T, R> mappingFunction) {
        Function<T, Mask<T, R>> maskFunction = item -> new Mask<>(item, mappingFunction);
        Set<Mask<T, R>> set = new HashSet<>(transform(additions, maskFunction));
        set.addAll(transform(source, maskFunction));

        ArrayList<T> result = new ArrayList<>(set.size());
        for (Mask<T, R> mask : set) result.add(mask.getItem());

        return result;
    }

}
