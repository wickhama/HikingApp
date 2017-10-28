package com.example.stupidgpsthing;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    public static final int locReqNum = 99;
    //String locProvider;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private TextView latView;
    private TextView longView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //locProvider = locationManager.getBestProvider(new Criteria(), false);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, this);
                latView = findViewById(R.id.latDisplay);
                longView = findViewById(R.id.longDisplay);

                latView.setText((new DecimalFormat(".#####").format(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude())));
                longView.setText((new DecimalFormat(".#####").format(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude())));
            }
        }
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
        GoogleMap mMap;
        mMap = googleMap;

        // Add a marker in user location
        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                LatLng myLocation = new LatLng(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude(), locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude());
                mMap.addMarker(new MarkerOptions().position(myLocation).title("Starting Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            }
        }
    }

    public boolean checkLocationPermission() {
        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //if they want an explanation
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //Need to add this to GUI to resolve the errors**************
                //these will turn into their respective popup counterparts
                new AlertDialog.Builder(this).setTitle(R.string.title_location_permission).setMessage(R.string.text_location_permission).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //user prompt
                        ActivityCompat.requestPermissions(MapsActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, locReqNum);
                    }
                })
                .create().show();
            }
            else {*/
                //just request the damn permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, locReqNum);
            /*}
            return false;
        }
        else {*/
            return true;
        //}
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}