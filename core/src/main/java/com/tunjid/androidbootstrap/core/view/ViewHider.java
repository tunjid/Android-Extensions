package com.tunjid.androidbootstrap.core.view;


import android.graphics.Point;
import android.support.annotation.IntDef;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Interpolator;

import java.lang.annotation.Retention;

import static android.content.Context.WINDOW_SERVICE;
import static android.view.View.VISIBLE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Translates a view offscreen, useful for adding the quick return pattern to a view.
 * <p>
 * Created by tj.dahunsi on 2/19/16.
 */
public class ViewHider {

    private static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();

    private boolean isVisible = true;
    private final @HideDirection int direction;
    private final long duration;
    private final View view;

    @Retention(SOURCE)
    @IntDef({LEFT, TOP, RIGHT, BOTTOM})
    @SuppressWarnings("WeakerAccess")
    public @interface HideDirection {
    }

    public static final int LEFT = 0;
    public static final int TOP = 1;
    public static final int RIGHT = 2;
    public static final int BOTTOM = 3;

    public ViewHider(View view, @HideDirection int direction) {
        this(view, direction, 200L);
    }

    @SuppressWarnings("WeakerAccess")
    public ViewHider(View view, @HideDirection int direction, long duration) {
        this.view = view;
        this.duration = duration;
        this.direction = direction;
    }

    public void show() {
        toggle(true);
    }

    public void hide() {
        toggle(false);
    }

    private void toggle(final boolean visible) {
        if (this.isVisible != visible) {

            this.isVisible = visible;
            final ViewTreeObserver observer = view.getViewTreeObserver();

            // View hasn't been laid out yet and has it's observer attached
            if (view.getHeight() == 0 && observer.isAlive()) {
                observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        if (observer.isAlive()) observer.removeOnPreDrawListener(this);
                        // toggle as soon as the view is visible
                        toggle(visible);
                        return true;
                    }
                });
                return;
            }

            float displacement = visible ? 0 : getDistanceOffscreen();
            ViewPropertyAnimatorCompat animator = ViewCompat.animate(view)
                    .setDuration(duration)
                    .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);

            if (view.getVisibility() == VISIBLE) {
                if (direction == LEFT || direction == RIGHT) animator.translationX(displacement);
                else animator.translationY(displacement);
            }

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
}