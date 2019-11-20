package com.kimjio.wear.datetimepicker.widget;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kimjio.wear.datetimepicker.R;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DatePicker extends TeaserLayout {
    private final Calendar mValidationCalendar;
    private DayAdapter mDayAdapter;
    private WearableListView mDayPicker;
    private int mDayPickerIndex;
    private View mDoneView;
    private OnDateSetListener mListener;
    private int mMaxDay;
    private int mMaxMonth;
    private int mMaxYear;
    private int mMinDay;
    private int mMinMonth;
    private int mMinYear;
    private MonthAdapter mMonthAdapter;
    private WearableListView mMonthPicker;
    private int mMonthPickerIndex;
    private int mSelectedDayIndex;
    private int mSelectedMonthIndex;
    private int mSelectedYearIndex;
    private YearAdapter mYearAdapter;
    private WearableListView mYearPicker;
    private int mYearPickerIndex;

    public DatePicker(Context context) {
        this(context, null);
    }

    public DatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DatePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DatePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mValidationCalendar = new GregorianCalendar();
        inflate(getContext(), R.layout.date_picker, this);
        init();
    }

    private static int clamp(int toClamp, int min, int max) {
        return Math.min(Math.max(toClamp, min), max);
    }

    private void init() {
        char[] order = DateFormat.getDateFormatOrder(getContext());
        setupPicker(order, 0, (WearableListView) findViewById(R.id.first_picker));
        setupPicker(order, 1, (WearableListView) findViewById(R.id.second_picker));
        setupPicker(order, 2, (WearableListView) findViewById(R.id.third_picker));
        this.mDoneView = findViewById(R.id.check);
        this.mDoneView.setOnClickListener(new DoneCheckListener());
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int monthOfYear = c.get(Calendar.MONTH);
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        setMinDate(year - 100, 0, 1, false);
        setMaxDate(year + 100, 11, 31, false);
        updateDate(year, monthOfYear, dayOfMonth);
        setCenterIndex(0, false);
        setOnDateChangedListener(null);
    }

    public void init(int year, int month, int day, OnDateSetListener onDateSetListener) {
        setMinDate(year - 100, 0, 1, false);
        setMaxDate(year + 100, 11, 31, false);
        updateDate(year, month, day);
        setCenterIndex(0, false);
        setOnDateChangedListener(onDateSetListener);
    }

    public void setOnDateChangedListener(OnDateSetListener onDateSetListener) {
        this.mListener = onDateSetListener;
    }

    public int getMonth() {
        return this.mMonthAdapter.getMonth(this.mSelectedMonthIndex);
    }

    public int getYear() {
        return this.mYearAdapter.getYear(this.mSelectedYearIndex);
    }

    public int getDayOfMonth() {
        return this.mDayAdapter.getDay(this.mSelectedDayIndex);
    }

    public void setMinDate(int year, int month, int day) {
        setMinDate(year, month, day, true);
    }

    private void setMinDate(int year, int month, int day, boolean update) {
        this.mMinYear = year;
        this.mMinMonth = month;
        this.mMinDay = day;
        int currentYear = getYear();
        int currentMonth = getMonth();
        int currentDay = getDayOfMonth();
        this.mYearAdapter.setMinYear(year);
        if (update) {
            updateDate(currentYear, currentMonth, currentDay);
        }
    }

    public void setMaxDate(int year, int month, int day) {
        setMaxDate(year, month, day, true);
    }

    private void setMaxDate(int year, int month, int day, boolean update) {
        this.mMaxYear = year;
        this.mMaxMonth = month;
        this.mMaxDay = day;
        int currentYear = getYear();
        int currentMonth = getMonth();
        int currentDay = getDayOfMonth();
        this.mYearAdapter.setMaxYear(year);
        if (update) {
            updateDate(currentYear, currentMonth, currentDay);
        }
    }

    public void updateDate(int year, int month, int dayOfMonth) {
        moveYearTo(year);
        moveMonthTo(month);
        moveDayTo(dayOfMonth);
    }

    private int getNumDaysInCurrentMonth() {
        this.mValidationCalendar.set(this.mYearAdapter.getYear(this.mSelectedYearIndex), this.mMonthAdapter.getMonth(this.mSelectedMonthIndex), 1);
        return this.mValidationCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    @Override
    protected float getTargetScaleForView(int index, boolean activated) {
        if (index != this.mMonthPickerIndex) {
            return super.getTargetScaleForView(index, activated);
        }
        return activated ? 1.0f : 0.85f;
    }

    private void setupPicker(char[] dateOrder, int index, WearableListView view) {
        switch (dateOrder[index]) {
            case 'M':
                setupMonthPicker(view);
                this.mMonthPickerIndex = index;
                break;
            case 'd':
                setupDayPicker(view);
                this.mDayPickerIndex = index;
                break;
            case 'y':
                setupYearPicker(view);
                this.mYearPickerIndex = index;
                break;
        }
    }

    private void setupDayPicker(WearableListView view) {
        this.mDayPicker = view;
        this.mDayAdapter = new DayAdapter(getContext());
        this.mDayPicker.setAdapter(this.mDayAdapter);
        this.mDayPicker.addOnCentralPositionChangedListener(new DayScrollListener());
        this.mDayPicker.setClickListener(new AdvanceFromDayListener());
    }

    private void setupMonthPicker(WearableListView view) {
        this.mMonthPicker = view;
        this.mMonthAdapter = new MonthAdapter(getContext());
        this.mMonthPicker.setAdapter(this.mMonthAdapter);
        this.mMonthPicker.addOnCentralPositionChangedListener(new MonthScrollListener());
        this.mMonthPicker.setClickListener(new AdvanceFromMonthListener());
    }

    private void setupYearPicker(WearableListView view) {
        this.mYearPicker = view;
        this.mYearAdapter = new YearAdapter(getContext());
        this.mYearPicker.setAdapter(this.mYearAdapter);
        this.mYearPicker.addOnCentralPositionChangedListener(new YearScrollListener());
        this.mYearPicker.setClickListener(new AdvanceFromYearListener());
    }

    private void moveYearTo(int year) {
        int newYear = this.mYearAdapter.clamp(year);
        if (this.mSelectedYearIndex != this.mYearAdapter.getYearIndex(newYear)) {
            this.mSelectedYearIndex = this.mYearAdapter.getYearIndex(newYear);
            this.mYearPicker.scrollToPosition(this.mSelectedYearIndex);
        }
        int month = this.mMonthAdapter.getMonth(this.mSelectedMonthIndex);
        if (newYear == this.mMinYear) {
            this.mMonthAdapter.setMinMonth(this.mMinMonth);
        } else {
            this.mMonthAdapter.setMinMonth(0);
        }
        if (newYear == this.mMaxYear) {
            this.mMonthAdapter.setMaxMonth(this.mMaxMonth);
        } else {
            this.mMonthAdapter.setMaxMonth(11);
        }
        moveMonthTo(month);
    }

    private void moveMonthTo(int month) {
        int newMonth = this.mMonthAdapter.clamp(month);
        if (newMonth != this.mMonthAdapter.getMonth(this.mSelectedMonthIndex)) {
            this.mSelectedMonthIndex = this.mMonthAdapter.getMonthIndex(newMonth);
            this.mMonthPicker.scrollToPosition(this.mMonthAdapter.getMonthIndex(newMonth));
        }
        int year = this.mYearAdapter.getYear(this.mSelectedYearIndex);
        int day = this.mDayAdapter.getDay(this.mSelectedDayIndex);
        if (year == this.mMinYear && newMonth == this.mMinMonth) {
            this.mDayAdapter.setMinDay(this.mMinDay);
        } else {
            this.mDayAdapter.setMinDay(1);
        }
        this.mValidationCalendar.set(year, month, 1);
        if (year == this.mMaxYear && newMonth == this.mMaxMonth) {
            this.mDayAdapter.setMaxDay(Math.min(this.mMaxDay, getNumDaysInCurrentMonth()));
        } else {
            this.mDayAdapter.setMaxDay(getNumDaysInCurrentMonth());
        }
        moveDayTo(day);
    }

    private void moveDayTo(int day) {
        int newDay = this.mDayAdapter.clamp(day);
        if (newDay != this.mDayAdapter.getDay(this.mSelectedDayIndex)) {
            this.mSelectedDayIndex = this.mDayAdapter.getDayIndex(newDay);
            this.mDayPicker.scrollToPosition(this.mSelectedDayIndex);
        }
    }

    public interface OnDateSetListener {
        void onDateSet(DatePicker view, int year, int month, int dayOfMonth);
    }

    private static final class DayAdapter extends RecyclerView.Adapter {
        private static final String[] DAY_STRINGS = new String[31];

        private final LayoutInflater mInflater;
        private int mMaxDay = 31;
        private int mMinDay = 1;
        private int mNumDays = ((this.mMaxDay - this.mMinDay) + 1);

        private DayAdapter(Context context) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.set(1970, 0, 1, 0, 0, 0);
            for (int i = 0; i < DAY_STRINGS.length; i++) {
                calendar.set(Calendar.DAY_OF_MONTH, i + 1);
                DAY_STRINGS[i] = formatter.format(calendar.getTime());
            }

            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((TextView) holder.itemView).setText(DAY_STRINGS[getDay(position) - 1]);
        }

        @Override
        public int getItemCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DateTimeViewHolder(this.mInflater.inflate(R.layout.picker_item, parent, false));
        }

        private void updateMinMaxDays() {
            this.mNumDays = (this.mMaxDay - this.mMinDay) + 1;
            notifyDataSetChanged();
        }

        private void updateMinMaxDays(int minDay, int maxDay) {
            this.mMinDay = minDay;
            this.mMaxDay = maxDay;
            updateMinMaxDays();
        }

        private void setMinDay(int minDay) {
            updateMinMaxDays(minDay, this.mMaxDay);
        }

        private void setMaxDay(int maxDay) {
            updateMinMaxDays(this.mMinDay, maxDay);
        }

        private int clamp(int day) {
            return Math.min(Math.max(day, this.mMinDay), this.mMaxDay);
        }

        private int getDay(int index) {
            return (index % this.mNumDays) + this.mMinDay;
        }

        private int getDayIndex(int day) {
            int middle = getItemCount() / 2;
            return ((middle - (middle % this.mNumDays)) + day) - this.mMinDay;
        }
    }

    private static final class MonthAdapter extends RecyclerView.Adapter {
        private final String[] MONTHS;

        private final LayoutInflater mInflater;
        private int mMaxMonth;
        private int mMinMonth;
        private int mNumMonths;

        private MonthAdapter(Context context) {
            MONTHS = new DateFormatSymbols(Locale.getDefault()).getShortMonths();
            for (int i = 0; i < MONTHS.length; i++) {
                MONTHS[i] = MONTHS[i].toUpperCase(Locale.getDefault());
            }
            mMaxMonth = (MONTHS.length - 1);
            mMinMonth = 0;
            mNumMonths = ((this.mMaxMonth - this.mMinMonth) + 1);
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((TextView) holder.itemView).setText(MONTHS[getMonth(position)]);
        }

        @Override
        public int getItemCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MonthViewHolder(this.mInflater.inflate(R.layout.picker_item_small, parent, false));
        }

        private void setMinMonth(int minMonth) {
            updateMinMaxMonths(minMonth, this.mMaxMonth);
        }

        private void setMaxMonth(int maxMonth) {
            updateMinMaxMonths(this.mMinMonth, maxMonth);
        }

        private void updateMinMaxMonths() {
            this.mNumMonths = (this.mMaxMonth - this.mMinMonth) + 1;
            notifyDataSetChanged();
        }

        private void updateMinMaxMonths(int minMonth, int maxMonth) {
            this.mMinMonth = minMonth;
            this.mMaxMonth = maxMonth;
            updateMinMaxMonths();
        }

        private int clamp(int month) {
            return Math.min(Math.max(month, this.mMinMonth), this.mMaxMonth);
        }

        private int getMonth(int index) {
            return (index % this.mNumMonths) + this.mMinMonth;
        }

        private int getMonthIndex(int month) {
            int middle = getItemCount() / 2;
            return ((middle - (middle % this.mNumMonths)) + month) - this.mMinMonth;
        }
    }

    private static class MonthViewHolder extends DateTimeViewHolder {
        private MonthViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected float getOffCenterScale() {
            return 0.85f;
        }
    }

    private static final class YearAdapter extends RecyclerView.Adapter {
        private final SimpleDateFormat YEAR_FORMATTER;
        private final Calendar mFormatCalendar = Calendar.getInstance();
        private final LayoutInflater mInflater;
        private int mMaxYear;
        private int mMinYear;
        private int mNumYears;

        private YearAdapter(Context context) {
            YEAR_FORMATTER = new SimpleDateFormat("yyyy", Locale.getDefault());
            this.mInflater = LayoutInflater.from(context);
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            setYearRange(currentYear - 100, currentYear);
            this.mFormatCalendar.set(1970, 0, 1, 0, 0, 0);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            TextView view = (TextView) holder.itemView;
            this.mFormatCalendar.set(Calendar.YEAR, getYear(position));
            view.setText(YEAR_FORMATTER.format(this.mFormatCalendar.getTime()));
        }

        @Override
        public int getItemCount() {
            return this.mNumYears;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DateTimeViewHolder(this.mInflater.inflate(R.layout.picker_item, parent, false));
        }

        private void setMinYear(int year) {
            setYearRange(year, this.mMaxYear);
        }

        private void setMaxYear(int year) {
            setYearRange(this.mMinYear, year);
        }

        private void setYearRange(int minYear, int maxYear) {
            this.mMinYear = minYear;
            this.mMaxYear = maxYear;
            this.mNumYears = (maxYear - minYear) + 1;
            notifyDataSetChanged();
        }

        private int clamp(int year) {
            return DatePicker.clamp(year, this.mMinYear, this.mMaxYear);
        }

        private int getYear(int index) {
            return this.mMinYear + index;
        }

        private int getYearIndex(int year) {
            return year - this.mMinYear;
        }
    }

    private class AdvanceFromDayListener implements WearableListView.ClickListener {

        @SuppressWarnings("deprecation")
        @Override
        public void onClick(WearableListView.ViewHolder view) {
            if (view.getAdapterPosition() == DatePicker.this.mSelectedDayIndex) {
                DatePicker.this.setCenterIndex(DatePicker.this.mDayPickerIndex + 1, true);
            }
        }

        @Override
        public void onTopEmptyRegionClick() {
        }
    }

    private class AdvanceFromMonthListener implements WearableListView.ClickListener {

        @Override
        public void onClick(WearableListView.ViewHolder view) {
            if (view.getAdapterPosition() == DatePicker.this.mSelectedMonthIndex) {
                DatePicker.this.setCenterIndex(DatePicker.this.mMonthPickerIndex + 1, true);
            }
        }

        @Override
        public void onTopEmptyRegionClick() {
        }
    }

    private class AdvanceFromYearListener implements WearableListView.ClickListener {

        @Override
        public void onClick(WearableListView.ViewHolder view) {
            if (view.getAdapterPosition() == DatePicker.this.mSelectedYearIndex) {
                DatePicker.this.setCenterIndex(DatePicker.this.mYearPickerIndex + 1, true);
            }
        }

        @Override
        public void onTopEmptyRegionClick() {
        }
    }

    private class DayScrollListener implements WearableListView.OnCentralPositionChangedListener {

        @Override
        public void onCentralPositionChanged(int centralPosition) {
            DatePicker.this.mSelectedDayIndex = centralPosition;
            DatePicker.this.moveDayTo(DatePicker.this.mDayAdapter.getDay(DatePicker.this.mSelectedDayIndex));
        }
    }

    private class MonthScrollListener implements WearableListView.OnCentralPositionChangedListener {

        @Override
        public void onCentralPositionChanged(int centralPosition) {
            DatePicker.this.mSelectedMonthIndex = centralPosition;
            DatePicker.this.moveMonthTo(DatePicker.this.mMonthAdapter.getMonth(DatePicker.this.mSelectedMonthIndex));
        }
    }

    private class YearScrollListener implements WearableListView.OnCentralPositionChangedListener {

        @Override
        public void onCentralPositionChanged(int centralPosition) {
            DatePicker.this.mSelectedYearIndex = centralPosition;
            DatePicker.this.moveYearTo(DatePicker.this.mYearAdapter.getYear(DatePicker.this.mSelectedYearIndex));
        }
    }

    private class DoneCheckListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            DatePicker.this.mDoneView.setClickable(false);
            int month = DatePicker.this.mMonthAdapter.getMonth(DatePicker.this.mSelectedMonthIndex);
            int day = DatePicker.this.mDayAdapter.getDay(DatePicker.this.mSelectedDayIndex);
            int year = DatePicker.this.mYearAdapter.getYear(DatePicker.this.mSelectedYearIndex);
            if (DatePicker.this.mListener != null) {
                DatePicker.this.mListener.onDateSet(DatePicker.this, year, month, day);
            }
        }
    }
}
