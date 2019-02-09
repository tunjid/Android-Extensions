package com.tunjid.androidbootstrap.adapters;

import android.view.ViewGroup;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.viewholders.InputViewHolder;

import java.util.List;

import androidx.annotation.NonNull;

public class InputAdapter extends InteractiveAdapter<InputViewHolder, InteractiveAdapter.AdapterListener> {

    private List<String> hints;

    public InputAdapter(List<String> hints) {
        setHasStableIds(true);
        this.hints = hints;
    }

    @NonNull
    @Override
    public InputViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, parent));
    }

    @Override
    public void onBindViewHolder(@NonNull InputViewHolder holder, int position) {
        holder.bind(hints.get(position));
    }

    @Override public void onViewDetachedFromWindow(@NonNull InputViewHolder holder) {
        holder.text.removeTextChangedListener(holder);
    }

    @Override
    public void onViewRecycled(@NonNull InputViewHolder holder) {
        holder.text.removeTextChangedListener(holder);
    }

    @Override
    public int getItemCount() {
        return hints.size();
    }

    @Override
    public long getItemId(int position) {
        return hints.get(position).hashCode();
    }


}