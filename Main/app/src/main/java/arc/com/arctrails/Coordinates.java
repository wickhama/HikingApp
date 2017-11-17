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

import static java.lang.String.format;


public class Coordinates extends Fragment implements LocationListener, LocationPermissionListener {

    private TextView latView, longView;
    private LocationRequestListener mRequestListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mRequestListener = (LocationRequestListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement LocationRequestListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coordinates, container, false);   // Inflate the layout for this fragment
        latView = view.findViewById(R.id.latitude);
        longView = view.findViewById(R.id.longitude);

        mRequestListener.requestPermission(this);

        return view;
    }

    /*
     * Once permission has been granted, we can begin tracking coordinates
     */
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
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
                onLocationChanged(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
            }
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            latView.setText(format("%.5f", location.getLatitude()));
            longView.setText(format("%.5f", location.getLongitude()));
        }
    }

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
