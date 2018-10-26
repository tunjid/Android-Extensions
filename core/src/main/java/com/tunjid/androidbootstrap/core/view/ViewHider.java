package com.tunjid.androidbootstrap.core.view;


import android.graphics.Point;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorCompat;
import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Interpolator;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.WINDOW_SERVICE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Translates a view offscreen, useful for adding the quick return pattern to a view.
 * <p>
 * Created by tj.dahunsi on 2/19/16.
 */
public class ViewHider {

    private static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();

    @Retention(SOURCE)
    @IntDef({LEFT, TOP, RIGHT, BOTTOM})
    @SuppressWarnings("WeakerAccess")
    public @interface HideDirection {
    }

    public static final int LEFT = 0;
    public static final int TOP = 1;
    public static final int RIGHT = 2;
    public static final int BOTTOM = 3;

    private boolean isVisible = true;
    private final @HideDirection int direction;
    private final long duration;

    private final View view;
    private final Listener listener;

    private final Runnable startRunnable;
    private final Runnable endRunnable;

    public static Builder of(View view) {return new Builder(view);}

    private ViewHider(View view, Listener listener, @HideDirection int direction, long duration) {
        this.view = view;
        this.listener = listener;
        this.duration = duration;
        this.direction = direction;

        startRunnable = () -> {if (isVisible) view.setVisibility(View.VISIBLE);};
        endRunnable = () -> {if (!isVisible) view.setVisibility(View.GONE);};
    }

    public void show() {
        toggle(true);
    }

    public void hide() {
        toggle(false);
    }

    private void toggle(final boolean visible) {
        if (this.isVisible != visible) {
            final ViewTreeObserver original = view.getViewTreeObserver();

            // View hasn't been laid out yet and has it's observer attached
            if (view.getHeight() == 0 && original.isAlive()) {
                original.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        ViewTreeObserver current = view.getViewTreeObserver();
                        if (current.isAlive()) current.removeOnPreDrawListener(this);
                        // toggle as soon as the view is visible
                        toggle(visible);
                        return true;
                    }
                });
                return;
            }

            this.isVisible = visible;
            float displacement = visible ? 0 : getDistanceOffscreen();

            ViewPropertyAnimatorCompat animator = ViewCompat.animate(view)
                    .setDuration(duration)
                    .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setListener(listener);

            if (direction == LEFT || direction == RIGHT) animator.translationX(displacement);
            else animator.translationY(displacement);

            animator.start();
        }
    }

    private int getDistanceOffscreen() {
        int[] location = new int[2];
        Point displaySize = new Point();

        WindowManager manager = (WindowManager) view.getContext().getSystemService(WINDOW_SERVICE);

        view.getLocationInWindow(location);
        manager.getDefaultDisplay().getSize(displaySize);

        // These calculations don't take the status bar into account, unlikely to matter however
        switch (direction) {
            case LEFT:
                return -(location[0] + view.getWidth());
            case TOP:
                return -(location[1] + view.getHeight());
            case RIGHT:
                return displaySize.x - location[0];
            case BOTTOM:
                return displaySize.y - location[1];
            default:
                throw new IllegalArgumentException("Invalid direction");
        }
    }

    public static class Builder {

        @HideDirection
        private int direction = BOTTOM;
        private long duration = 200L;
        private final View view;
        private final Listener listener = new Listener();

        Builder(@NonNull View view) {this.view = view;}

        public Builder setDirection(@HideDirection int direction) {
            this.direction = direction;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setDuration(long duration) {
            this.duration = duration;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder addStartRunnable(@NonNull Runnable runnable) {
            listener.startRunnables.add(runnable);
            return this;
        }

        @SuppressWarnings("unused")
        public Builder addEndRunnable(@NonNull Runnable runnable) {
            listener.endRunnables.add(runnable);
            return this;
        }

        public ViewHider build() {
            ViewHider viewHider = new ViewHider(view, listener, direction, duration);
            listener.startRunnables.add(0, viewHider.startRunnable);
            listener.endRunnables.add(0, viewHider.endRunnable);

            return viewHider;
        }
    }

    private static class Listener implements ViewPropertyAnimatorListener {

        private final List<Runnable> startRunnables = new ArrayList<>();
        private final List<Runnable> endRunnables = new ArrayList<>();

        Listener() {}

        @Override
        public void onAnimationStart(View view) {
            if (!startRunnables.isEmpty()) for (Runnable runnable : startRunnables) runnable.run();
        }

        @Override
        public void onAnimationEnd(View view) {
            if (!endRunnables.isEmpty()) for (Runnable runnable : endRunnables) runnable.run();
        }

        @Override
        public void onAnimationCancel(View view) {}
    }
}