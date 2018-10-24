package com.tunjid.androidbootstrap.view.animator;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.Transition.TransitionListener;
import android.transition.TransitionManager;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.android.material.button.MaterialButton;
import com.tunjid.androidbootstrap.view.R;
import com.tunjid.androidbootstrap.view.util.ViewUtil;

import java.util.Objects;

public class FabExtensionAnimator {

    private static final int DURATION = 200;
    private static final float TWITCH_END = 20.0f;
    private static final float TWITCH_START = 0.0f;
    private static final String ROTATION_Y_PROPERTY = "rotationY";

    private int fabSize;
    private boolean isAnimating;

    private State state;
    private final MaterialButton button;
    private final ConstraintLayout container;

    private final TransitionListener listener =  new TransitionListener() {
        public void onTransitionStart(Transition transition) { isAnimating = true; }

        public void onTransitionEnd(Transition transition) { isAnimating = false; }

        public void onTransitionCancel(Transition transition) {isAnimating = false; }

        public void onTransitionPause(Transition transition) { }

        public void onTransitionResume(Transition transition) {}
    };

    public FabExtensionAnimator(ConstraintLayout container) {
        this.container = container;
        this.button = (MaterialButton) container.getChildAt(0);
        this.fabSize = container.getResources().getDimensionPixelSize(R.dimen.fab_size);
    }

    public static State newState(CharSequence text, Drawable icon) { return new SimpleState(text, icon);}

    public void update(@NonNull State state) {
        boolean isSame = state.equals(this.state);
        this.state = state;
        animateChange(state, isSame);
    }

    public void setExtended(boolean extended) { setExtended(extended, false); }

    private boolean isExtended() {
        return ViewUtil.getLayoutParams(button).height != this.button.getResources().getDimensionPixelSize(R.dimen.fab_size);
    }

    private void animateChange(State state, boolean isSame) {
        boolean extended = isExtended();
        this.button.setText(state.getText());
        this.button.setIcon(state.getIcon());
        setExtended(extended, !isSame);
        if (!extended) twitch();
    }

    private void setExtended(boolean extended, boolean force) {
        if (isAnimating || (extended && isExtended() && !force)) return;

        ConstraintSet set = extended ? expanded() : collapsed();
        TransitionManager.beginDelayedTransition(this.container, new AutoTransition().addListener(listener).setDuration(150));

        if (extended) this.button.setText(this.state.getText());
        else this.button.setText("");

        set.applyTo(container);
    }

    private void twitch() {
        AnimatorSet set = new AnimatorSet();
        set.play(animateProperty(TWITCH_END, TWITCH_START)).after(animateProperty(TWITCH_START, TWITCH_END));
        set.start();
    }

    private ConstraintSet expanded() {
        ConstraintSet set = fabSet();
        set.constrainHeight(this.button.getId(), ConstraintSet.WRAP_CONTENT);
        set.constrainWidth(this.button.getId(), ConstraintSet.WRAP_CONTENT);
        return set;
    }

    private ConstraintSet collapsed() {
        ConstraintSet set = fabSet();
        set.constrainHeight(this.button.getId(), this.fabSize);
        set.constrainWidth(this.button.getId(), this.fabSize);
        return set;
    }

    private ConstraintSet fabSet() {
        int buttonId = this.button.getId();
        int containerId = this.container.getId();
        ConstraintSet set = new ConstraintSet();
        set.connect(buttonId, ConstraintSet.LEFT, containerId, ConstraintSet.LEFT);
        set.connect(buttonId, ConstraintSet.TOP, containerId, ConstraintSet.TOP);
        set.connect(buttonId, ConstraintSet.RIGHT, containerId, ConstraintSet.RIGHT);
        set.connect(buttonId, ConstraintSet.BOTTOM, containerId, ConstraintSet.BOTTOM);
        return set;
    }

    @NonNull
    private ObjectAnimator animateProperty(float start, float end) {
        return ObjectAnimator.ofFloat(container, ROTATION_Y_PROPERTY, start, end).setDuration(DURATION);
    }

    public static abstract class State {

        public abstract Drawable getIcon();

        public abstract CharSequence getText();
    }

    public static class SimpleState extends State {

        public Drawable icon;
        public CharSequence text;

        private SimpleState(CharSequence text, Drawable icon) {
            this.text = text;
            this.icon = icon;
        }

        public CharSequence getText() { return text; }

        public Drawable getIcon() { return icon; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimpleState that = (SimpleState) o;
            return Objects.equals(icon, that.icon) &&
                    Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(icon, text);
        }
    }
}
