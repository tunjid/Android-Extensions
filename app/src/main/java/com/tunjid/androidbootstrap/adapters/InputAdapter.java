package com.tunjid.androidbootstrap.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import static android.text.TextUtils.isEmpty;
import static com.tunjid.androidbootstrap.view.util.ViewUtil.listenForLayout;

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


    static class InputViewHolder extends InteractiveViewHolder
            implements
            TextWatcher {

        private static final int HINT_ANIMATION_DURATION = 200;
        private static final float HINT_SHRINK_SCALE = 0.8F;
        private static final float HALF = 0.5F;

        private int lastLineCount = 1;

        protected final TextView hint;
        protected final EditText text;

        InputViewHolder(View itemView) {
            super(itemView);
            hint = itemView.findViewById(R.id.hint);
            text = itemView.findViewById(R.id.input);
            text.setOnFocusChangeListener((v, hasFocus) -> {
                tintHint(hasFocus);
                scaleHint(!hasFocus && isEmpty(text.getText()));
            });
        }

        private float getHintLateralTranslation() {
            int width = hint.getWidth();
            return -((width - (HINT_SHRINK_SCALE * width)) * HALF);
        }

        private float getHintLongitudinalTranslation() {
            return -((itemView.getHeight() - hint.getHeight()) * HALF);
        }

         void bind(String hintValue) {
            hint.setText(hintValue);
            text.addTextChangedListener(this);
            setTintAlpha(text.hasFocus());
            listenForLayout(hint, () -> scaleHint(isEmpty(text.getText())));
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {/* Nothing */}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {/* Nothing */}

        @Override
        public void afterTextChanged(Editable editable) {
            int currentLineCount = text.getLineCount();
            if (lastLineCount != currentLineCount) listenForLayout(hint, () -> scaleHint(false));
            lastLineCount = currentLineCount;
        }

        private void scaleHint(boolean grow) {
            float scale = grow ? 1F : HINT_SHRINK_SCALE;
            float translationX = grow ? 0 : getHintLateralTranslation();
            float translationY = grow ? 0 : getHintLongitudinalTranslation();

            hint.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .translationX(translationX)
                    .translationY(translationY)
                    .setDuration(HINT_ANIMATION_DURATION)
                    .start();
        }

        private void tintHint(boolean hasFocus) {
            int start = hint.getCurrentTextColor();
            int end = ContextCompat.getColor(hint.getContext(), hasFocus
                    ? R.color.colorAccent
                    : R.color.dark_grey);

            ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), start, end);
            animator.setDuration(HINT_ANIMATION_DURATION);
            animator.addUpdateListener(animation -> hint.setTextColor((int) animation.getAnimatedValue()));
            animator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) { setTintAlpha(hasFocus); }
            });
            animator.start();
        }

        private void setTintAlpha(boolean hasFocus) {
            hint.setAlpha(!hasFocus ? 0.38F : 1F);
        }
    }
}