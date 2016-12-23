package sqrtstudio.com.fitwalker;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.volley.Request.Method.*;
import static sqrtstudio.com.fitwalker.AppConfig.*;


public class Main extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener,SensorEventListener{

    private static final int MY_PERMISSIONS_REQUEST = 99;//int bebas, maks 1 byte

    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;

    private GoogleMap mMap;
    private LatLng origin;
    private Marker me;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    private ArrayList<Marker> mMarkerArray = new ArrayList<Marker>();
    private ArrayList<Marker> gMarker = new ArrayList<Marker>();
    private ArrayList<Marker> pMarker = new ArrayList<Marker>();
    LocationManager locManager;

    private Boolean activityRunning = false;

    /**
     * Class storing every place in the system
     */
    public static class Places{
        public String desc;
        public String add;

        Places(String a, String b){
            desc = a;
            add = b;
        }
    }

    /**
     * on Create get player LastLogin, if different. Delete all green marker have been visited
     * build Google API
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sp = getSharedPreferences(SP,MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        if(!sp.getString("date","NULL").equals(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))){
            DbFitWalker db = new DbFitWalker(getApplicationContext());
            db.open();
            db.removeAll();
            db.close();
            ed.putString("date",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            ed.commit();
        }
        buildGoogleApiClient();
        createLocationRequest();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        /**
         * GET ID FOR BETTER LIFE
         */
        StringRequest postRequest = new StringRequest(Request.Method.POST, SBID_URL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // menampilkan respone
                        Log.d("Response ID", response);
                        SharedPreferences sp = getSharedPreferences(SP,MODE_PRIVATE);
                        SharedPreferences.Editor ed = sp.edit();
                        String mysz2 = response.replaceAll("\\s","");
                        ed.putString("id",mysz2);
                        ed.commit();

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {         // Menambahkan parameters post
                Map<String, String>  params = new HashMap<String, String>();
                SharedPreferences sp = getSharedPreferences(SP,MODE_PRIVATE);
                SharedPreferences.Editor ed = sp.edit();
                params.put("name",sp.getString("owner","FUCK"));

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(postRequest);

    }



    /**
     * Request location every 10 second
     * Request location high accuracy
     */
    protected void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        // 10 detik sekali meminta lokasi 10000ms = 10 detik
        mLocationRequest.setInterval(10000);
        // tapi tidak boleh lebih cepat dari 5 detik
        mLocationRequest.setFastestInterval(500);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * enable google location services API
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission diberikan, mulai ambil lokasi
                buildGoogleApiClient();

            } else {
                //permssion tidak diberikan, tampilkan pesan
                AlertDialog ad = new AlertDialog.Builder(this).create();
                ad.setMessage("Tidak mendapat ijin, tidak dapat mengambil lokasi");
                ad.show();
            }
            return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        ///STATUS TRUE
        StringRequest postRequest = new StringRequest(Request.Method.POST, UPDATE_URL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // menampilkan respone
                        Log.d("Response START", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.e(TAG, "onErrorResponse: Error= " + error);
                        Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                // Menambahkan parameters post
                Map<String, String>  params = new HashMap<String, String>();
                SharedPreferences sp = getSharedPreferences(SP,MODE_PRIVATE);
                params.put("id", sp.getString("id",null));
                params.put("stats","1");

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(postRequest);
    }


    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        StringRequest postRequest = new StringRequest(Request.Method.POST, UPDATE_URL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // menampilkan respone
                        Log.d("Response STOP", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.e(TAG, "onErrorResponse: Error= " + error);
                        Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                // Menambahkan parameters post
                Map<String, String>  params = new HashMap<String, String>();
                SharedPreferences sp = getSharedPreferences(SP,MODE_PRIVATE);
                SharedPreferences.Editor ed = sp.edit();
                params.put("id", sp.getString("id",null));
                params.put("stats","0");

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(postRequest);
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    protected void onResume() {
        super.onResume();
        activityRunning = true;
        Sensor countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if(countSensor != null) {
            mSensorManager.registerListener((SensorEventListener) this, countSensor, SensorManager.SENSOR_DELAY_UI);

        } else {
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        activityRunning = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            // tampilkan dialog minta ijin
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST);
//            mMap.setMyLocationEnabled(true);
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            location.setLatitude(-6.860422);
            location.setLongitude(107.589905);
        }
        origin = new LatLng(location.getLatitude(),location.getLongitude());
        me = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.current)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),17));
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        firstHandleLocation(origin,"mosque");
//        firstHandleLocation(origin,"park");
        handlePlayer();

    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        mMap.getUiSettings().setCompassEnabled(true);
//        mMap.getUiSettings().setZoomControlsEnabled(true);
        try {
            // Customise the styling of the base map using a JSON object defined
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));
            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }

    }
    private void handlePlayer(){
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, SELECT_URL, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.i(TAG, "onResponse: playerResult= " + response.toString());
                        parsePlayer(response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //menampilkan error pada logcat
                        Log.e(TAG, "onErrorResponse: Error= " + error);
                        Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());

                    }
                }
        );

        AppController.getInstance().addToRequestQueue(request);
    }
    private void parsePlayer(JSONObject result){
        String id,name, done,meter,photo,stats;
        double latitude, longitude;


        try {
            JSONArray jsonArray = result.getJSONArray("users");

            if (result.getString("success").equalsIgnoreCase("1")) {

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject user = jsonArray.getJSONObject(i);

                    id = user.getString("id");

                    name = user.getString("name");

                    latitude = user.getDouble("latitude");
                    longitude = user.getDouble("longitude");
                    LatLng latLng = new LatLng(latitude, longitude);
                    if (!user.isNull("done")) {
                        done = user.getString("done");
                    }

                    meter = user.getString("meter");

                    if (!user.isNull("photo")) {
                        photo = user.getString("photo");
                    }

                    stats = user.getString("stats");

                    SharedPreferences sp = getSharedPreferences(SP,MODE_PRIVATE);
                    SharedPreferences.Editor ed = sp.edit();
                    String hm = sp.getString("id",null);
                    if(stats.equals("1") && (!id.equals(sp.getString("id",null)))){
                        MarkerOptions mo = new MarkerOptions().position(latLng).title(name).icon(BitmapDescriptorFactory.fromResource(R.drawable.other)).snippet("Walked: "+meter+" m");
                        pMarker.add(mMap.addMarker(mo));
                    }

                }
            } else if (result.getString("success").equalsIgnoreCase("0")){

            }
        } catch (JSONException e) {

            e.printStackTrace();
            Log.e(TAG, "parseLocationResult: Error=" + e.getMessage());
        }
    }
    private void firstHandleLocation(LatLng firstLoc,String type){
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=").append(firstLoc.latitude).append(",").append(firstLoc.longitude);
        googlePlacesUrl.append("&radius=").append(PROXIMITY_RADIUS);
        googlePlacesUrl.append("&types=").append(type);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + GOOGLE_BROWSER_API_KEY);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, googlePlacesUrl.toString(), null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.i(TAG, "onResponse: firstResult= " + response.toString());
                        firstParse(response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //menampilkan error pada logcat
                        Log.e(TAG, "onErrorResponse: Error= " + error);
                        Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());

                    }
                }
        );

        AppController.getInstance().addToRequestQueue(request);
    }
    private void firstParse(JSONObject result){
        String id, place_id, placeName = null, reference, icon, vicinity = null;
        double latitude, longitude;

        try {
            JSONArray jsonArray = result.getJSONArray("results");

            if (result.getString(STATUS).equalsIgnoreCase(OK)) {
//                mMarkerArray.clear();
//                gMarker.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject place = jsonArray.getJSONObject(i);

                    if (!place.isNull(NAME)) {
                        placeName = place.getString(NAME);
                    }
                    if (!place.isNull(VICINITY)) {
                        vicinity = place.getString(VICINITY);
                    }
                    latitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
                            .getDouble(LATITUDE);
                    longitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
                            .getDouble(LONGITUDE);

                    LatLng latLng = new LatLng(latitude, longitude);

                    DbFitWalker db = new DbFitWalker(getApplicationContext());
                    db.open();

                    if(db.checkCoor(latLng.latitude,latLng.longitude)){
                        Marker added = mMap.addMarker(new MarkerOptions().position(latLng).title(placeName).icon(BitmapDescriptorFactory.fromResource(R.drawable.done)).snippet("Visited 1 times"));
                        added.setTag(new Places(placeName,vicinity));
                        gMarker.add(added);
                    }else{

                        Marker added = mMap.addMarker(new MarkerOptions().position(latLng).title(placeName).icon(BitmapDescriptorFactory.fromResource(R.drawable.mark)));
                        added.setTag(new Places(placeName,vicinity));
                        mMarkerArray.add(added);
                    }
                    db.close();

                }
            } else if (result.getString(STATUS).equalsIgnoreCase(ZERO_RESULTS)) {


            }
        } catch (JSONException e) {

            e.printStackTrace();
            Log.e(TAG, "parseLocationResult: Error=" + e.getMessage());
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                int stats = 0;

                for(Marker mrk : pMarker){
                    if(mrk.getTitle().equals(marker.getTitle()) && stats == 0){
                        stats = 1;
                        toOtherProfile(marker.getTitle());
                    }
                }
                if(marker.getTitle().equals("You")){
                    toProfile2();
                }else if(stats == 0){
                    Log.d("TITLE",marker.getTitle());
                    toInfoWindow(marker);
                }
            }
        });

//        currentPos.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));
        for(Marker marker : mMarkerArray){
            if(distance(origin.latitude,origin.longitude,marker.getPosition().latitude,marker.getPosition().longitude) <= 25){
                DbFitWalker db = new DbFitWalker(getApplicationContext());
                db.open();
                db.insertNew("Visited",marker.getPosition().latitude,marker.getPosition().longitude,0);
                Log.d("INSERTED",String.valueOf(marker.getPosition().latitude+"**"+marker.getPosition().longitude));
                db.close();

                Marker added = mMap.addMarker(new MarkerOptions().position(marker.getPosition()).title(marker.getTitle()).icon(BitmapDescriptorFactory.fromResource(R.drawable.done)).snippet("Visited 1 times"));
//                Log.d("GREEN",as.add+"--"+as.desc);
                added.setTag(marker.getTag());
                gMarker.add(added);
                marker.remove();

            }
        }
        Log.d("Origin",String.valueOf(origin.latitude)+"-------"+String.valueOf(origin.longitude));

        float perpindahan = distance(origin.latitude,origin.longitude,location.getLatitude(),location.getLongitude());
        SharedPreferences sp = getSharedPreferences(SP,MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putFloat("dist",sp.getFloat("dist",0)+((float)((double) Math.round(perpindahan*100)/100)));
        ed.commit();

        //handle new location
        for(Marker m : pMarker){
            m.remove();
        }
        handlePlayer();
        handleNewLocation(location);
    }
    private void handleNewLocation(final Location location) {
//        Toast.makeText(getBaseContext(), String.valueOf((float)((double) Math.round(perpindahan*100)/100))+"---"+String.valueOf(sp.getFloat("dist",0)), Toast.LENGTH_LONG).show();
//        mMap.clear();
//        Toast.makeText(getBaseContext(), me.getPosition()+"---"+location,
//                Toast.LENGTH_SHORT).show();
        me.remove();
        origin = new LatLng(location.getLatitude(),location.getLongitude());
        MarkerOptions mo = new MarkerOptions().position(origin).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.current));
        me =  mMap.addMarker(mo);
//        mMap.addCircle(new CircleOptions().center(origin).radius(20).fillColor(Color.argb(127,55,239,82)).clickable(false).strokeColor(Color.TRANSPARENT));

        StringRequest postRequest = new StringRequest(Request.Method.POST, UPDATE_URL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // menampilkan respone
                        Log.d("Response POST", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.e(TAG, "onErrorResponse: Error= " + error);
                        Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                // Menambahkan parameters post
                Map<String, String>  params = new HashMap<String, String>();
                SharedPreferences sp = getSharedPreferences(SP,MODE_PRIVATE);
                params.put("id", sp.getString("id",null));
                params.put("name", sp.getString("owner",null));
                params.put("lat", String.valueOf(location.getLatitude()));
                params.put("long", String.valueOf(location.getLongitude()));
                params.put("done", sp.getString("count",null));
                params.put("meter", String.valueOf(sp.getFloat("dist",0)));

                params.put("stats","1");

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(postRequest);




        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mMap.addCircle(new CircleOptions().center(origin).radius(20).fillColor(Color.argb(127,55,239,82)).clickable(false).strokeColor(Color.TRANSPARENT));
                marker.showInfoWindow();
                return false;
            }
        });

//        Log.d("Distance", String.valueOf(distance(location.getLatitude(),destination.latitude,location.getLongitude(),destination.longitude,0,0)));
    }

    public float distance (double lat_a, double lng_a, double lat_b, double lng_b )
    {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return new Float(distance * meterConversion).floatValue();
    }
    /**
     * onInfoWindowClick Listener
     * get Tag for every places
     * @param m
     */
    public void toInfoWindow(Marker m){
        Places as = (Places)m.getTag();
        Intent i = new Intent(this,InfoWindow.class);
        i.putExtra("namap",as.desc);
        i.putExtra("namaa",as.add);
        if(m.getSnippet() == null){
            i.putExtra("visited","not");
        }else{
            i.putExtra("visited","yes");
        }
        i.putExtra("cat","shake");
        startActivity(i);

    }
    public void toOtherProfile(final String title){

        StringRequest postRequest = new StringRequest(Request.Method.POST, SABID_URL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // menampilkan respone
                        Log.d("Response SABID", response);
                        toOtherProfile2(response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {         // Menambahkan parameters post
                Map<String, String>  params = new HashMap<String, String>();
                params.put("name",title);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(postRequest);


    }
    public void toOtherProfile2(String resp){
        Intent i = new Intent(this,OtherProfile.class);
        String[] split = resp.split("\\+");
        i.putExtra("data",split);
        startActivity(i);

    }
    public void toProfile(View v){
        Intent i = new Intent(this,Profile.class);
        DbFitWalker db = new DbFitWalker(getApplicationContext());
        db.open();
        i.putExtra("count",String.valueOf(db.selectAll().size()));
        db.close();

        startActivity(i);
    }
    public void toProfile2(){
        Intent i = new Intent(this,Profile.class);
        DbFitWalker db = new DbFitWalker(getApplicationContext());
        db.open();
        i.putExtra("count",String.valueOf(db.selectAll().size()));
        db.close();

        startActivity(i);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(activityRunning) {
            Toast.makeText(getBaseContext(), String.valueOf(event.values[0]+" x`Step"),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
