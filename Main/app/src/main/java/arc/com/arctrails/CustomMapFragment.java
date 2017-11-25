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
 * @author Caleigh
 * @since 14-10-17
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
        OnMapReadyCallback, LocationPermissionListener
{
    private static final String TAG = CustomMapFragment.class.getSimpleName();
    private LocationRequestListener mRequestListener;

    String locProvider;
    private GoogleMap mMap;
    private Location mLastKnownLocation;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int DEFAULT_ZOOM = 15;

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    /**
     * @author Ryley
     * @since 14-10-17

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
     * @author Ryley & Caleigh
     * @since 14-10-17

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
     * @author Caleigh
     * @since 14-10-17

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
     * @author Caleigh
     * @since 14-10-17

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
     * @author Caleigh
     * @since 14-10-17

     * Creates the blue dot centred on the user's location for the map
     * - Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
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

    /** set the location controls on the map. If the user has granted location permission, enable the My Location
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
     * Draw a path from an arrayList of waypoints and update camera to the start of the trail.
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
     * Draw a trail from a hashset of paths in a GPX file
     */
    public void makeTrail(GPX trail) {
        mMap.clear();
        HashSet<Track> tracks;
        HashSet<Waypoint> points;
        int numWaypoints = 0;
        double lat = 0;
        double lng = 0;
        if((points = trail.getWaypoints()) != null && !points.isEmpty()) {     //Prevents crash if there are no waypoints in GPX File (aw)
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
