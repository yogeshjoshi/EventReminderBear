package com.example.joshiyogesh.eventreminderbear;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /*
    * this methods helps to call PickerDialog (Choosing Date)
    * */
    public void setDate(View view) {
        PickerDialog pickerDialogs = new PickerDialog();
        pickerDialogs.show(getSupportFragmentManager(),"Date_Picker");
    }
}
