package com.example.chynnasevilleno.mfw;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

//Fragments, TabLayout, ViewPager
//Firebase imports
//Mapbox imports
//Volley HTTP imports



public class MainActivity extends AppCompatActivity implements OnItemSelectedListener, OnMapReadyCallback{

    private MapboxMap mapboxMap;
    private MapView mapView;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = (TabLayout)findViewById(R.id.tablayout_id);
        viewPager = (ViewPager)findViewById(R.id.viewpager_id);

        tabLayout.setupWithViewPager(viewPager);

        //Creates the fragment (tab)
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentFlood(),"Flood");
        adapter.addFragment(new FragmentWeather(), "Weather");
        adapter.addFragment(new FragmentAnalytics(),"Analytics");

        tabLayout.addTab(tabLayout.newTab().setText("Flood"));
        tabLayout.addTab(tabLayout.newTab().setText("Weather"));
        tabLayout.addTab(tabLayout.newTab().setText("Analytics"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //pass view to view pager
        viewPager.setAdapter(adapter);


        //Initialize Mapbox map
        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //Initialize Matina spinner(dropdown)
        Spinner matina_spinner = findViewById(R.id.matina_dropdown);
        ArrayAdapter<CharSequence> matina_adapter = ArrayAdapter.createFromResource(this,
                R.array.matina_areas, android.R.layout.simple_spinner_item);
        matina_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        matina_spinner.setAdapter(matina_adapter);
        matina_spinner.setOnItemSelectedListener(this);
        matina_spinner.bringToFront();

    }



    public void onItemSelected(AdapterView<?> parent, View view,int pos, long id) {
        //Assign an action to each item selected in the matina spinner
        switch (pos){
            case 0: //Select Location
                //Do nothing
                break;
            case 1: //Matina Aplaya
                updateCamera(7.042983, 125.574454);
                break;
            case 2://Matina Crossing
                updateCamera(7.056755, 125.578778);
                break;
            case 3://Matina Pangi
                updateCamera(7.077592, 125.568452);
                break;
        }
    }


    public void updateCamera(double lat, double lang){
        //Update location of map given a set of latitude and longitude values
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(lat, lang)) // Sets the new camera position
                .zoom(15) // Sets the zoom
                .bearing(90) // Rotate the camera
                .build(); // Creates a CameraPosition from the builder

        mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 3500);
    }


    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        //Get map instance once ready
        MainActivity.this.mapboxMap = mapboxMap;
    }

    //Add the mapView's own lifecycle methods to the activity's lifecycle methods
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }


    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
