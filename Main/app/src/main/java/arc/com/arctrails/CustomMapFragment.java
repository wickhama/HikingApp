package arc.com.arctrails;

import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Track;
import org.alternativevision.gpx.beans.Waypoint;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Caleigh
 * Added for increment 1

 * This class contains the map and includes everything done using the google maps api.

 * Methods:
 * onAttach (handles what to do when the map fragment is associated with its activity)
 * onCreate (essentially the  main method - adds all the default items into the newly created map)
 * onMapReady (initiates map-related tasks after the map is loaded)
 * onPermissionResult (begins calling permission-related tasks when the user allows location permissions)
 * moveCameraLocation (updates the zoom location on the map when called)
 * updateLocationUI (moves the blue dot as the gps updates)
 * onSaveInstantState (saves the location data when app is closed or new activity called)
 * drawPath (Draws a polyline on the map using an arrayList of coordinates)
 * makeTrail (Creates a trail using Waypoints and paths from the input GPX file)
 */
public class CustomMapFragment extends SupportMapFragment implements
        OnMapReadyCallback, LocationPermissionListener {
    /**
     * Instance variables mostly added by Caleigh.
     */
    // The TAG does nothing within the cat, but provides output on logcat within the IDE
    private static final String TAG = CustomMapFragment.class.getSimpleName();

    // Added by Ryley for calling the location request fragment
    private LocationRequestListener mRequestListener;

    // Defined later for storing the current location provider
    String locProvider;

    // Instance for the current map
    private GoogleMap mMap;

    // Instance for the user's last known location
    private Location mLastKnownLocation;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // This will be the default camera zoom for the user's location
    private static final int DEFAULT_ZOOM = 15;

    // Instances for saving the state before closing
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    /**
     * Created by Ryley
     * Added for increment 1

     * This activity is called when the map fragment becomes owned by its related context
     * We needed to override this to add the location request listener so that we could
     * request the user's location

     * @param context
     */
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        try {
            mRequestListener = (LocationRequestListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement LocationRequestListener");
        }
    }

    /**
     * Created by Ryley & Caleigh
     * Added for increment 1

     * This activity is called directly after onAttach, and is needed for initial fragment creation.
     * It may be called when the activity is still being created, so we couldn't add visual assets to
     * the map until onMapReady is called.

     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locProvider = locationManager != null ? locationManager.getBestProvider(new Criteria(), false) : null;
        // Get notified when the map is ready to be used
        getMapAsync(this);
    }

    /**
     * Created by Caleigh
     * Added for increment 1

     * Manipulates the map once is fully available.
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
        mRequestListener.requestPermission(this);
    }

    /**
     * Created by Caleigh
     * Added for increment 1

     * Gets called the moment the user responds to the location permissions request. If permission
     * is granted, we add in the location-specific utilities AKA. the blue dot on the user's
     * location and the camera's zoom

     * @param result
     */
    @Override
    public void onPermissionResult(boolean result)
    {
        if(result)
        {
            moveCameraLocation();
            updateLocationUI();
        }
    }

    /**
     * Created by Caleigh
     * Added for increment 1

     * Creates the blue dot centred on the user's location for the map.
     * Gets the best and most recent location of the device, which may be null in rare cases when a
     * location is not available. We handle this with
     */
    private void moveCameraLocation() {
        try {
            if (mRequestListener.hasPermission()) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if(mLastKnownLocation != null)
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Created by Caleigh
     * Added for increment 1

     * set the location controls on the map. If the user has granted location permission, enable the My Location
     * layer and the related control on the map, otherwise disable the layer and the control, and set the current
     * location to null
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mRequestListener.hasPermission()) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            }
            else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Created by Caleigh
     * Added for increment 1

     * Saves the instance and current coordinates as the user closes the app, just in case GPS or
     * Google Play data isn't available next time app is opened.

     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }
    /**
     * Created by Caleigh
     * Added for increment 3

     * Draw a polyline path from an arrayList of waypoints and update camera to the start of the
     * trail. To make it more nature-esque, I coloured the path a custom green colour.
     */
    private void drawPath(ArrayList<Waypoint> points) {
        PolylineOptions polylineOptions = new PolylineOptions();
        int len = points.size();
        for (int i=0; i<len; ++i) {
            LatLng iLatLng = new LatLng(points.get(i).getLatitude(),points.get(i).getLongitude());
            polylineOptions.add(iLatLng).width(5).color(Color.rgb(60,195,0)).geodesic(true);
        }
        mMap.addPolyline(polylineOptions);
    }

    /**
     * Created by Caleigh
     * Added for increment 3

     * Add the waypoints in the GPX file into the map, and call the drawPath method  to draw the
     * paths using the coordinates from the same GPX file
     */
    public void makeTrail(GPX trail) {
        mMap.clear();
        HashSet<Track> tracks;
        HashSet<Waypoint> points;
        points = trail.getWaypoints();
        double lat = 0;
        double lng = 0;
        if(!points.isEmpty() || points != null) { //Prevents crash if there are no waypoints in GPX File
            int numWaypoints = 0;
            for (Waypoint w : points) {
                LatLng wLatLng = new LatLng(w.getLatitude(), w.getLongitude());
                mMap.addMarker(new MarkerOptions().position(wLatLng));
                lat += w.getLatitude();
                lng += w.getLongitude();
                numWaypoints++;
            }
            lat /= numWaypoints;
            lng /= numWaypoints;
            LatLng zLatLng = new LatLng(lat, lng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zLatLng, 14));
        }
        tracks = trail.getTracks();
        for (Track t : tracks) {
            drawPath(t.getTrackPoints());
        }
    }
}
