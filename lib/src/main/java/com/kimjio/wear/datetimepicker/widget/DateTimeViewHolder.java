package com.kimjio.wear.datetimepicker.widget;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.widget.TextView;

class DateTimeViewHolder extends WearableListView.ViewHolder {
    private final ObjectAnimator mScaleDownAnimator;
    private final ObjectAnimator mScaleUpAnimator;
    private final PropertyValuesHolder mScaleXDownValues;
    private final PropertyValuesHolder mScaleXUpValues = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.0f);
    private final PropertyValuesHolder mScaleYDownValues;
    private final PropertyValuesHolder mScaleYUpValues = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.0f);

    DateTimeViewHolder(View itemView) {
        super(itemView);
        this.mScaleUpAnimator = ObjectAnimator.ofPropertyValuesHolder(itemView, this.mScaleXUpValues, this.mScaleYUpValues).setDuration(150);
        this.mScaleXDownValues = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.0f);
        this.mScaleYDownValues = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.0f);
        this.mScaleDownAnimator = ObjectAnimator.ofPropertyValuesHolder(itemView, this.mScaleXDownValues, this.mScaleYDownValues).setDuration(150);
    }

    protected float getCenterScale() {
        return 1.0f;
    }

    protected float getOffCenterScale() {
        return 0.6f;
    }

    @Override
    protected void onCenterProximity(boolean isCentralItem, boolean animate) {
        TextView textView = (TextView) this.itemView;
        textView.setSelected(isCentralItem);
        textView.setActivated(((View) textView.getParent()).isActivated());
        if (!animate) {
            this.mScaleUpAnimator.cancel();
            this.mScaleDownAnimator.cancel();
            textView.setScaleX(isCentralItem ? getCenterScale() : getOffCenterScale());
            textView.setScaleY(isCentralItem ? getCenterScale() : getOffCenterScale());
        } else if (isCentralItem) {
            this.mScaleDownAnimator.cancel();
            if (!this.mScaleUpAnimator.isRunning()) {
                this.mScaleXUpValues.setFloatValues(textView.getScaleX(), getCenterScale());
                this.mScaleYUpValues.setFloatValues(textView.getScaleY(), getCenterScale());
                this.mScaleUpAnimator.start();
            }
        } else {
            this.mScaleUpAnimator.cancel();
            if (!this.mScaleDownAnimator.isRunning()) {
                this.mScaleXDownValues.setFloatValues(textView.getScaleX(), getOffCenterScale());
                this.mScaleYDownValues.setFloatValues(textView.getScaleY(), getOffCenterScale());
                this.mScaleDownAnimator.start();
            }
        }
    }
}
