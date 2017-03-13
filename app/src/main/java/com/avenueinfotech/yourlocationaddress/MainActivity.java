package com.avenueinfotech.yourlocationaddress;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.avenueinfotech.yourlocationaddress.utils.GPSTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMyLocationButtonClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap mMap;
    private GoogleApiClient mApiClient;
    private TextView textView;
    ConnectionDetector cd;
    WifiManager wifiManager;
    TextView wifiStatus;
    Switch wifiSwitch;
    private ProgressDialog dialog;
    private static GPSTracker gps;
    final int REQUEST_LOCATION = 199;
    public final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    String[] PERMISSIONS = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-1183672799205641~7719191016");

        if (googleServicesAvailable()) {
            Toast.makeText(this, "Google services present", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_main);
            initMap();
        } else {
            Toast.makeText(this, "Google services Absent", Toast.LENGTH_LONG).show();
        }

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();

        gps = new GPSTracker(this);

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_ID_MULTIPLE_PERMISSIONS);
        } else {

            if (!gps.canGetLocation()) {
                switchGPS();
            }

//            GeneralUtils.createDirectory();
        }

        dialog = new ProgressDialog(MainActivity.this);
        dialog.setCancelable(false);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiSwitch = (Switch)findViewById(R.id.wifiswitch);
        wifiStatus = (TextView)findViewById(R.id.wifistatus);

        if (wifiManager.isWifiEnabled()){
            wifiSwitch.setChecked(true);
            wifiStatus.setText("Wifi ON");
        } else {
            wifiSwitch.setChecked(false);
            wifiStatus.setText("Wifi OFF");
        }

        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    wifiManager.setWifiEnabled(true);
                    wifiStatus.setText("Wifi On");
                    Toast.makeText(MainActivity.this, "Wifi may take a moment to turn ON", Toast.LENGTH_LONG).show();
                }else {
                    wifiManager.setWifiEnabled(false);
                    wifiStatus.setText("Wifi Off");
                    Toast.makeText(MainActivity.this, "Wifi is switched OFF", Toast.LENGTH_LONG).show();
                }
            }
        });

//        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
//        mapFragment.getMapAsync(this);

        cd = new ConnectionDetector(this);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
        if (cd.isConnected()) {
            Toast.makeText(MainActivity.this, "Internet Connected", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Please Enable Internet", Toast.LENGTH_LONG).show();
        }

        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        textView = (TextView) findViewById(R.id.textView);
//
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void switchGPS() {
        {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            // **************************
            builder.setAlwaysShow(true); // this is the key ingredient
            // **************************

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                    .checkLocationSettings(mApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result
                            .getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can
                            // initialize location
                            // requests here.
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be
                            // fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling
                                // startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(MainActivity.this, REQUEST_LOCATION);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have
                            // no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }
    }

    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cant connect to play services", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void initMap() {
//        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
//        mapFragment.getMapAsync(this);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
//        LatLng sydney = new LatLng(-33.867, 151.206);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;


        }
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();


    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }


//        if(googleSevicesAvailable()){
//            Toast.makeText(this, "play services exits", Toast.LENGTH_SHORT).show();
//            setContentView(R.layout.activity_main);
//            initMap();
//        } else {
//
//        }
//    }
//
//    private void initMap() {
//        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.mapFragment);
//        mapFragment.getMapAsync(this);
//    }
//
//    public boolean googleSevicesAvailable(){
//        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
//        int isAvailable = api.isGooglePlayServicesAvailable(this);
//        if(isAvailable == ConnectionResult.SUCCESS){
//            return true;
//        } else  if (api.isUserResolvableError(isAvailable)){
//            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
//            dialog.show();
//        } else {
//            Toast.makeText(this, "canot concet to play sevrvices", Toast.LENGTH_SHORT).show();
//        }
//        return false;
//    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mGoogleMap = googleMap;
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mapTypeNone:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.mapTypeNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeSatellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeTerrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeHybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    LocationRequest mLocationRequest;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(36000);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, mLocationRequest, this);
        Log.i("LOG", "onConnection(" + bundle + ")");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
//        Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        if(l != null){
//            Log.i("LOG", "Lat: "+l.getLatitude());
//            Log.i("LOG", "Log: "+l.getLongitude());
//            tvCoordiante.setText(l.getLatitude()+" | "+l.getLongitude());
//
//        }
//
//        startLocationUpdate();


    }
//    private void initLocationRequest() {
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(5000);
//        mLocationRequest.setFastestInterval(2000);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//    }

//    private void startLocationUpdate() {
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        initLocationRequest();
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MainActivity.this);
//    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location == null){
            Toast.makeText(this, "Cant get current location", Toast.LENGTH_LONG).show();
        } else {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 19);
            mMap.animateCamera(update);
            textView.setText("Long: " + location.getLongitude() + " Lat: " + location.getLatitude() +  " Alt:" + location.getAltitude() +  " Acc:" + location.getAccuracy()+  "m" );



        }

//        tvCoordiante.setText(Html.fromHtml("Location: "+location.getLatitude()+"<br />"+
//                "Longitude: "+location.getLongitude()+"<br />'"+
//                "Bearing: "+location.getBearing()+"<br />"+
//                "Altitude: "+location.getAltitude()+"<br />"+
//                "Speed: "+location.getSpeed()+"<br />"+
//                "Provider: "+location.getProvider()+"<br />"+
//                "Accurucy: "+location.getAccuracy()+"<br />"+
//                "Speed: "+ DateFormat.getTimeFormat(this).format(new Date())+"<br +>"
//        " Accurcy:" + location.getAccuracy() +"mts" +
//        ));
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }
}
