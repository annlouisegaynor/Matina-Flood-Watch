package com.example.chynnasevilleno.mfw;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FragmentWeather extends Fragment {
    View view;

    private TextView t1_city, t2_temp, t3_humidity, t4_weather_desc, t5_date;
    DatabaseReference weatherDB;


    public FragmentWeather() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //Initialize temperature values
        t1_city = (TextView) view.findViewById(R.id.city);
        t2_temp = (TextView) view.findViewById(R.id.temp);
        t3_humidity = (TextView) view.findViewById(R.id.humidity);
        t4_weather_desc = (TextView) view.findViewById(R.id.weather_desc);
        t5_date = (TextView) view.findViewById(R.id.date);
        weatherDB = FirebaseDatabase.getInstance().getReference("weather");

        view=inflater.inflate(R.layout.weather_fragment,container,false);
        return view;
    }


}
