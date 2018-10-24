package com.tunjid.androidbootstrap.view.animator;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnAdapterChangeListener;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import static androidx.core.content.ContextCompat.getDrawable;

public class ViewPagerIndicatorAnimator {

    private static final int MIN_INDICATORS_TO_SHOW = 2;
    private static final int MIN_VIEWS_IN_LAYOUT = 3;

    private int indicatorCount;
    private int guideLineWidth;

    private final int indicatorHeight;
    private final int indicatorPadding;
    private final int indicatorWidth;

    @DrawableRes
    private final int inActiveDrawable;

    private final ConstraintLayout container;
    private final ViewPager viewPager;
    private final ImageView indicator;
    private final View guide;

    private Implementation implementation;

    private ViewPagerIndicatorAnimator(int indicatorWidth, int indicatorHeight, int indicatorPadding,
                                       @DrawableRes int activeDrawable, @DrawableRes int inActiveDrawable, @DrawableRes int backgroundDrawable,
                                       ConstraintLayout container, ViewPager viewPager, View guide) {

        this.indicatorWidth = indicatorWidth;
        this.indicatorHeight = indicatorHeight;
        this.inActiveDrawable = inActiveDrawable;
        this.indicatorPadding = indicatorPadding;
        this.container = container;
        this.viewPager = viewPager;
        this.guide = guide;
        this.indicator = buildIndicator();
        this.implementation = new Implementation();
        PagerAdapter adapter = viewPager.getAdapter();

        if (adapter != null) buildIndicators(adapter);
        Context context = viewPager.getContext();

        if (backgroundDrawable != 0) guide.setBackground(getDrawable(context, backgroundDrawable));
        if (activeDrawable != 0) this.indicator.setImageResource(activeDrawable);
    }

    public static Builder builder() {
        return new Builder();
    }

    private ImageView buildIndicator() {
        ImageView result = new ImageView(this.container.getContext());
        result.setId(View.generateViewId());

        LayoutParams params = new LayoutParams(indicatorWidth, indicatorHeight);
        params.rightMargin = params.leftMargin = indicatorPadding;
        params.bottomToBottom = guide.getId();

        container.addView(result, params);
        return result;
    }

    private void buildIndicators(@Nullable PagerAdapter newAdapter) {
        if (newAdapter == null) return;

        int pageCount = newAdapter.getCount();

        if (this.container.getChildCount() > MIN_VIEWS_IN_LAYOUT) {
            this.container.removeViews(MIN_VIEWS_IN_LAYOUT, this.indicatorCount);
        }
        if (pageCount < MIN_INDICATORS_TO_SHOW) {
            this.indicator.setVisibility(View.GONE);
            return;
        }
        this.indicator.setVisibility(View.VISIBLE);
        int[] chainIds = new int[pageCount];
        this.indicatorCount = 0;
        while (this.indicatorCount < pageCount) {
            ImageView imageView = buildIndicator();
            chainIds[this.indicatorCount] = imageView.getId();
            imageView.setImageResource(this.inActiveDrawable);
            this.indicatorCount++;
        }

        int guideId = this.guide.getId();
        int indicatorId = this.indicator.getId();

        this.guide.getLayoutParams().width = pageCount * (this.indicatorWidth + (this.indicatorPadding * 2));

        ConstraintSet set = new ConstraintSet();
        set.clone(container);
        set.createHorizontalChain(guideId, ConstraintSet.LEFT, guideId, ConstraintSet.RIGHT, chainIds, null, 2);
        set.connect(indicatorId, ConstraintSet.LEFT, guideId, ConstraintSet.LEFT);
        set.connect(chainIds[0], ConstraintSet.LEFT, guideId, ConstraintSet.LEFT);
        set.connect(chainIds[pageCount - 1], ConstraintSet.RIGHT, guideId, ConstraintSet.RIGHT);
        set.applyTo(container);

        ViewTreeObserver observer = this.guide.getViewTreeObserver();
        if (observer.isAlive()) {
            observer.addOnGlobalLayoutListener(this.implementation);
            guide.invalidate();
        }
    }

    public class Implementation extends DataSetObserver implements OnPageChangeListener, OnAdapterChangeListener, OnGlobalLayoutListener {

        private float lastPositionOffset;

        private Implementation() {
            PagerAdapter adapter = viewPager.getAdapter();
            if (adapter != null) adapter.registerDataSetObserver(this);

            viewPager.addOnPageChangeListener(this);
            viewPager.addOnAdapterChangeListener(this);
        }

        public void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
            if (newAdapter == null) return;
            newAdapter.registerDataSetObserver(this);
            buildIndicators(newAdapter);
        }

        public void onPageScrolled(int position, float fraction, int pixelOffset) {
            float currentPositionOffset = ((float) position) + fraction;
            boolean toTheRight = currentPositionOffset > lastPositionOffset;

            indicator.setTranslationX(getTranslation(toTheRight, toTheRight ? position : position + 1, fraction));
            lastPositionOffset = currentPositionOffset;
        }

        public void onGlobalLayout() {
            ViewTreeObserver observer = guide.getViewTreeObserver();
            if (observer.isAlive()) observer.removeOnGlobalLayoutListener(this);

            guideLineWidth = guide.getWidth();
            int initialIndex = viewPager.getCurrentItem() + 1;
            indicator.setTranslationX(getTranslation(false, initialIndex, 0.0f));
        }

        private float getTranslation(boolean toTheRight, int originalPosition, float fraction) {
            if (!toTheRight) fraction = 1.0f - fraction;

            float chunkWidth = ((float) guideLineWidth) / ((float) viewPager.getAdapter().getCount());
            float currentChunk = ((float) originalPosition) * chunkWidth;
            float diff = chunkWidth * fraction;

            return toTheRight ? currentChunk + diff : currentChunk - diff;
        }

        public void onPageSelected(int position) {
        }

        public void onPageScrollStateChanged(int state) {
        }
    }

    public static class Builder {
        private int activeDrawable;
        private int backgroundDrawable;
        private int inActiveDrawable;
        private int indicatorHeight;
        private int indicatorPadding;
        private int indicatorWidth;
        private ConstraintLayout container;
        private ViewPager viewPager;
        private View guideLine;

        public Builder setIndicatorWidth(int indicatorWidth) {
            this.indicatorWidth = indicatorWidth;
            return this;
        }

        public Builder setIndicatorHeight(int indicatorHeight) {
            this.indicatorHeight = indicatorHeight;
            return this;
        }

        public Builder setIndicatorPadding(int indicatorPadding) {
            this.indicatorPadding = indicatorPadding;
            return this;
        }

        public Builder setActiveDrawable(int activeDrawable) {
            this.activeDrawable = activeDrawable;
            return this;
        }

        public Builder setInActiveDrawable(int inActiveDrawable) {
            this.inActiveDrawable = inActiveDrawable;
            return this;
        }

        public Builder setBackgroundDrawable(int backgroundDrawable) {
            this.backgroundDrawable = backgroundDrawable;
            return this;
        }

        public Builder setContainer(ConstraintLayout container) {
            this.container = container;
            return this;
        }

        public Builder setViewPager(ViewPager viewPager) {
            this.viewPager = viewPager;
            return this;
        }

        public Builder setGuideLine(View guideLine) {
            this.guideLine = guideLine;
            return this;
        }

        public ViewPagerIndicatorAnimator build() {
            return new ViewPagerIndicatorAnimator(
                    indicatorWidth, indicatorHeight, indicatorPadding,
                    activeDrawable, inActiveDrawable, backgroundDrawable,
                    container, viewPager, guideLine);
        }
    }
}
