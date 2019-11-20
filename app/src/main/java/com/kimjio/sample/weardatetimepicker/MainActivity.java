package com.kimjio.sample.weardatetimepicker;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.TextView;

import com.kimjio.wear.datetimepicker.app.DatePickerDialog;
import com.kimjio.wear.datetimepicker.app.TimePickerDialog;
import com.kimjio.wear.datetimepicker.widget.DatePicker;
import com.kimjio.wear.datetimepicker.widget.TimePicker;

import java.util.Locale;

public class MainActivity extends WearableActivity {

    private TextView resultDate, resultTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultDate = findViewById(R.id.result_date);
        resultTime = findViewById(R.id.result_time);
    }

    public void onDateClick(View view) {
        new DatePickerDialog(this)
                .setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        resultDate.setText(String.format(Locale.getDefault(), "%d/%02d/%02d", year, month + 1, dayOfMonth));
                    }
                })
                .show();
    }

    public void onTime24Click(View view) {
        new TimePickerDialog(this, true)
                .setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        resultTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                    }
                })
                .show();
    }

    public void onTime12Click(View view) {
        new TimePickerDialog(this, false)
                .setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        resultTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                    }
                })
                .show();
    }
}
