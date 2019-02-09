package com.tunjid.androidbootstrap.viewholders;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import androidx.core.content.ContextCompat;

import static android.text.TextUtils.isEmpty;
import static com.tunjid.androidbootstrap.view.util.ViewUtil.listenForLayout;

public class InputViewHolder extends InteractiveViewHolder
         implements
        TextWatcher {

     private static final int HINT_ANIMATION_DURATION = 200;
     private static final float HINT_SHRINK_SCALE = 0.8F;
     private static final float HALF = 0.5F;

     private int lastLineCount = 1;

     private final TextView hint;
     public final EditText text;

     public InputViewHolder(View itemView) {
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

      public void bind(String hintValue) {
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
