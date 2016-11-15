package sqrtstudio.com.fitwalker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
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

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static sqrtstudio.com.fitwalker.AppConfig.GEOMETRY;
import static sqrtstudio.com.fitwalker.AppConfig.GOOGLE_BROWSER_API_KEY;
import static sqrtstudio.com.fitwalker.AppConfig.ICON;
import static sqrtstudio.com.fitwalker.AppConfig.LATITUDE;
import static sqrtstudio.com.fitwalker.AppConfig.LOCATION;
import static sqrtstudio.com.fitwalker.AppConfig.LONGITUDE;
import static sqrtstudio.com.fitwalker.AppConfig.NAME;
import static sqrtstudio.com.fitwalker.AppConfig.OK;
import static sqrtstudio.com.fitwalker.AppConfig.PLACE_ID;
import static sqrtstudio.com.fitwalker.AppConfig.PROXIMITY_RADIUS;
import static sqrtstudio.com.fitwalker.AppConfig.REFERENCE;
import static sqrtstudio.com.fitwalker.AppConfig.SP;
import static sqrtstudio.com.fitwalker.AppConfig.STATUS;
import static sqrtstudio.com.fitwalker.AppConfig.SUPERMARKET_ID;
import static sqrtstudio.com.fitwalker.AppConfig.TAG;
import static sqrtstudio.com.fitwalker.AppConfig.VICINITY;
import static sqrtstudio.com.fitwalker.AppConfig.ZERO_RESULTS;

public class Main extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private static final int MY_PERMISSIONS_REQUEST = 99;//int bebas, maks 1 byte
    private GoogleMap mMap;
    private LatLng origin;
    private Marker me;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    private ArrayList<Marker> mMarkerArray = new ArrayList<Marker>();
    LocationManager locManager;

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

    }
    protected void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        // 10 detik sekali meminta lokasi 10000ms = 10 detik
        mLocationRequest.setInterval(20000);
        // tapi tidak boleh lebih cepat dari 5 detik
        mLocationRequest.setFastestInterval(10000);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
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
    }
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
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


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            // tampilkan dialog minta ijin
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST);
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
        handleNewLocation(location);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private void handleNewLocation(Location location) {
//        Toast.makeText(getBaseContext(), String.valueOf((float)((double) Math.round(perpindahan*100)/100))+"---"+String.valueOf(sp.getFloat("dist",0)), Toast.LENGTH_LONG).show();
        mMap.clear();
        origin = new LatLng(location.getLatitude(),location.getLongitude());
        MarkerOptions mo = new MarkerOptions().position(origin).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.current));
        me =  mMap.addMarker(mo);

        mMap.addCircle(new CircleOptions().center(origin).radius(10).fillColor(Color.argb(127,55,239,82)).clickable(false).strokeColor(Color.TRANSPARENT));


//        loadNearByPlaces(origin.latitude,origin.longitude,"cafe");
//        loadNearByPlaces(origin.latitude,origin.longitude,"school");
        loadNearByPlaces(origin.latitude,origin.longitude,"mosque");

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
            mMap.addCircle(new CircleOptions().center(origin).radius(10).fillColor(Color.argb(127,55,239,82)).clickable(false).strokeColor(Color.TRANSPARENT));
                marker.showInfoWindow();

                return false;
            }
        });
//        Log.d("Distance", String.valueOf(distance(location.getLatitude(),destination.latitude,location.getLongitude(),destination.longitude,0,0)));
    }
    @Override
    public void onLocationChanged(Location location) {

//        currentPos.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));
        for(Marker marker : mMarkerArray){
//            Log.d("DIST",String.valueOf(distance(origin.latitude,origin.longitude,marker.getPosition().latitude,marker.getPosition().longitude)));
            if(distance(origin.latitude,origin.longitude,marker.getPosition().latitude,marker.getPosition().longitude) <= 35){
                DbFitWalker db = new DbFitWalker(getApplicationContext());
                db.open();
                db.insertNew("Visited",marker.getPosition().latitude,marker.getPosition().longitude,0);
                Log.d("INSERTED",String.valueOf(marker.getPosition().latitude+"**"+marker.getPosition().longitude));
                db.close();
            }
        }
        Log.d("Origin",String.valueOf(origin.latitude)+"-------"+String.valueOf(origin.longitude));
        float perpindahan = distance(origin.latitude,origin.longitude,location.getLatitude(),location.getLongitude());
        SharedPreferences sp = getSharedPreferences(SP,MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putFloat("dist",sp.getFloat("dist",0)+((float)((double) Math.round(perpindahan*100)/100)));
        ed.commit();
        handleNewLocation(location);
    }
    private String getMapsApiDirectionsUrl(LatLng origin,LatLng dest) {
        String waypoints = "origin="
                + origin.latitude + "," + origin.longitude
                + "&destination=" + dest.latitude + ","
                + dest.longitude;

        String sensor = "sensor=false";
        String params = waypoints + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;
        return url;
    }
    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }
    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(10);
                polyLineOptions.color(Color.RED);
            }

            mMap.addPolyline(polyLineOptions);
        }
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
    private void loadNearByPlaces(double latitude, double longitude,String type) {
//YOU Can change this type at your own will, e.g hospital, cafe, restaurant.... and see how it all works
        StringBuilder googlePlacesUrl =
                new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=").append(latitude).append(",").append(longitude);
        googlePlacesUrl.append("&radius=").append(PROXIMITY_RADIUS);
        googlePlacesUrl.append("&types=").append(type);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + GOOGLE_BROWSER_API_KEY);
        JsonObjectRequest request = new JsonObjectRequest(googlePlacesUrl.toString(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject result) {

                        Log.i(TAG, "onResponse: Result= " + result.toString());
                        parseLocationResult(result);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: Error= " + error);
                        Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());
                    }
                });

        AppController.getInstance().addToRequestQueue(request);
    }

    private void parseLocationResult(JSONObject result) {

        String id, place_id, placeName = null, reference, icon, vicinity = null;
        double latitude, longitude;

        try {
            JSONArray jsonArray = result.getJSONArray("results");

            if (result.getString(STATUS).equalsIgnoreCase(OK)) {

//                mMap.clear();
                mMarkerArray.clear();

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
                    }else{
                        Marker added = mMap.addMarker(new MarkerOptions().position(latLng).title(placeName).icon(BitmapDescriptorFactory.fromResource(R.drawable.mark)));
                        mMarkerArray.add(added);
                    }
                    db.close();

                }
//                Toast.makeText(getBaseContext(), jsonArray.length() + " Supermarkets found!",
//                        Toast.LENGTH_LONG).show();
            } else if (result.getString(STATUS).equalsIgnoreCase(ZERO_RESULTS)) {
                Toast.makeText(getBaseContext(), "Nothing Here :(",
                        Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e) {

            e.printStackTrace();
            Log.e(TAG, "parseLocationResult: Error=" + e.getMessage());
        }
    }
    public void toProfile(View v){
        Intent i = new Intent(this,Profile.class);
        DbFitWalker db = new DbFitWalker(getApplicationContext());
        db.open();
        i.putExtra("count",String.valueOf(db.selectAll().size()));
        db.close();
        startActivity(i);

    }
}
