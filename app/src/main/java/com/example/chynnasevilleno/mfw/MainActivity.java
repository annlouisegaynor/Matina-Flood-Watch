package com.example.chynnasevilleno.mfw;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Calendar;

//Fragments, TabLayout, ViewPager
//Firebase imports
//Mapbox imports
//Volley HTTP imports



public class MainActivity extends AppCompatActivity implements OnItemSelectedListener, OnMapReadyCallback{

    TextView cityTxt, tempTxt, humidityTxt, wind_speedTxt, dateTxt, sea_levelTxt, observation_dateTxt, matina_locTxt;
    DatabaseReference weatherDB;
    private MapboxMap mapboxMap;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        //initialize TextView(s) in Slide-up panel
        cityTxt = findViewById(R.id.city);
        tempTxt = findViewById(R.id.temp);
        humidityTxt = findViewById(R.id.humidity);
        wind_speedTxt = findViewById(R.id.wind_speed);
        sea_levelTxt = findViewById(R.id.sea_level);
        dateTxt = findViewById(R.id.date);
        observation_dateTxt = findViewById(R.id.observation_date);
        weatherDB = FirebaseDatabase.getInstance().getReference("weather");

        //Calls find_weather() to retrieve API data from Wunderground
        find_weather();
    }

    public void find_weather(){
        //This function retrieves API values from Wunderground for displaying
        String API_URL_CONDITIONS = "http://api.wunderground.com/api/e86f48eaac49650b/conditions/q/philippines/davao.json";

        JsonObjectRequest jor_conditions = new JsonObjectRequest(Request.Method.GET, API_URL_CONDITIONS, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String city, temp, hum, wind_speed, sea_level, current_date, observation_date;
                try {
                    //Get objects from JSON API
                    JSONObject weather_obj = response.getJSONObject("current_observation");
                    JSONObject location_obj = weather_obj.getJSONObject("display_location");
                    //Get specific values from JSON API
                    city = location_obj.getString("city")+" City";
                    temp = String.valueOf(weather_obj.getDouble("temp_c"));
                    hum = weather_obj.getString("relative_humidity");
                    wind_speed = weather_obj.getString("wind_kph");
                    sea_level = weather_obj.getString("pressure_mb");
                    observation_date = weather_obj.getString("observation_time");
                    //displays date using Java's .getDateInstance()
                    Calendar calendar = Calendar.getInstance();
                    current_date = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());

                    cityTxt.setText(city);
                    tempTxt.setText(temp);
                    humidityTxt.setText(hum);
                    wind_speedTxt.setText(wind_speed);
                    sea_levelTxt.setText(sea_level);
                    dateTxt.setText(current_date);
                    observation_dateTxt.setText(observation_date);

                    saveValues(city, temp, hum, wind_speed, sea_level,current_date);

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                error.printStackTrace();
            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jor_conditions);

    }


    public void saveValues(String city, String temp, String hum, String wind_speed,  String sea_level, String date){
        //Saves database objects to Firebase for later data training
        String id = weatherDB.push().getKey();

        Weather weather = new Weather(id, city, temp, hum, wind_speed, sea_level, date);

        weatherDB.child(id).setValue(weather);

        Toast.makeText(this, "Weather data saved", Toast.LENGTH_SHORT).show();
    }


    public void onItemSelected(AdapterView<?> parent, View view,int pos, long id) {
        //Assign an action to each item selected in the matina spinner
        //Changes values of location_id text in activity_main when clicked
        String matina_location;
        switch (pos){
            case 0: //Select Location
                //Do nothing
                break;
            case 1: //Matina Aplaya
                updateCamera(7.042983, 125.574454);
                //gets values from selected value (in String format) from dropdown and sets location_id value
                matina_location = parent.getItemAtPosition(pos).toString();
                matina_locTxt = (TextView) findViewById(R.id.location_id);
                matina_locTxt.setText(matina_location);
                break;
            case 2://Matina Crossing
                updateCamera(7.056755, 125.578778);
                matina_location = parent.getItemAtPosition(pos).toString();
                matina_locTxt = (TextView) findViewById(R.id.location_id);
                matina_locTxt.setText(matina_location);
                break;
            case 3://Matina Pangi
                updateCamera(7.077592, 125.568452);
                matina_location = parent.getItemAtPosition(pos).toString();
                matina_locTxt = (TextView) findViewById(R.id.location_id);
                matina_locTxt.setText(matina_location);
                break;
        }
    }


    public void updateCamera(double lat, double lang){
        //Update location of map given a set of latitude and longitude values
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(lat, lang)) // Sets the new camera position
                .zoom(16) // Sets the zoom
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
