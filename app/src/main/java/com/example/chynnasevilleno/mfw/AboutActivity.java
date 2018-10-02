package com.example.chynnasevilleno.mfw;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class AboutActivity extends NavigationDrawerActivity {

    protected LinearLayout contentFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout of this activity into parent activty
        contentFrame = (LinearLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_about, contentFrame);

        // Set checked item of navigation drawer to current activity
        navigationView.setCheckedItem(R.id.nav_about);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set checked item of navigation drawer to resumed activity
        navigationView.setCheckedItem(R.id.nav_about);
    }
}
