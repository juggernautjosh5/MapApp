package com.example.macbookstudiopro.mapapp;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.util.Log;
import android.util.LogPrinter;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.0f;


    private double latitude;
    private double longitude;

    private String[] results;
    private LatLng searchQ;

    EditText searching;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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


        searching = (EditText) findViewById(R.id.editText_Search);

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(32.885035, -117.225467);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Born Here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 3);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 3);
        }

        getLocation();


    }

    public void switchView(View v) {
        mMap.setMapType(((mMap.getMapType() + 1) % 3) + 1);
    }


    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //get GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) Log.d("MyMaps", "getLocation: GPS is enabled");

            //get Network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) Log.d("MyMaps", "getLocation: Network is enabled");

            if (!isNetworkEnabled && !isGPSEnabled) {
                Log.d("MyMaps", "getLocation: No provider is avalible!!");
            } else {
                canGetLocation = true;
                if (isGPSEnabled) {
                    Log.d("MyMaps", "getLocation: GPS enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGps);
                    Log.d("MyMaps", "getLocation: GPS update request success");
                    Toast.makeText(this, "Using GPS", Toast.LENGTH_SHORT).show();
                }
                if (isNetworkEnabled) {
                    Log.d("MyMaps", "getLocation: Network enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    Log.d("MyMaps", "getLocation: Network update request success");
                    Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT).show();
                }


            }


        } catch (Exception e) {
            Log.d("MyMaps", "getLocation: Catch_an_exception_in_getLocation");
            e.printStackTrace();
        }

    }

    private final LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.d("MyMaps", "LocationListenerGPS: Got location");
            Log.d("MyMaps", "LocationListenerGPS-" + location.getLatitude() + " " + location.getLongitude());

            longitude = location.getLongitude();
            latitude = location.getLatitude();
            LatLng currentLocation = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
            Toast.makeText(MapsActivity.this, "Update: New GPS Marker", Toast.LENGTH_SHORT).show();
            locationManager.removeUpdates(locationListenerNetwork);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMaps", "LocationListenerGPS: Status Change");
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("MyMaps", "GPS is now avalible");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGps);
                    Toast.makeText(MapsActivity.this, "Update: GPS avalible", Toast.LENGTH_SHORT).show();
                    break;


                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("MyMaps", "GPS is temporarily unavalible");
                    Toast.makeText(MapsActivity.this, "Update: GPS unavalible", Toast.LENGTH_SHORT).show();
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;

                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMaps", "GPS is out of service");
                    Toast.makeText(MapsActivity.this, "Update: GPS out of service", Toast.LENGTH_SHORT).show();
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;

                default:
                    Log.d("MyMaps", "GPS default");
                    Toast.makeText(MapsActivity.this, "Update: GPS default", Toast.LENGTH_SHORT).show();
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;


            }
        }
        //getLocation();


        @Override
        public void onProviderEnabled(String provider) {
            Log.d("MyMaps", "LocationListenerGPS: Provider enabled - " + provider);

        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("MyMaps", "LocationListenerGPS: Provider diabled - " + provider);

        }
    };

    private final LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.d("MyMaps", "LocationListenerNetwork: Got location");
            Log.d("MyMaps", "LocationListenerNetwork-" + location.getLatitude() + " " + location.getLongitude());
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            LatLng currentLocation = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
            Toast.makeText(MapsActivity.this, "Update: New Network Marker", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMaps", "LocationListenerNetwork: Status Change");
            getLocation();


        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("MyMaps", "LocationListenerNetwork: Provider enabled - " + provider);

        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("MyMaps", "LocationListenerNetwork: Provider diabled - " + provider);
            getLocation();

        }
    };

    public void searchIt(View v) {
        for (int i = 0; i<10;i++){
            Log.d("JSwa", "Search ativated");
            String siteUrl = "https://maps.googleapis.com/maps/api/place/radarsearch/json?keyword=" + searching.getText().toString() + "&location=" + latitude + "," + longitude + "&radius=9000&key=AIzaSyDB5yRV7iUIKx2K4OYKe4oFt7R-VlW9mr0";
            (new ParseURL()).execute(new String[]{siteUrl});
        }



    }

    public void clearIt(View v) {
        mMap.clear();
    }


    private class ParseURL extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String buffer = new String();
            try {
                Log.d("JSwa", "Connecting to [" + strings[0] + "]");
                Document doc = Jsoup.connect(strings[0]).ignoreContentType(true).get();
                Log.d("JSwa", "Connected to [" + strings[0] + "]");
                // Get document (HTML page) title

                Element bod = doc.body();
                buffer = ("BOD TEXT  " + bod.text() );
                Log.d("JSwa", ""+buffer.toString());
                results = new String[201];
                int j = 0;
                while (buffer.indexOf("location")>-1){
                    results[j] = buffer.substring(buffer.indexOf("lat")+7,buffer.indexOf("lng")-3 ) + " " + buffer.substring(buffer.indexOf("lng")+7, buffer.indexOf("lng")+16);
                    buffer = buffer.substring(buffer.indexOf("lng")+20);
                    j++;
                }


                Log.d("JSwa", ""+buffer.toString());
                for (String str: results){
                    Log.d("JSwa", ""+str);
                }
                markMaker();



            } catch (Throwable t) {
                Log.d("JSwa", "ERROR");
                t.printStackTrace();
            }

            return buffer.toString();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //respText.setText(s);
        }
    }

    public void markMaker(){
        for (String str: results){
            Log.d("JSwa", "Start Marking");
            searchQ = new LatLng(Double.parseDouble(str.substring(0,str.indexOf(" ")-1)), Double.parseDouble(str.substring(str.indexOf(" ")+1)));
            Log.d("JSwa", ""+searchQ.latitude + " "+searchQ.longitude);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMap.addMarker(new MarkerOptions().position(searchQ).title(searching.getText().toString()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                }
            });
            Log.d("JSwa", "Stop Marking");
        }
    }
}
