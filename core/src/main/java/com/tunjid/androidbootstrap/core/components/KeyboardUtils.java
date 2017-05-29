package com.tunjid.androidbootstrap.core.components;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

/**
 * According to Android Documentation, an app that is set to fullscreen, i.e can draw behind the
 * status bar cannot use {@link android.view.WindowManager.LayoutParams#SOFT_INPUT_ADJUST_RESIZE}
 * <p>
 * This poses a problem for apps that need to show the keyboard when an app is fullscreen as
 * elements, especially in a scrollable container, that are hidden by the keyboard are out of the
 * user's view.
 * <p>
 * This class therefore forces the visbible {@link android.view.Window} of the screen to be resized
 * whenever the virtual keyboard appears.
 *
 * @see <a href="http://stackoverflow.com/a/9108219/325479">StackOverflow</a>
 * <p>
 * Created by tj.dahunsi on 5/20/17.
 */

public final class KeyboardUtils {

    private final ViewProvider viewProvider;

    @SuppressWarnings("WeakerAccess")
    public KeyboardUtils(ViewProvider viewProvider) {
        this.viewProvider = viewProvider;
    }

    public KeyboardUtils(Fragment fragment) {
        this(fromFragment(fragment));
    }

    public boolean initialize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && viewProvider.getDecorView() != null) {
            viewProvider.getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
            return true;
        }
        return false;
    }

    public boolean stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && viewProvider.getDecorView() != null) {
            viewProvider.getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
            return true;
        }
        return false;
    }

    private final ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            View decorView = viewProvider.getDecorView();
            View contentView = viewProvider.getContentView();

            if (decorView == null) return;

            // Rect will have values of the visible area left
            Rect rect = new Rect();

            decorView.getWindowVisibleDisplayFrame(rect);

            // Get Screen height and calculate the difference between the visble and hidden areas
            int height = decorView.getContext().getResources().getDisplayMetrics().heightPixels;
            int diff = height - rect.bottom;

            // If the useable screen height differs from the total screen height, assume the
            // virtual keyboard is visible.
            if (diff != 0 && contentView != null) {
                // Check if the padding was not previously set on the contentView, if not, add it
                if (contentView.getPaddingBottom() != diff) contentView.setPadding(0, 0, 0, diff);
            }
            else if (contentView != null) {
                // Reset padding
                if (contentView.getPaddingBottom() != 0) contentView.setPadding(0, 0, 0, 0);
            }
        }
    };

    @SuppressWarnings("unused")
    public static void hideKeyboard(Activity activity) {
        if (activity != null && activity.getCurrentFocus() != null) {
            InputMethodManager manager = (InputMethodManager)
                    activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    private static ViewProvider fromFragment(final Fragment fragment) {
        return new ViewProvider() {
            @Nullable
            @Override
            public View getDecorView() {
                return fragment.getActivity() != null
                        ? fragment.getActivity().getWindow().getDecorView()
                        : null;
            }

            @Nullable
            @Override
            public View getContentView() {
                return fragment.getView();
            }
        };
    }

    @SuppressWarnings("unused")
    private static ViewProvider fromActivity(final Activity activity) {
        return new ViewProvider() {
            @Nullable
            @Override
            public View getDecorView() {
                return activity.getWindow().getDecorView();
            }

            @Nullable
            @Override
            public View getContentView() {
                return activity.getWindow().getDecorView().getRootView();
            }
        };
    }

    @SuppressWarnings("WeakerAccess")
    public interface ViewProvider {
        @Nullable
        View getDecorView();

        @Nullable
        View getContentView();
    }
}
