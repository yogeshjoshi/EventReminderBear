package com.example.joshiyogesh.eventreminderbear;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;

/**
 * Created by Joshi Yogesh on 08/04/2017.
 */
/*
* This class is used for showing Calendar as a Fragment
* */
public class PickerDialog extends DialogFragment {
    DateSettings dateSettings;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dateSettings = new DateSettings();
        Calendar calendar = Calendar.getInstance(); //making Instance of Calendar
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),dateSettings,year,month,day);//invoking DatePicker Dialog and passing instance of DateSettings
        return datePickerDialog;
    }
}

