package com.tunjid.androidbootstrap.recyclerview;

import com.tunjid.androidbootstrap.functions.BiFunction;
import com.tunjid.androidbootstrap.functions.Function;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

class DiffCallback<T> extends DiffUtil.Callback {

    private final List<T> stale;
    private final List<T> updated;
    private final Function<T, Differentiable> diffFunction;

    DiffCallback(List<T> stale, List<T> updated, Function<T, Differentiable> diffFunction) {
        this.stale = stale;
        this.updated = updated;
        this.diffFunction = diffFunction;
    }

    @Override
    public int getOldListSize() {
        return stale.size();
    }

    @Override
    public int getNewListSize() {
        return updated.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return apply(oldItemPosition, newItemPosition, (stale, current) -> stale.getId().equals(current.getId()));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return apply(oldItemPosition, newItemPosition, Differentiable::areContentsTheSame);
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return apply(oldItemPosition, newItemPosition, Differentiable::getChangePayload);
    }

    private <S> S apply(int oldPosition, int newPosition, BiFunction<Differentiable, Differentiable, S> function) {
        return function.apply(diffFunction.apply(stale.get(oldPosition)), diffFunction.apply(updated.get(newPosition)));
    }
}
