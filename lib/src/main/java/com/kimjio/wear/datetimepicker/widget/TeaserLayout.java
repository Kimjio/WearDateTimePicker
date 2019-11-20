package com.kimjio.wear.datetimepicker.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.kimjio.wear.datetimepicker.R;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO WearableListView to WearableRecyclerView
 */
class TeaserLayout extends FrameLayout {
    private final GestureWatcher mGestureWatcher = new GestureWatcher();
    private int mCenterIndex = -1;
    private ActivePassiveSetter mActivePassiveSetter;
    private CenterIndexListener mCenterIndexListener;
    private GestureDetector mGestureDetector;
    private Boolean mHorizontalScrollDetected;
    private float mStartX;
    private float mStartY;
    private float mTeaserPxFromCenterHiding;
    private int mTouchSlopSq;

    TeaserLayout(Context context) {
        this(context, null);
    }

    TeaserLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    TeaserLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    TeaserLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.mGestureDetector = new GestureDetector(context, this.mGestureWatcher);
        this.mGestureDetector.setIsLongpressEnabled(false);
        this.mTouchSlopSq = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mTouchSlopSq *= this.mTouchSlopSq;
        this.mTeaserPxFromCenterHiding = (float) getResources().getDimensionPixelOffset(R.dimen.teaser_center_offset);
        setActivePassiveSetter(new ActivePassiveSetter());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.mCenterIndex != -1) {
            immediatelyCenter(this.mCenterIndex);
        }
    }

    public boolean canScrollHorizontally(int direction) {
        if (direction > 0 && this.mCenterIndex < getChildCount() - 1) {
            return true;
        }
        return direction < 0 && this.mCenterIndex > 0;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == 0) {
            this.mStartX = event.getX();
            this.mStartY = event.getY();
            this.mHorizontalScrollDetected = null;
            this.mGestureDetector.onTouchEvent(event);
            return false;
        }
        if (event.getActionMasked() == 2 && this.mHorizontalScrollDetected == null) {
            float deltaX = Math.abs(this.mStartX - event.getX());
            float deltaY = Math.abs(this.mStartY - event.getY());
            if ((deltaX * deltaX) + (deltaY * deltaY) > ((float) this.mTouchSlopSq)) {
                this.mHorizontalScrollDetected = deltaX > deltaY;
            }
        }
        if (this.mHorizontalScrollDetected == null || !this.mHorizontalScrollDetected) {
            return false;
        }
        return this.mGestureDetector.onTouchEvent(event);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == 1) {
            this.mGestureWatcher.onUp();
        }
        return this.mGestureDetector.onTouchEvent(event);
    }

    private void setActivePassiveSetter(ActivePassiveSetter ap) {
        this.mActivePassiveSetter = ap;
        if (this.mActivePassiveSetter != null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (i == this.mCenterIndex) {
                    this.mActivePassiveSetter.onActivate(child);
                } else {
                    this.mActivePassiveSetter.onDeactivate(child);
                }
            }
        }
    }

    public void setCenterIndexListener(CenterIndexListener listener) {
        this.mCenterIndexListener = listener;
    }

    public void setCenterIndex(int index, boolean animate) {
        if (index >= 0 && index < getChildCount() && this.mCenterIndex != index) {
            if (this.mCenterIndex < 0 && this.mActivePassiveSetter != null) {
                for (int i = 0; i < getChildCount(); i++) {
                    if (i != index) {
                        this.mActivePassiveSetter.onDeactivate(getChildAt(i));
                    }
                }
            } else if (this.mActivePassiveSetter != null) {
                this.mActivePassiveSetter.onDeactivate(getChildAt(this.mCenterIndex));
            }
            this.mCenterIndex = index;
            if (this.mActivePassiveSetter != null) {
                this.mActivePassiveSetter.onActivate(getChildAt(this.mCenterIndex));
            }
            if (animate) {
                animateToCenter(index);
            } else {
                immediatelyCenter(index);
            }
            if (this.mCenterIndexListener != null) {
                this.mCenterIndexListener.onCenterIndexChanged(this.mCenterIndex);
            }
        }
    }

    private void animateToCenter(int index) {
        createAnimatorSet(index).start();
    }

    private AnimatorSet createAnimatorSet(int index) {
        float newAlpha;
        TargetState targetState = getTargetState(index);
        List<Animator> animators = new ArrayList<>();
        for (int i = 0; i < targetState.getCount(); i++) {
            View child = getChildAt(i);
            float oldX = child.getX();
            float newX = (float) targetState.x[i];
            PropertyValuesHolder xValues = PropertyValuesHolder.ofFloat(View.X, oldX, newX);
            float newScale = targetState.scale[i];
            float oldScaleX = child.getScaleX();
            PropertyValuesHolder scaleXs = PropertyValuesHolder.ofFloat(View.SCALE_X, oldScaleX, newScale);
            float oldScaleY = child.getScaleY();
            PropertyValuesHolder scaleYs = PropertyValuesHolder.ofFloat(View.SCALE_Y, oldScaleY, newScale);
            if (child instanceof ViewGroup) {
                WearableListView listView = findListInGroup((ViewGroup) child);
                if (listView != null) {
                    for (int j = 0; j < listView.getChildCount(); j++) {
                        View listChild = listView.getChildAt(j);
                        float oldAlpha = listChild.getAlpha();
                        if (listChild.isSelected()) {
                            newAlpha = targetState.centerOpacity[i];
                        } else {
                            newAlpha = targetState.offCenterOpacity[i];
                        }
                        animators.add(ObjectAnimator.ofPropertyValuesHolder(listChild, PropertyValuesHolder.ofFloat(View.ALPHA, oldAlpha, newAlpha)));
                    }
                }
            }
            animators.add(ObjectAnimator.ofPropertyValuesHolder(child, xValues, scaleXs, scaleYs));
        }
        AnimatorSet animatorSet = new AnimatorSet().setDuration(300);
        animatorSet.playTogether(animators);
        return animatorSet;
    }

    private void immediatelyCenter(int index) {
        float f;
        TargetState targetState = getTargetState(index);
        for (int i = 0; i < targetState.getCount(); i++) {
            View child = getChildAt(i);
            child.setX((float) targetState.x[i]);
            child.setScaleX(targetState.scale[i]);
            child.setScaleY(targetState.scale[i]);
            if (child instanceof ViewGroup) {
                WearableListView listView = findListInGroup((ViewGroup) child);
                if (listView != null) {
                    for (int j = 0; j < listView.getChildCount(); j++) {
                        View v = listView.getChildAt(j);
                        if (v.isSelected()) {
                            f = targetState.centerOpacity[i];
                        } else {
                            f = targetState.offCenterOpacity[i];
                        }
                        v.setAlpha(f);
                    }
                }
            }
        }
    }

    private WearableListView findListInGroup(ViewGroup group) {
        if (group instanceof WearableListView) {
            return (WearableListView) group;
        }
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof ViewGroup) {
                WearableListView listView = findListInGroup((ViewGroup) child);
                if (listView != null) {
                    return listView;
                }
            }
        }
        return null;
    }

    protected float getTargetScaleForView(int index, boolean activated) {
        float mActivatedScale = 1.0f;
        float mPassiveScale = 0.5f;
        return activated ? mActivatedScale : mPassiveScale;
    }

    protected float getTargetCenterOpacityForView(int index, boolean activated) {
        return 1.0f;
    }

    protected float getTargetOffCenterOpacityForView(int index, boolean activated) {
        return activated ? 1.0f : 0.0f;
    }

    private TargetState getTargetState(int centerIndex) {
        TargetState targetState = new TargetState(getChildCount());
        int totalWidth = getWidth();
        int halfWidth = totalWidth / 2;
        targetState.x[centerIndex] = halfWidth - (getChildAt(centerIndex).getWidth() / 2);
        View leftChild = getChildAt(centerIndex - 1);
        if (leftChild != null) {
            targetState.x[centerIndex - 1] = -Math.round(((float) (leftChild.getWidth() / 2)) - this.mTeaserPxFromCenterHiding);
        }
        View rightChild = getChildAt(centerIndex + 1);
        if (rightChild != null) {
            int rightChildWidth = rightChild.getWidth();
            targetState.x[centerIndex + 1] = totalWidth - (rightChildWidth - Math.round(((float) (rightChildWidth / 2)) - this.mTeaserPxFromCenterHiding));
        }
        for (int i = centerIndex - 2; i >= 0; i--) {
            targetState.x[i] = ((targetState.x[i + 1] + (getChildAt(i + 1).getWidth() / 2)) - halfWidth) - Math.round(((float) (getChildAt(i).getWidth() / 2)) - this.mTeaserPxFromCenterHiding);
        }
        for (int i = centerIndex + 2; i < targetState.getCount(); i++) {
            int previousChildX = targetState.x[i - 1];
            int halfPreviousChildWidth = getChildAt(i - 1).getWidth() / 2;
            int childWidth = getChildAt(i).getWidth();
            targetState.x[i] = ((previousChildX + halfPreviousChildWidth) + halfWidth) - (childWidth - Math.round(((float) (childWidth / 2)) - this.mTeaserPxFromCenterHiding));
        }
        for (int i = 0; i < targetState.getCount(); i++) {
            targetState.scale[i] = getTargetScaleForView(i, i == centerIndex);
            targetState.centerOpacity[i] = getTargetCenterOpacityForView(i, i == centerIndex);
            targetState.offCenterOpacity[i] = getTargetOffCenterOpacityForView(i, i == centerIndex);
        }
        return targetState;
    }

    public interface CenterIndexListener {
        void onCenterIndexChanged(int i);
    }

    private static class TargetState {
        private final float[] centerOpacity;
        private final float[] offCenterOpacity;
        private final float[] scale;
        private final int[] x;

        private TargetState(int childCount) {
            this.x = new int[childCount];
            this.scale = new float[childCount];
            this.centerOpacity = new float[childCount];
            this.offCenterOpacity = new float[childCount];
        }

        private int getCount() {
            return this.x.length;
        }
    }

    private static class ActivePassiveSetter {

        private void onActivate(View childView) {
            setEnabled(childView, true);
            childView.setActivated(true);
            childView.setClickable(true);
            childView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }

        private void onDeactivate(View childView) {
            setEnabled(childView, false);
            childView.setActivated(false);
            childView.setClickable(false);
            childView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        }

        private void setEnabled(View view, boolean enabled) {
            view.setEnabled(enabled);
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    setEnabled(viewGroup.getChildAt(i), enabled);
                }
            }
        }
    }

    private class GestureWatcher extends SimpleOnGestureListener {
        private float mScrollDistance;
        private AnimatorSet mScrollLeftAnimatorSet;
        private float mScrollPercentage;
        private AnimatorSet mScrollRightAnimatorSet;

        @Override
        public boolean onDown(MotionEvent event) {
            this.mScrollDistance = 0.0f;
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            int totalWidth = TeaserLayout.this.getWidth();
            float tapThreshold = 0.25f * ((float) totalWidth);
            float x = event.getX();
            if (x < tapThreshold && canMoveLeft()) {
                TeaserLayout.this.setCenterIndex(TeaserLayout.this.mCenterIndex - 1, true);
            } else if (x + tapThreshold > ((float) totalWidth) && canMoveRight()) {
                TeaserLayout.this.setCenterIndex(TeaserLayout.this.mCenterIndex + 1, true);
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            this.mScrollDistance = e2.getX() - e1.getX();
            this.mScrollPercentage = Math.min(1.0f, Math.abs(this.mScrollDistance) / (((float) TeaserLayout.this.getWidth()) / 2.0f));
            if (this.mScrollDistance < 0.0f && canMoveRight()) {
                if (this.mScrollRightAnimatorSet == null) {
                    this.mScrollRightAnimatorSet = TeaserLayout.this.createAnimatorSet(TeaserLayout.this.mCenterIndex + 1);
                }
                for (Animator a : this.mScrollRightAnimatorSet.getChildAnimations()) {
                    ValueAnimator va = (ValueAnimator) a;
                    va.setCurrentPlayTime(Math.round(this.mScrollPercentage * ((float) va.getDuration())));
                }
            } else if (this.mScrollDistance == 0.0f) {
                TeaserLayout.this.immediatelyCenter(TeaserLayout.this.mCenterIndex);
            } else if (this.mScrollDistance > 0.0f && canMoveLeft()) {
                if (this.mScrollLeftAnimatorSet == null) {
                    this.mScrollLeftAnimatorSet = TeaserLayout.this.createAnimatorSet(TeaserLayout.this.mCenterIndex - 1);
                }
                for (Animator a2 : this.mScrollLeftAnimatorSet.getChildAnimations()) {
                    ValueAnimator va2 = (ValueAnimator) a2;
                    va2.setCurrentPlayTime(Math.round(this.mScrollPercentage * (float) va2.getDuration()));
                }
            }
            return true;
        }

        private void onUp() {
            if (this.mScrollDistance >= 0.0f || this.mScrollRightAnimatorSet == null) {
                if (this.mScrollDistance > 0.0f && this.mScrollLeftAnimatorSet != null) {
                    if (this.mScrollPercentage >= 0.3f) {
                        final int nextLeftIndex = TeaserLayout.this.mCenterIndex - 1;
                        ValueAnimator animator = ValueAnimator.ofInt(Math.round(this.mScrollPercentage * (float) mScrollLeftAnimatorSet.getDuration()), 300);
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                if (mScrollLeftAnimatorSet != null)
                                    for (Animator a : mScrollLeftAnimatorSet.getChildAnimations()) {
                                        ((ValueAnimator) a).setCurrentPlayTime((int) animation.getAnimatedValue());
                                    }
                            }
                        });
                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                TeaserLayout.this.setCenterIndex(nextLeftIndex, false);
                                mScrollLeftAnimatorSet = null;
                            }
                        });
                        animator.setDuration(300);
                        animator.start();
                    } else {
                        for (Animator a : this.mScrollLeftAnimatorSet.getChildAnimations()) {
                            ((ValueAnimator) a).reverse();
                        }
                    }
                }
            } else if (this.mScrollPercentage >= 0.3f) {
                final int nextRightIndex = TeaserLayout.this.mCenterIndex + 1;
                ValueAnimator animator = ValueAnimator.ofInt(Math.round(this.mScrollPercentage * (float) mScrollRightAnimatorSet.getDuration()), 300);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (mScrollRightAnimatorSet != null)
                            for (Animator a : mScrollRightAnimatorSet.getChildAnimations()) {
                                ((ValueAnimator) a).setCurrentPlayTime((int) animation.getAnimatedValue());
                            }
                    }
                });
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        TeaserLayout.this.setCenterIndex(nextRightIndex, false);
                        mScrollRightAnimatorSet = null;
                    }
                });
                animator.setDuration(300);
                animator.start();
            } else {
                for (Animator animator : this.mScrollRightAnimatorSet.getChildAnimations()) {
                    ((ValueAnimator) animator).reverse();
                }
            }
        }

        private boolean canMoveLeft() {
            return TeaserLayout.this.mCenterIndex > 0;
        }

        private boolean canMoveRight() {
            return TeaserLayout.this.mCenterIndex < TeaserLayout.this.getChildCount() + -1;
        }
    }
}
