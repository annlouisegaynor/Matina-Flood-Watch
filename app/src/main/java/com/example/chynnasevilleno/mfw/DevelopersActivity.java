package com.example.chynnasevilleno.mfw;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class DevelopersActivity extends NavigationDrawerActivity {
    private LinearLayout contentFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout of this activity into parent activity
        contentFrame = (LinearLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_developers, contentFrame);

        // Set checked item of navigation drawer to current activity
        navigationView.setCheckedItem(R.id.nav_developers);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set checked item of navigation drawer to resumed activity
        navigationView.setCheckedItem(R.id.nav_developers);
    }
}
