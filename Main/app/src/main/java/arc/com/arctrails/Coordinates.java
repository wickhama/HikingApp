package arc.com.arctrails;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import static java.lang.String.format;

/**Coordinates displays a fragment in the side menu
 * and shows the user's current location if permissions is granted
 *
 * Created by Ayla Wickham Increment 1
 * Modified by Ryley
 */

public class Coordinates extends Fragment implements LocationListener, LocationPermissionListener {

    //The text boxes displaying position
    private TextView latView, longView;
    //A listener that handles permission requests
    private LocationRequestListener mRequestListener;
    //A flag for knowing whether to store location data
    private boolean recording;
    //A list of recorded data
    private ArrayList<Double[]> trail = new ArrayList<>();

    /**Created by Ryley Increment 1
     *
     * Called at startup
     * Uses the context this fragment is attached to as a permission listener
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            //try to use the context as a listener
            mRequestListener = (LocationRequestListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement LocationRequestListener");
        }
    }

    /**Created by Ayla Wickham Increment 1
     * Builds the layout of the fragment.
     * Tracks the textboxes for later updates.
     *
     * Modified by Ryley - Increment 1
     * Instead of immediately trying to begin tracking, requests permission and
     * begins tracking if permission is granted
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coordinates, container, false);   // Inflate the layout for this fragment
        latView = view.findViewById(R.id.latitude);
        longView = view.findViewById(R.id.longitude);

        //once the layout is built, ask for location permission
        mRequestListener.requestPermission(this);

        return view;
    }

    /**Created by Ryley Increment 1
     * Modified by Caleigh
     *
     * Once permission has been granted, we can begin tracking coordinates
     **/
    @Override
    public void onPermissionResult(boolean result) {
        if (result) {
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            // Added to double-check that location is available ~Caleigh
            if (locationManager != null) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                //begin tracking
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
                //set the initial position
                onLocationChanged(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
            }
        }
    }

    /**Created by Ayla Wickham Increment 3
     * Records latitude and longitude on location change
     * Only called when user wishes to create a new trail.
     */
    public void record() {
        trail.clear();
        recording = true;
    }

    /**Created by Ayla Wickham Increment 3
     * Stops recording and returns an ArrayList<Double[]> of points.
     */
    public ArrayList<Double[]> stopRecord() {
        recording = false;
        return trail;
    }

    /**Created by Ayla Wickham Increment 1
     *
     * When the user moves, update the display
     *
     * Modified in increment 3
     *      If the user is recording, store the new location
     *
     * Supress DefaultLocale: AndroidStudio complains about printf's locale, which I assume means
     * the language? The app is only being produced in english so this is not a concern -Ryley
     */
    @SuppressLint("DefaultLocale")
    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            double lat= location.getLatitude();
            double lon = location.getLongitude();

            latView.setText(format("%.5f", lat));
            longView.setText(format("%.5f", lon));
            if(recording) {
                Double[] point = {lat, lon};
                trail.add(point);
            }
        }
    }

    //Dummy methods for implementing a LocationListener
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }
}
