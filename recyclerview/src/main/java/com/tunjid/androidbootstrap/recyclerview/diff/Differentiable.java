package com.tunjid.androidbootstrap.recyclerview.diff;


import com.tunjid.androidbootstrap.functions.Supplier;

public interface Differentiable {

    String getId();

    default boolean areContentsTheSame(Differentiable other) { return getId().equals(other.getId()); }

    default Object getChangePayload(Differentiable other) {
        return null;
    }

    static Differentiable fromCharSequence(Supplier<CharSequence> charSequenceSupplier) {
        final String id = charSequenceSupplier.get().toString();

        //noinspection EqualsWhichDoesntCheckParameterClass
        return new Differentiable() {
            @Override
            public boolean equals(Object obj) { return id.equals(obj); }

            @Override
            public int hashCode() { return id.hashCode(); }

            @Override
            public String getId() { return id; }
        };
    }

}
