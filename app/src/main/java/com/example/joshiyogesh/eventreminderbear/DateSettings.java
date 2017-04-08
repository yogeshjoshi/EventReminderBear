package com.example.joshiyogesh.eventreminderbear;

import android.app.DatePickerDialog;
import android.widget.DatePicker;

/**
 * Created by Joshi Yogesh on 08/04/2017.
 */
/*
* This Class is used for Selecting Date from DatePicker Dialog
* Listener is used to indicate that user has finished selection of Date
* */
public class DateSettings implements DatePickerDialog.OnDateSetListener {
    int year , month , dayOfMonth;
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
    }
}
