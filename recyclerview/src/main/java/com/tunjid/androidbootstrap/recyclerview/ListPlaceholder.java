package com.tunjid.androidbootstrap.recyclerview;


public interface ListPlaceholder<T> {

    void toggle(boolean visible);

    void bind(T data);
}
