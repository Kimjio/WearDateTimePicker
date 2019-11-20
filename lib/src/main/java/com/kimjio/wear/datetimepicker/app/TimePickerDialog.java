package com.kimjio.wear.datetimepicker.app;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kimjio.wear.datetimepicker.R;
import com.kimjio.wear.datetimepicker.widget.TimePicker;

import java.util.Calendar;

/**
 * A simple dialog containing an {@link com.kimjio.wear.datetimepicker.widget.TimePicker}.
 */
public class TimePickerDialog extends Dialog implements TimePicker.OnTimeSetListener {
    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String IS_24_HOUR = "is24hour";

    private final TimePicker mTimePicker;
    private OnTimeSetListener mTimeSetListener;

    /**
     * Creates a new date picker dialog for the current date using the parent
     * context's default date picker dialog theme.
     *
     * @param context the parent context
     */
    public TimePickerDialog(Context context, boolean is24HourView) {
        this(context, 0, null, Calendar.getInstance(), -1, -1, is24HourView);
    }

    /**
     * Creates a new time picker dialog.
     *
     * @param context      the parent context
     * @param listener     the listener to call when the time is set
     * @param hourOfDay    the initial hour
     * @param minute       the initial minute
     * @param is24HourView whether this is a 24 hour view or AM/PM
     */
    public TimePickerDialog(Context context, OnTimeSetListener listener, int hourOfDay, int minute,
                            boolean is24HourView) {
        this(context, 0, listener, null, hourOfDay, minute, is24HourView);
    }

    /**
     * Creates a new time picker dialog with the specified theme.
     * <p>
     * The theme is overlaid on top of the theme of the parent {@code context}.
     *
     * @param context      the parent context
     * @param themeResId   the resource ID of the theme to apply to this dialog
     * @param listener     the listener to call when the time is set
     * @param hourOfDay    the initial hour
     * @param minute       the initial minute
     * @param is24HourView Whether this is a 24 hour view, or AM/PM.
     */
    public TimePickerDialog(Context context, int themeResId, OnTimeSetListener listener,
                            int hourOfDay, int minute, boolean is24HourView) {
        this(context, themeResId, listener, null, hourOfDay, minute, is24HourView);
    }

    private TimePickerDialog(Context context, int themeResId, OnTimeSetListener listener,
                             Calendar calendar, int hourOfDay, int minute, boolean is24HourView) {
        super(context, themeResId);

        int mInitialHourOfDay;
        int mInitialMinute;
        if (calendar != null) {
            mInitialHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            mInitialMinute = calendar.get(Calendar.MINUTE);
        } else {
            mInitialHourOfDay = hourOfDay;
            mInitialMinute = minute;
        }

        mTimeSetListener = listener;

        setContentView(R.layout.time_picker_dialog);

        mTimePicker = findViewById(R.id.container);
        mTimePicker.setIs24HourView(is24HourView);
        mTimePicker.setHour(mInitialHourOfDay);
        mTimePicker.setMinute(mInitialMinute);
        mTimePicker.setOnTimeChangedListener(this);
    }

    /**
     * Sets the listener to call when the user sets the time.
     *
     * @param listener the listener to call when the user sets the time
     */
    public TimePickerDialog setOnTimeSetListener(@Nullable OnTimeSetListener listener) {
        mTimeSetListener = listener;
        return this;
    }

    /**
     * Returns the {@link TimePicker} contained in this dialog.
     *
     * @return the time picker
     */
    public TimePicker getTimePicker() {
        return mTimePicker;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (mTimeSetListener != null) {
            mTimeSetListener.onTimeSet(mTimePicker, mTimePicker.getHour(),
                    mTimePicker.getMinute());
        }
        dismiss();
    }

    /**
     * Sets the current time.
     *
     * @param hourOfDay    The current hour within the day.
     * @param minuteOfHour The current minute within the hour.
     */
    public void updateTime(int hourOfDay, int minuteOfHour) {
        mTimePicker.setHour(hourOfDay);
        mTimePicker.setMinute(minuteOfHour);
    }

    @NonNull
    @Override
    public Bundle onSaveInstanceState() {
        final Bundle state = super.onSaveInstanceState();
        state.putInt(HOUR, mTimePicker.getHour());
        state.putInt(MINUTE, mTimePicker.getMinute());
        state.putBoolean(IS_24_HOUR, mTimePicker.is24HourView());
        return state;
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final int hour = savedInstanceState.getInt(HOUR);
        final int minute = savedInstanceState.getInt(MINUTE);
        mTimePicker.setIs24HourView(savedInstanceState.getBoolean(IS_24_HOUR));
        mTimePicker.setHour(hour);
        mTimePicker.setMinute(minute);
    }

    /**
     * The callback interface used to indicate the user is done filling in
     * the time (e.g. they clicked on the 'OK' button).
     */
    public interface OnTimeSetListener {

        /**
         * Called when the user is done setting a new time and the dialog has
         * closed.
         *
         * @param view      the view associated with this listener
         * @param hourOfDay the hour that was set
         * @param minute    the minute that was set
         */
        void onTimeSet(TimePicker view, int hourOfDay, int minute);
    }
}
