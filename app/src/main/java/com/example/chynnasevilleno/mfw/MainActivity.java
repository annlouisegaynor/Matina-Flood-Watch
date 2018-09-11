package com.example.chynnasevilleno.mfw;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
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
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.InfoWindow;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;


public class MainActivity extends NavigationDrawerActivity implements OnItemSelectedListener, OnMapReadyCallback{
    //Text View
    TextView floodTxt, cityTxt, tempTxt, humidityTxt, wind_speedTxt, dateTxt, sea_levelTxt,
            observation_dateTxt, matina_locTxt, rainfallTxt;

    //Flood route
    private DirectionsRoute currentRoute;
    private Polyline polyline;

    //Matina spinner
    Spinner matina_spinner;
    ArrayAdapter<CharSequence> matina_adapter;
    private boolean userIsInteracting = false;

    //Matina markers
    Marker aplayaMarker, crossingMarker, pangiMarker;

    //Mapbox
    private MapboxMap mapboxMap;
    private MapView mapView;

    //Firebase
    DatabaseReference weatherDB;
    Weather weather;

    //Flood Predicting
    Boolean floodInAplaya, floodInCrossing, floodInPangi;
    double aplayaFloodlvl, crossingFloodlvl, pangiFloodlvl;
    float floodlvltemp = 0;

    // Side Navigation Drawer
    protected LinearLayout contentFrame;

    //Timer
    Timer timer = new Timer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout of this activity into parent activity
        contentFrame = (LinearLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_main, contentFrame);

        //Initialize Mapbox map
        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView. getMapAsync(this);

        //Initialize Matina spinner(dropdown)
        matina_spinner = findViewById(R.id.matina_dropdown);
        matina_adapter = ArrayAdapter.createFromResource(this,
                R.array.matina_areas, android.R.layout.simple_spinner_item);
        matina_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        matina_spinner.setAdapter(matina_adapter);
        matina_spinner.setOnItemSelectedListener(this);
        matina_spinner.bringToFront();

        //Initialize TextView(s) in Slide-up panel
        floodTxt = findViewById(R.id.flood);
        cityTxt = findViewById(R.id.city);
        tempTxt = findViewById(R.id.temp);
        humidityTxt = findViewById(R.id.humidity);
        wind_speedTxt = findViewById(R.id.wind_speed);
        rainfallTxt = findViewById(R.id.rainfall);
        sea_levelTxt = findViewById(R.id.sea_level);
        dateTxt = findViewById(R.id.date);
        observation_dateTxt = findViewById(R.id.observation_date);
        weatherDB = FirebaseDatabase.getInstance().getReference("weather");

        // Initialize Flooding in Matina Areas to false
        floodInPangi= false;
        floodInCrossing= false;
        floodInAplaya = false;

        // Set checked item of navigation drawer to current activity
        navigationView.setCheckedItem(R.id.nav_home);

        // Check for an internet connection
        new InternetCheck(internet -> {
            if(true){

            }else{
                Toast.makeText(this, "No Internet Connection. Values set to default.", Toast.LENGTH_LONG).show();
            }
        });

        // Accesses the cache if there's no internet
        SharedPreferences sp = getSharedPreferences("key", 0);
        String cityCache = sp.getString("city","");
        String floodCache = sp.getString("flood","");
        String rainfallCache = sp.getString("rainfall","");
        String temperatureCache = sp.getString("temperature","");
        String humidityCache = sp.getString("humidity","");
        String windspeedCache = sp.getString("windspeed","");
        String sealevelCache = sp.getString("sealevel","");
        String currentCache = sp.getString("currentdate","");
        String observationCache = sp.getString("observationdate","");

        displayCache(cityCache, rainfallCache, temperatureCache, humidityCache, windspeedCache, sealevelCache,
                currentCache, observationCache);

    }


    // FIND AND SET-UP WEATHER
    TimerTask hourlyTask = new TimerTask () {
        @Override
        public void run () {
            //This function calls the below function every hour

            //Calls find_weather() to retrieve API data from Wunderground
            find_weather();
        }
    };


    public void find_weather(){
        //This function retrieves API values from Wunderground & OpenWeatherMap to display
        //String city, temp, hum, wind_speed, sea_level, current_date, observation_date;
        String API_URL_WUNDERGROUND = "http://api.wunderground.com/api/e86f48eaac49650b/conditions/q/philippines/davao.json";

        // JsonObjectRequest for Wunderground
        JsonObjectRequest jor_wunderground = new JsonObjectRequest(Request.Method.GET, API_URL_WUNDERGROUND, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String city, hum, sea_level, rainfall, wind_speed, spress, observation_date, current_date;

                try {
                    //Get objects from JSON API
                    JSONObject weather_obj = response.getJSONObject("current_observation");
                    JSONObject location_obj = weather_obj.getJSONObject("display_location");

                    //Get specific values from JSON API
                    city = location_obj.getString("city")+" City";
                    hum = weather_obj.getString("relative_humidity");
                    sea_level = weather_obj.getString("pressure_mb");
                    rainfall = weather_obj.getString("precip_today_metric");
                    observation_date = weather_obj.getString("observation_time");
                    wind_speed = weather_obj.getString("wind_kph");
                    spress = weather_obj.getString("pressure_mb");

                    //displays date using Java's .getDateInstance()
                    Calendar calendar = Calendar.getInstance();
                    current_date = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());

                    // hum value has "%" and i need to delete that when i save in the database
                    hum = hum.replace("%", "");

                    // you cannot execute two asynchronous calls at the same time
                    // so i created a function that could call the next API request
                    // and pass the values of the first API request to the next request

                    floodInAplaya = false;
                    floodInCrossing = false;
                    floodInPangi = false;
                    
                    getOWM(city, hum, sea_level, rainfall, wind_speed, spress, observation_date, current_date);


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
        queue.add(jor_wunderground);

    }


    public void getOWM(String city, String hum, String sea_level, String rainfall, String wind_speed, String spress, String observation_date, String current_date ){
        // JsonObjectRequest for OpenWeatherMap
        String API_URL_OWM = "http://api.openweathermap.org/data/2.5/weather?q=davao,ph&id=524901&units=metric&APPID=bd5e378503939ddaee76f12ad7a97608";
        JsonObjectRequest jor_owm = new JsonObjectRequest(Request.Method.GET, API_URL_OWM, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String mint, maxt, meant, floodlvl;
                float meantemp;

                try {
                    //Get objects from JSON API
                    JSONObject main_obj = response.getJSONObject("main");

                    //Get specific values from JSON API
                    mint = main_obj.getString("temp_min");
                    maxt = main_obj.getString("temp_max");

                    //meantemp is the temporary calculation holder
                    meantemp = (Float.parseFloat(maxt) + Float.parseFloat(mint)) / 2;
                    //converting meantemp to String
                    meant = String.valueOf(meantemp);

                    //converting floodlvl to string
                    floodlvl = Float.toString(floodlvltemp);

                    getFloodedAreas(hum, sea_level, rainfall, wind_speed, spress, mint, maxt, meant);
                    displayWeatherValues(city, rainfall, meant, hum, wind_speed, sea_level, current_date, observation_date);
                    saveValues(rainfall, hum, meant, maxt, mint, spress, wind_speed, floodlvl);
                    //saveValues(rainfall, hum, meant, maxt, mint, spress, wind_speed, aplayaFloodlvl, crossingFloodlvl, pangiFloodlvl);

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
        queue.add(jor_owm);
    }


    public void displayCache(String city, String rainfall, String temp, String hum, String wind_speed, String sea_level,
                         String current_date, String observation_date){

        // Cache display method (displays the ones we saved)
        cityTxt.setText(city);
        tempTxt.setText(temp);
        rainfallTxt.setText(rainfall);
        humidityTxt.setText(hum);
        wind_speedTxt.setText(wind_speed);
        sea_levelTxt.setText(sea_level);
        dateTxt.setText(current_date);
        observation_dateTxt.setText(observation_date);

        int selected_location = matina_spinner.getSelectedItemPosition();
        switch(selected_location){
            case 0:
                floodTxt.setText(aplayaFloodlvl +" meters");
                break;
            case 1:
                floodTxt.setText(crossingFloodlvl +" meters");
                break;
            case 2:
                floodTxt.setText(pangiFloodlvl +" meters");
                break;
        }
    }


    public void displayWeatherValues(String city, String rainfall, String temp, String hum, String wind_speed, String sea_level,
                                     String current_date, String observation_date){
        cityTxt.setText(city);
        tempTxt.setText((temp) + " \u2103");
        rainfallTxt.setText((rainfall) + " mm");
        humidityTxt.setText((hum) + " %");
        wind_speedTxt.setText((wind_speed)+" m/s");
        sea_levelTxt.setText((sea_level)+" hPa");
        dateTxt.setText(current_date);
        observation_dateTxt.setText(observation_date);

        int selected_location = matina_spinner.getSelectedItemPosition();
        switch(selected_location){
            case 0:
                floodTxt.setText(aplayaFloodlvl +" meters");
                break;
            case 1:
                floodTxt.setText(crossingFloodlvl +" meters");
                break;
            case 2:
                floodTxt.setText(pangiFloodlvl +" meters");
                break;
        }

        //Setting up cache
        SharedPreferences sp = getSharedPreferences("key", 0);
        SharedPreferences.Editor sedt = sp.edit();
        // .putString(key,value) to be stored in the SharedPreferences cache
        sedt.putString("city", cityTxt.getText().toString());
        sedt.putString("flood", floodTxt.getText().toString());
        sedt.putString("rainfall", rainfallTxt.getText().toString());
        sedt.putString("temperature", tempTxt.getText().toString());
        sedt.putString("humidity", humidityTxt.getText().toString());
        sedt.putString("windspeed", wind_speedTxt.getText().toString());
        sedt.putString("sealevel", sea_levelTxt.getText().toString());
        sedt.putString("observationdate", observation_dateTxt.getText().toString());
        sedt.putString("currentdate", dateTxt.getText().toString());
        sedt.commit();
    }


    public void saveValues(String rainfall, String rh, String meant, String maxt, String mint, String spress, String windspeed,  String floodlvl){
        //Saves database objects to Firebase for later data training
        String id = weatherDB.push().getKey();

        Weather weather = new Weather(id, rainfall, rh, meant, maxt, mint, spress, windspeed, floodlvl);

        weatherDB.child(id).setValue(weather);

        //Toast.makeText(this, "Weather data saved", Toast.LENGTH_SHORT).show();
    }


    // SPINNER EVENTS
    public void onItemSelected(AdapterView<?> parent, View view,int pos, long id) {
        //Assign an action to each item selected in the matina spinner
        //Changes values of location_id text in activity_main when clicked

        String matina_location;

        if (userIsInteracting) {
            switch (pos){
                case 0: //Matina Aplaya
                    createRoute(pos);
                    updateMarker(pos);
                    updateCamera(7.041237, 125.573432);
                    //gets values from selected value (in String format) from dropdown and sets location_id value
                    matina_location = parent.getItemAtPosition(pos).toString();
                    matina_locTxt = (TextView) findViewById(R.id.location_id);
                    matina_locTxt.setText(matina_location);
                    floodTxt.setText(aplayaFloodlvl +" meters");
                    break;

                case 1://Matina Crossing
                    createRoute(pos);
                    updateMarker(pos);
                    updateCamera(7.058762, 125.568750);
                    matina_location = parent.getItemAtPosition(pos).toString();
                    matina_locTxt = (TextView) findViewById(R.id.location_id);
                    matina_locTxt.setText(matina_location);
                    floodTxt.setText(crossingFloodlvl +" meters");
                    break;

                case 2://Matina Pangi
                    createRoute(pos);
                    updateMarker(pos);
                    updateCamera(7.074707, 125.573764);
                    matina_location = parent.getItemAtPosition(pos).toString();
                    matina_locTxt = (TextView) findViewById(R.id.location_id);
                    matina_locTxt.setText(matina_location);
                    floodTxt.setText(pangiFloodlvl +" meters");
                    break;
            }
        }

    }


    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


    public void updateCamera(double lat, double lang){
        // Update location of map given a set of latitude and longitude values

        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(lat, lang)) // Sets the new camera position
                .zoom(14) // Sets the zoom
                .bearing(90) // Rotate the camera
                .build(); // Creates a CameraPosition from the builder

        mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 3500);
    }


    // FLOOD CALCULATING
    public void getFloodedAreas(String hum, String sea_level, String rainfall, String wind_speed, String spress, String mint, String maxt, String meant){
        Double rf = Double.parseDouble(rainfall);

        if (rf == 2.6)
            floodInPangi = true;

        if (rf == 2.8)
            floodInCrossing = true;

        if (rf == 22.4)
            floodInCrossing = true;


        if (floodInAplaya == true)
            aplayaFloodlvl = getFloodLevels(1, hum, sea_level, rainfall, wind_speed, spress, mint, maxt, meant);
        else
            aplayaFloodlvl = 0;

        if (floodInCrossing == true)
            crossingFloodlvl = getFloodLevels(2, hum, sea_level, rainfall, wind_speed, spress, mint, maxt, meant);
        else
            crossingFloodlvl = 0;

        if (floodInPangi == true)
            pangiFloodlvl = getFloodLevels(3, hum, sea_level, rainfall, wind_speed, spress, mint, maxt, meant);
        else
            pangiFloodlvl = 0;
    }


    public Double getFloodLevels(int matina, String hum, String sea_level, String rainfall, String wind_speed, String spress, String mint, String maxt, String meant){
        // Calculate for flood water level areas

        double floodLevel = 0.0;
        double rf = Double.parseDouble(rainfall);
        double slvlpress = Double.parseDouble(sea_level);
        double ws = Double.parseDouble(wind_speed);
        double mit = Double.parseDouble(mint);
        double met = Double.parseDouble(meant);
        double mat = Double.parseDouble(maxt);
        double rh = Double.parseDouble(hum);
        int area = matina;


        if (met < 24.15){
            floodLevel = 4.27;
        }
        else if (met >= 24.15){
            if (area >= 1){
                if (rf < 3.3){
                    floodLevel = 1.07;
                }
                else if (rf >= 3.3){
                    if (rf < 5.7){
                        floodLevel = 3;
                    }
                    else if (rf >= 5.7){
                        if (mat < 29.4){
                            floodLevel = 2;
                        }
                        else if (mat >= 29.4){
                            floodLevel = 1.07;
                        }
                    }
                }
            }
        }

        return floodLevel;
    }


    // FLOOD ROUTES
    public void createRoute(int matina){
        //Create a flood information route for the selected Matina area
        double origin_longt, origin_lat, dest_longt, dest_lat;

        //Set the origin and destination points for route of the selected Matina Area
        if (matina == 0) { // Matina Aplaya
            origin_longt = 125.577305;
            origin_lat = 7.041089;

            dest_longt = 125.570138;
            dest_lat = 7.042366;
        }
        else if (matina == 1){ // Matina Crossing
            origin_longt = 125.570846;
            origin_lat = 7.064173;

            dest_longt = 125.566168;
            dest_lat = 7.054654;
        }
        else{ // Matina Pangi
            origin_longt = 125.572509;
            origin_lat = 7.078786;

            dest_longt = 125.574676;
            dest_lat = 7.070396;
        }

        Point origin = Point.fromLngLat(origin_longt, origin_lat);
        Point destination = Point.fromLngLat(dest_longt, dest_lat);

        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, retrofit2.Response<DirectionsResponse> response) {

                        // Get route to draw
                        currentRoute = response.body().routes().get(0);

                        // Draw the route on the map
                        drawRoute(currentRoute, matina);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                    }
                });
    }


    public void drawRoute(DirectionsRoute route, int matina){
        // Visualize flood route onto map

        String floodIntensity;
        List<LatLng> points = new ArrayList<>();
        List<Point> coords = LineString.fromPolyline(route.geometry(), Constants.PRECISION_6).coordinates();

        floodIntensity = getFloodIntensity(matina);

        for (Point point : coords) {
            points.add(new LatLng(point.latitude(), point.longitude()));
        }

        if (!points.isEmpty()) {

            if (polyline != null) {
                mapboxMap.removePolyline(polyline);
            }

            //Draw polyline on map
            mapboxMap.addPolyline(new PolylineOptions()
                    .addAll(points)
                    .color(Color.parseColor(floodIntensity))
                    .width(5));
        }
    }


    public String getFloodIntensity(int matina){
        // Gets flood intensity for flood route
        // Routes can either be: no flooding, slight flooding, moderate flooding, or heavy flooding

        String intensity = "#00BE21";
        double floodlevel = 0;

        switch(matina) {
            case 0:
                floodlevel = aplayaFloodlvl;
                break;
            case 1:
                floodlevel = crossingFloodlvl;
                break;
            case 2:
                floodlevel = pangiFloodlvl;
                break;
        }

        if (floodlevel < 0.5 && floodlevel > 0)
            intensity = "#FBED00";

        if (floodlevel < 5 && floodlevel >= 0.5)
            intensity = "#FF6F00";

        if (floodlevel >= 5)
            intensity = "#C30000";

        return intensity;
    }


    // MAP MARKERS
    public void createMarkers(){
        IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
        Icon icon = iconFactory.fromResource(R.drawable.transparent_marker);

        aplayaMarker = mapboxMap.addMarker(new MarkerOptions()
                .position(new LatLng(7.041127, 125.574047))
                .title("Matina Aplaya")
                .icon(icon)
        );
        updateMarker(0);

        crossingMarker = mapboxMap.addMarker(new MarkerOptions()
                .position(new LatLng(7.05873, 125.56878))
                .title("Matina Crossing")
                .icon(icon)
        );
        updateMarker(1);

        pangiMarker = mapboxMap.addMarker(new MarkerOptions()
                .position(new LatLng(7.074572, 125.573631))
                .title("Matina Pangi")
                .icon(icon)
        );
        updateMarker(2);
    }


    public void updateMarker(int matina){
        Marker marker = null;
        double floodlevel = 0;
        String snippet = getResources().getString(R.string.info_window_1);

        switch(matina){
            case 0:
                marker = aplayaMarker;
                floodlevel = aplayaFloodlvl;
                break;
            case 1:
                marker = crossingMarker;
                floodlevel = crossingFloodlvl;
                break;
            case 2:
                marker = pangiMarker;
                floodlevel = pangiFloodlvl;
                break;
        }

        if (floodlevel < 0.5 && floodlevel > 0)
            snippet = getResources().getString(R.string.info_window_2) +" "
                      + floodlevel + getString(R.string.info_window_2SF);

        if (floodlevel < 5 && floodlevel >= 0.5)
            snippet = getResources().getString(R.string.info_window_2) +" "
                    + floodlevel + getString(R.string.info_window_2MF);

        if (floodlevel >= 5)
            snippet = getResources().getString(R.string.info_window_2) +" "
                    + floodlevel + getString(R.string.info_window_2HF);

        marker.setSnippet(snippet);
    }


    // OVERWRITTEN EVENT LISTENERS
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Adds a refresh button to the toolbar

        getMenuInflater().inflate(R.menu.refresh, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Execute certain action on either refresh or home button click

        switch(item.getItemId()){
            case R.id.refresh_btn:
                new InternetCheck(internet -> {
                    if(true){
                        find_weather();
                        Toast.makeText(this, "Weather values up to date.", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(this, "No Internet Connection. Values set to default.", Toast.LENGTH_LONG).show();
                    }
                });
                return true;

            case android.R.id.home:
                // Open the navigation drawer
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onUserInteraction() {
        // Checks whether user is interacting with the application

        super.onUserInteraction();
        userIsInteracting = true;
    }


    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        // Get map instance once ready
        MainActivity.this.mapboxMap = mapboxMap;

        // Perform function every hour
        timer.schedule (hourlyTask, 0l, 3600000);

        // Create Matina markers
        createMarkers();

        // Allow the map to have multiple open infowindows at once
        mapboxMap.setAllowConcurrentMultipleOpenInfoWindows(true);

        // Allow the map to have keep infowindows open at all times
        mapboxMap.getUiSettings().setDeselectMarkersOnTap(false);

        // Display flood routes for Matina
        createRoute(0); // Aplaya
        createRoute(1); // Crossing
        createRoute(2); // Pangi

        // Display info windows of Matina markers
        mapboxMap.selectMarker(aplayaMarker);
        mapboxMap.selectMarker(crossingMarker);
        mapboxMap.selectMarker(pangiMarker);
    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        navigationView.setCheckedItem(R.id.nav_home);
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
