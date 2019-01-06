package com.example.luoyangfan.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tegetherDayView = (TextView)findViewById(R.id.our_together_day);
        tegetherDayView.setText(""+daysFromOurTogether());

    }

    private int daysFromOurTogether(){
        Calendar togetherDay = Calendar.getInstance();
        togetherDay.set(2017,6,1);
        Calendar today = Calendar.getInstance();
        long time = today.getTimeInMillis()-togetherDay.getTimeInMillis();
        long oneDay = 24*60*60*1000;
        return  (int)(time/oneDay)+1;

    }


}
