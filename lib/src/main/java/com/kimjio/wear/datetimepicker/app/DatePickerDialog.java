package com.kimjio.wear.datetimepicker.app;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.kimjio.wear.datetimepicker.R;
import com.kimjio.wear.datetimepicker.widget.DatePicker;

import java.util.Calendar;

/**
 * A simple dialog containing an {@link com.kimjio.wear.datetimepicker.widget.DatePicker}.
 */
public class DatePickerDialog extends Dialog implements DatePicker.OnDateSetListener {
    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";

    private final DatePicker mDatePicker;

    private OnDateSetListener mDateSetListener;

    /**
     * Creates a new date picker dialog for the current date using the parent
     * context's default date picker dialog theme.
     *
     * @param context the parent context
     */
    public DatePickerDialog(@NonNull Context context) {
        this(context, 0, null, Calendar.getInstance(), -1, -1, -1);
    }

    /**
     * Creates a new date picker dialog for the current date.
     *
     * @param context    the parent context
     * @param themeResId the resource ID of the theme against which to inflate this dialog
     */
    public DatePickerDialog(@NonNull Context context, @StyleRes int themeResId) {
        this(context, themeResId, null, Calendar.getInstance(), -1, -1, -1);
    }

    /**
     * Creates a new date picker dialog for the specified date using the parent
     * context's default date picker dialog theme.
     *
     * @param context    the parent context
     * @param listener   the listener to call when the user sets the date
     * @param year       the initially selected year
     * @param month      the initially selected month (0-11 for compatibility with
     *                   {@link Calendar#MONTH})
     * @param dayOfMonth the initially selected day of month (1-31, depending
     *                   on month)
     */
    public DatePickerDialog(@NonNull Context context, @Nullable OnDateSetListener listener,
                            int year, int month, int dayOfMonth) {
        this(context, 0, listener, null, year, month, dayOfMonth);
    }

    /**
     * Creates a new date picker dialog for the specified date.
     *
     * @param context     the parent context
     * @param themeResId  the resource ID of the theme against which to inflate this dialog
     *                    {@code context}'s default alert dialog theme
     * @param listener    the listener to call when the user sets the date
     * @param year        the initially selected year
     * @param monthOfYear the initially selected month of the year (0-11 for
     *                    compatibility with {@link Calendar#MONTH})
     * @param dayOfMonth  the initially selected day of month (1-31, depending
     *                    on month)
     */
    public DatePickerDialog(@NonNull Context context, @StyleRes int themeResId,
                            @Nullable OnDateSetListener listener, int year, int monthOfYear, int dayOfMonth) {
        this(context, themeResId, listener, null, year, monthOfYear, dayOfMonth);
    }

    private DatePickerDialog(@NonNull Context context, @StyleRes int themeResId,
                             @Nullable OnDateSetListener listener, @Nullable Calendar calendar, int year,
                             int monthOfYear, int dayOfMonth) {
        super(context, themeResId);

        if (calendar != null) {
            year = calendar.get(Calendar.YEAR);
            monthOfYear = calendar.get(Calendar.MONTH);
            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        }

        setContentView(R.layout.date_picker_dialog);

        mDatePicker = findViewById(R.id.container);
        mDatePicker.init(year, monthOfYear, dayOfMonth, this);

        mDateSetListener = listener;
    }

    @Override
    public void onDateSet(@NonNull DatePicker view, int year, int month, int dayOfMonth) {
        if (mDateSetListener != null) {
            mDateSetListener.onDateSet(mDatePicker, mDatePicker.getYear(),
                    mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
        }
        dismiss();
    }

    /**
     * Sets the listener to call when the user sets the date.
     *
     * @param listener the listener to call when the user sets the date
     */
    public DatePickerDialog setOnDateSetListener(@Nullable OnDateSetListener listener) {
        mDateSetListener = listener;
        return this;
    }

    /**
     * Returns the {@link DatePicker} contained in this dialog.
     *
     * @return the date picker
     */
    @NonNull
    public DatePicker getDatePicker() {
        return mDatePicker;
    }

    /**
     * Sets the current date.
     *
     * @param year       the year
     * @param month      the month (0-11 for compatibility with
     *                   {@link Calendar#MONTH})
     * @param dayOfMonth the day of month (1-31, depending on month)
     */
    public void updateDate(int year, int month, int dayOfMonth) {
        mDatePicker.updateDate(year, month, dayOfMonth);
    }

    @NonNull
    @Override
    public Bundle onSaveInstanceState() {
        final Bundle state = super.onSaveInstanceState();
        state.putInt(YEAR, mDatePicker.getYear());
        state.putInt(MONTH, mDatePicker.getMonth());
        state.putInt(DAY, mDatePicker.getDayOfMonth());
        return state;
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final int year = savedInstanceState.getInt(YEAR);
        final int month = savedInstanceState.getInt(MONTH);
        final int day = savedInstanceState.getInt(DAY);
        mDatePicker.init(year, month, day, this);
    }

    /**
     * The listener used to indicate the user has finished selecting a date.
     */
    public interface OnDateSetListener {
        /**
         * @param view       the picker associated with the dialog
         * @param year       the selected year
         * @param month      the selected month (0-11 for compatibility with
         *                   {@link Calendar#MONTH})
         * @param dayOfMonth the selected day of the month (1-31, depending on
         *                   month)
         */
        void onDateSet(DatePicker view, int year, int month, int dayOfMonth);
    }
}
