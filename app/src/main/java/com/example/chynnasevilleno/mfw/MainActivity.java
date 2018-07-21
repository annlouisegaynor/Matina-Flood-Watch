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

//Volley HTTP imports
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

//Firebase imports
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//Mapbox imports
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class MainActivity extends AppCompatActivity implements OnItemSelectedListener, OnMapReadyCallback{

    TextView t1_city, t2_temp, t3_humidity, t4_weather_desc, t5_date;
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

        //Initialize temperature values
        t1_city = findViewById(R.id.city);
        t2_temp = findViewById(R.id.temp);
        t3_humidity = findViewById(R.id.humidity);
        t4_weather_desc = findViewById(R.id.weather_desc);
        t5_date = findViewById(R.id.date);
        weatherDB = FirebaseDatabase.getInstance().getReference("weather");

        find_weather();
    }


    public void find_weather(){
        //This function retrieves API values from Wunderground and
        // displays it on the app

        String url = "http://api.wunderground.com/api/e86f48eaac49650b/conditions/q/philippines/davao.json";

        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //Get objects from JSON API
                    JSONObject weather_obj = response.getJSONObject("current_observation");
                    JSONObject location_obj = weather_obj.getJSONObject("display_location");
                    //Get specific values from JSON API
                    String city = location_obj.getString("full");
                    String temp = String.valueOf(weather_obj.getDouble("temp_c"));
                    String hum = weather_obj.getString("relative_humidity");
                    String description = weather_obj.getString("weather");
                    String date = weather_obj.getString("local_time_rfc822");

                    t1_city.setText(city);
                    t2_temp.setText(temp);
                    t3_humidity.setText(hum);
                    t4_weather_desc.setText(description);
                    t5_date.setText(date);

                    saveValues(city, temp, hum, description, date);

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
        queue.add(jor);
    }


    public void saveValues(String city, String temp, String hum, String description, String date){
        //Saves database objects to Firebase

        String id = weatherDB.push().getKey();

        Weather weather = new Weather(id, city, temp, hum, description, date);

        weatherDB.child(id).setValue(weather);

        Toast.makeText(this, "Weather data saved", Toast.LENGTH_SHORT).show();
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
                .zoom(12) // Sets the zoom
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
