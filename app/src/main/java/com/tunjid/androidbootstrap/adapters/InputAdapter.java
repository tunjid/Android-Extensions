package com.tunjid.androidbootstrap.adapters;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.textfield.TextInputLayout;
import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveViewHolder;

import java.util.List;

import androidx.annotation.NonNull;

public class InputAdapter extends InteractiveAdapter<InputAdapter.InputViewHolder, InteractiveAdapter.AdapterListener> {

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

    @Override
    public int getItemCount() {
        return hints.size();
    }

    @Override
    public long getItemId(int position) {
        return hints.get(position).hashCode();
    }


    static class InputViewHolder extends InteractiveViewHolder {

        private final TextInputLayout input;

        InputViewHolder(View itemView) {
            super(itemView);
            input = (TextInputLayout) itemView;
        }

        void bind(String hint) {
            input.setHint(hint);
        }
    }
}
