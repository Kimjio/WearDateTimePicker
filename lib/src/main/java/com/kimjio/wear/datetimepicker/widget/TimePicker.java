package com.kimjio.wear.datetimepicker.widget;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.math.MathUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.kimjio.wear.datetimepicker.R;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimePicker extends TeaserLayout {
    private View mDoneView;
    private OnTimeSetListener mListener;
    private int mSelectedAmPmIndex;
    private int mSelectedHourIndex;
    private int mSelectedMinuteIndex;
    private boolean m24HourClock;
    private WearableListView mAmPmPicker;
    private HourAdapter mHourAdapter;
    private WearableListView mHourPicker;
    private MinuteAdapter mMinuteAdapter;
    private ViewGroup mMinuteContainer;
    private WearableListView mMinutePicker;
    private TextView mMinuteSymbolSpacer;

    public TimePicker(Context context) {
        this(context, null, 0, 0);
    }

    public TimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflate(getContext(), R.layout.time_picker, this);
        init();
    }

    public void setOnTimeChangedListener(OnTimeSetListener onTimeSetListener) {
        this.mListener = onTimeSetListener;
    }

    private void init() {
        this.mHourPicker = findViewById(R.id.hour_picker);
        this.mHourAdapter = new HourAdapter(getContext());
        this.mHourPicker.setAdapter(this.mHourAdapter);
        this.mHourPicker.setGreedyTouchMode(true);
        this.mHourPicker.addOnCentralPositionChangedListener(new HourScrollListener());
        this.mHourPicker.setClickListener(new AdvanceFromHourListener());
        this.mMinuteContainer = findViewById(R.id.minute_container);
        this.mMinutePicker = findViewById(R.id.minute_picker);
        this.mMinuteAdapter = new MinuteAdapter(getContext());
        this.mMinutePicker.setAdapter(this.mMinuteAdapter);
        this.mMinutePicker.setGreedyTouchMode(true);
        this.mMinutePicker.addOnCentralPositionChangedListener(new MinuteScrollListener());
        this.mMinutePicker.setClickListener(new AdvanceFromMinuteListener());
        this.mAmPmPicker = findViewById(R.id.second_picker);
        this.mAmPmPicker.setAdapter(new AmPmAdapter(getContext()));
        this.mAmPmPicker.setGreedyTouchMode(true);
        this.mAmPmPicker.addOnCentralPositionChangedListener(new AmPmScrollListener());
        this.mAmPmPicker.setClickListener(new AdvanceFromAmPmListener());
        setIs24HourView(DateFormat.is24HourFormat(getContext()));
        this.mDoneView = findViewById(R.id.check);
        this.mDoneView.setOnClickListener(new DoneCheckListener());
        this.mMinuteSymbolSpacer = findViewById(R.id.minute_symbol_spacer);
        Calendar c = Calendar.getInstance();
        setTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
        setCenteredField(0, false);
    }

    public void setTime(int hours, int minutes) {
        if (this.m24HourClock) {
            this.mSelectedHourIndex = this.mHourAdapter.getHourIndex(hours);
            this.mSelectedMinuteIndex = this.mMinuteAdapter.getMinuteIndex(minutes);
            this.mHourPicker.scrollToPosition(this.mSelectedHourIndex);
            this.mMinutePicker.scrollToPosition(this.mSelectedMinuteIndex);
            return;
        }
        this.mSelectedHourIndex = this.mHourAdapter.getHourIndex(hours - 12);
        this.mSelectedMinuteIndex = this.mMinuteAdapter.getMinuteIndex(minutes);
        this.mSelectedAmPmIndex = hours < 12 ? 0 : 1;
        this.mHourPicker.scrollToPosition(this.mSelectedHourIndex);
        this.mMinutePicker.scrollToPosition(this.mSelectedMinuteIndex);
        this.mAmPmPicker.scrollToPosition(this.mSelectedAmPmIndex);
    }

    public void setCenteredField(int timeField, boolean animate) {
        setCenterIndex(timeField, animate);
    }

    public int getHour() {
        int hour = this.mHourAdapter.getHour(this.mSelectedHourIndex);
        if (this.m24HourClock)
            return hour;
        return hour + (this.mSelectedAmPmIndex != 0 ? 12 : 0);
    }

    public void setHour(@IntRange(from = 0, to = 23) int hour) {
        hour = MathUtils.clamp(hour, 0, 23);
        if (this.m24HourClock) {
            this.mSelectedHourIndex = this.mHourAdapter.getHourIndex(hour);
            this.mHourPicker.scrollToPosition(this.mSelectedHourIndex);
            return;
        }
        this.mSelectedHourIndex = this.mHourAdapter.getHourIndex(hour - 12);
        this.mSelectedAmPmIndex = hour < 12 ? 0 : 1;
        this.mHourPicker.scrollToPosition(this.mSelectedHourIndex);
        this.mAmPmPicker.scrollToPosition(this.mSelectedAmPmIndex);
    }

    public int getMinute() {
        return this.mMinuteAdapter.getMinute(this.mSelectedMinuteIndex);
    }

    public void setMinute(@IntRange(from = 0, to = 59) int minute) {
        this.mSelectedMinuteIndex = this.mMinuteAdapter.getMinuteIndex(MathUtils.clamp(minute, 0, 59));
        this.mMinutePicker.scrollToPosition(this.mSelectedMinuteIndex);
        this.mMinutePicker.scrollToPosition(this.mSelectedMinuteIndex);
    }

    public void setIs24HourView(boolean is24HourView) {
        if (this.m24HourClock != is24HourView) {
            int hour = getHour();
            int minute = getMinute();
            this.m24HourClock = is24HourView;
            if (this.m24HourClock) {
                removeView(this.mAmPmPicker);
            } else {
                addView(this.mAmPmPicker, indexOfChild(this.mMinuteContainer) + 1);
            }
            this.mHourAdapter.set24Hour(this.m24HourClock);
            setTime(hour, minute);
        }
    }

    public boolean is24HourView() {
        return m24HourClock;
    }

    public void setMinuteSymbol(CharSequence text) {
        mMinuteSymbolSpacer.setText(text);
    }

    public void setMinuteSymbol(@StringRes int resId) {
        mMinuteSymbolSpacer.setText(resId);
    }

    public interface OnTimeSetListener {
        void onTimeSet(TimePicker view, int hourOfDay, int minute);
    }

    private static final class AmPmAdapter extends RecyclerView.Adapter {
        private final String[] AM_PMS;

        private final LayoutInflater mInflater;

        private AmPmAdapter(Context context) {
            AM_PMS = new DateFormatSymbols(Locale.getDefault()).getAmPmStrings();
            Locale locale = Locale.getDefault();
            for (int i = 0; i < AM_PMS.length; i++) {
                AM_PMS[i] = AM_PMS[i].toLowerCase(locale);
            }
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((TextView) holder.itemView).setText(AM_PMS[position]);
        }

        @Override
        public int getItemCount() {
            return AM_PMS.length;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AmPmViewHolder(this.mInflater.inflate(R.layout.picker_item_small, parent, false));
        }

    }

    private static class AmPmViewHolder extends DateTimeViewHolder {

        private AmPmViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected float getOffCenterScale() {
            return 0.85f;
        }
    }

    private static final class HourAdapter extends RecyclerView.Adapter {
        private static final String[] HOUR_STRINGS = new String[24];

        static {
            SimpleDateFormat formatter = new SimpleDateFormat("HH", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.set(1970, 0, 1, 0, 0, 0);
            for (int i = 0; i < HOUR_STRINGS.length; i++) {
                calendar.set(Calendar.HOUR_OF_DAY, i);
                HOUR_STRINGS[i] = formatter.format(calendar.getTime());
            }
        }

        private final LayoutInflater mInflater;
        private int hourMod;
        private int hourOffset;
        private boolean mIs24Hour;

        private HourAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
            this.mIs24Hour = DateFormat.is24HourFormat(context);
            if (this.mIs24Hour) {
                this.hourMod = 24;
                this.hourOffset = 0;
                return;
            }
            this.hourMod = 12;
            this.hourOffset = 1;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((TextView) holder.itemView).setText(getHourString(position));
        }

        @Override
        public int getItemCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DateTimeViewHolder(this.mInflater.inflate(R.layout.picker_item, parent, false));
        }

        void set24Hour(boolean is24Hour) {
            this.mIs24Hour = is24Hour;
            if (this.mIs24Hour) {
                this.hourMod = 24;
                this.hourOffset = 0;
                return;
            }
            this.hourMod = 12;
            this.hourOffset = 1;
        }

        private String getHourString(int position) {
            int index = getHour(position);
            if (!this.mIs24Hour && index == 0) {
                index += 12;
            }
            return HOUR_STRINGS[index];
        }

        private int getHour(int index) {
            return ((index % this.hourMod) + this.hourOffset) % this.hourMod;
        }

        private int getHourIndex(int hour) {
            int middle = getItemCount() / 2;
            return (middle - ((middle % this.hourMod) + this.hourOffset)) + hour;
        }
    }

    private static final class MinuteAdapter extends RecyclerView.Adapter {
        private static final String[] MINUTE_STRINGS = new String[60];

        static {
            SimpleDateFormat formatter = new SimpleDateFormat("mm", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.set(1970, 0, 1, 0, 0, 0);
            for (int i = 0; i < MINUTE_STRINGS.length; i++) {
                calendar.set(Calendar.MINUTE, i);
                MINUTE_STRINGS[i] = formatter.format(calendar.getTime());
            }
        }

        private final LayoutInflater mInflater;

        private MinuteAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((TextView) holder.itemView).setText(MINUTE_STRINGS[getMinute(position)]);
        }

        @Override
        public int getItemCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DateTimeViewHolder(this.mInflater.inflate(R.layout.picker_item, parent, false));
        }

        private int getMinute(int index) {
            return index % MINUTE_STRINGS.length;
        }

        private int getMinuteIndex(int minute) {
            int middle = getItemCount() / 2;
            return (minute % MINUTE_STRINGS.length) + (middle - (middle % MINUTE_STRINGS.length));
        }
    }

    private class AdvanceFromAmPmListener implements WearableListView.ClickListener {

        @Override
        public void onClick(WearableListView.ViewHolder view) {
            if (view.getAdapterPosition() == TimePicker.this.mSelectedAmPmIndex) {
                TimePicker.this.setCenterIndex(3, true);
            }
        }

        @Override
        public void onTopEmptyRegionClick() {
        }
    }

    private class AdvanceFromHourListener implements WearableListView.ClickListener {

        @Override
        public void onClick(WearableListView.ViewHolder view) {
            if (view.getAdapterPosition() == TimePicker.this.mSelectedHourIndex) {
                TimePicker.this.setCenterIndex(1, true);
            }
        }

        @Override
        public void onTopEmptyRegionClick() {
        }
    }

    private class AdvanceFromMinuteListener implements WearableListView.ClickListener {

        @Override
        public void onClick(WearableListView.ViewHolder view) {
            if (view.getAdapterPosition() == TimePicker.this.mSelectedMinuteIndex) {
                TimePicker.this.setCenterIndex(2, true);
            }
        }

        @Override
        public void onTopEmptyRegionClick() {
        }
    }

    private class AmPmScrollListener implements WearableListView.OnCentralPositionChangedListener {
        private AmPmScrollListener() {
        }

        public void onCentralPositionChanged(int centralPosition) {
            TimePicker.this.mSelectedAmPmIndex = centralPosition;
        }
    }

    private class DoneCheckListener implements OnClickListener {
        private DoneCheckListener() {
        }

        public void onClick(View view) {
            TimePicker.this.mDoneView.setClickable(false);
            int hour = TimePicker.this.getHour();
            int minute = TimePicker.this.getMinute();
            if (TimePicker.this.mListener != null) {
                TimePicker.this.mListener.onTimeSet(TimePicker.this, hour, minute);
            }
        }
    }

    private class HourScrollListener implements WearableListView.OnCentralPositionChangedListener {
        private HourScrollListener() {
        }

        public void onCentralPositionChanged(int centralPosition) {
            TimePicker.this.mSelectedHourIndex = centralPosition;
        }
    }

    private class MinuteScrollListener implements WearableListView.OnCentralPositionChangedListener {
        private MinuteScrollListener() {
        }

        public void onCentralPositionChanged(int centralPosition) {
            TimePicker.this.mSelectedMinuteIndex = centralPosition;
        }
    }
}
