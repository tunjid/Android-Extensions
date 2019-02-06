package com.tunjid.androidbootstrap.material.animator;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.Transition.TransitionListener;
import android.transition.TransitionManager;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.ripple.RippleUtils;
import com.tunjid.androidbootstrap.view.R;
import com.tunjid.androidbootstrap.view.util.ViewUtil;

import java.util.Objects;

import androidx.annotation.NonNull;

public class FabExtensionAnimator {

    private static final float TWITCH_END = 20.0f;
    private static final float TWITCH_START = 0.0f;
    private static final int TWITCH_DURATION = 200;
    private static final int EXTENSION_DURATION = 150;
    private static final String ROTATION_Y_PROPERTY = "rotationY";

    private final int collapsedFabSize;
    private final int extendedFabHeight;
    private boolean isAnimating;

    private GlyphState glyphState;
    private final MaterialButton button;

    private final TransitionListener listener = new TransitionListener() {
        public void onTransitionStart(Transition transition) { isAnimating = true; }

        public void onTransitionEnd(Transition transition) { isAnimating = false; }

        public void onTransitionCancel(Transition transition) {isAnimating = false; }

        public void onTransitionPause(Transition transition) { }

        public void onTransitionResume(Transition transition) {}
    };

    public FabExtensionAnimator(MaterialButton button) {
        this.button = button;
        collapsedFabSize = button.getResources().getDimensionPixelSize(R.dimen.collapsed_fab_size);
        extendedFabHeight = button.getResources().getDimensionPixelSize(R.dimen.extended_fab_height);
        button.setBackground(getDrawable());
    }

    public static GlyphState newState(CharSequence text, Drawable icon) { return new SimpleGlyphState(text, icon);}

    public void updateGlyphs(@NonNull GlyphState glyphState) {
        boolean isSame = glyphState.equals(this.glyphState);
        this.glyphState = glyphState;
        animateChange(glyphState, isSame);
    }

    public void setExtended(boolean extended) { setExtended(extended, false); }

    @SuppressWarnings("WeakerAccess")
    public boolean isExtended() {
        ViewGroup.MarginLayoutParams params = ViewUtil.getLayoutParams(button);
        return !(params.height == params.width && params.width == getCollapsedFabSize());
    }

    private void animateChange(GlyphState glyphState, boolean isSame) {
        boolean extended = isExtended();
        this.button.setText(glyphState.getText());
        this.button.setIcon(glyphState.getIcon());
        setExtended(extended, !isSame);
        if (!extended) onPreExtend();
    }

    private void setExtended(boolean extended, boolean force) {
        if (isAnimating || (extended && isExtended() && !force)) return;

        int collapsedFabSize = getCollapsedFabSize();
        int width = extended ? ViewGroup.LayoutParams.WRAP_CONTENT : collapsedFabSize;
        int height = extended ? getExpandedFabHeight() : collapsedFabSize;

        ViewGroup.LayoutParams params = ViewUtil.getLayoutParams(button);
        ViewGroup group = (ViewGroup) button.getParent();

        params.width = width;
        params.height = height;

        TransitionManager.beginDelayedTransition(group, new AutoTransition()
                .setDuration(EXTENSION_DURATION)
                .addListener(listener)
                .addTarget(button));

        if (extended) this.button.setText(this.glyphState.getText());
        else this.button.setText("");

        button.requestLayout();
        button.invalidate();
    }

    @SuppressWarnings("WeakerAccess")
    public void onPreExtend() {
        AnimatorSet set = new AnimatorSet();
        set.play(animateProperty(TWITCH_END, TWITCH_START)).after(animateProperty(TWITCH_START, TWITCH_END));
        set.start();
    }

    @SuppressWarnings("WeakerAccess")
    protected int getCollapsedFabSize() { return collapsedFabSize;}

    @SuppressWarnings("WeakerAccess")
    protected int getExpandedFabHeight() { return extendedFabHeight;}

    @NonNull
    private ObjectAnimator animateProperty(float start, float end) {
        return ObjectAnimator.ofFloat(button, ROTATION_Y_PROPERTY, start, end).setDuration(TWITCH_DURATION);
    }

    @SuppressLint("RestrictedApi")
    private Drawable getDrawable() {
        int cornerRadius = getCollapsedFabSize();
        int strokeWidth = button.getStrokeWidth();
        ColorStateList rippleColor = button.getRippleColor();
        ColorStateList strokeColor = button.getStrokeColor();

        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setCornerRadius(cornerRadius);
        backgroundDrawable.setColor(-1);

        GradientDrawable strokeDrawable = new GradientDrawable();
        strokeDrawable.setStroke(strokeWidth, strokeColor);
        strokeDrawable.setCornerRadius(cornerRadius);
        strokeDrawable.setColor(0);

        GradientDrawable maskDrawable = new GradientDrawable();
        maskDrawable.setCornerRadius(cornerRadius);
        maskDrawable.setColor(-1);

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{backgroundDrawable, strokeDrawable});
        return new RippleDrawable(RippleUtils.convertToRippleDrawableColor(rippleColor), layerDrawable, maskDrawable);
    }

    public static abstract class GlyphState {

        public abstract Drawable getIcon();

        public abstract CharSequence getText();
    }

    private static class SimpleGlyphState extends GlyphState {

        public Drawable icon;
        public CharSequence text;

        private SimpleGlyphState(CharSequence text, Drawable icon) {
            this.text = text;
            this.icon = icon;
        }

        public CharSequence getText() { return text; }

        public Drawable getIcon() { return icon; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimpleGlyphState that = (SimpleGlyphState) o;
            return Objects.equals(icon, that.icon) &&
                    Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(icon, text);
        }
    }
}
