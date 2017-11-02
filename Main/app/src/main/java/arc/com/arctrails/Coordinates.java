package arc.com.arctrails;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static java.lang.String.format;


public class Coordinates extends Fragment implements LocationListener{

    private LocationManager locationManager;
    private TextView latView, longView;

    //TODO: input missing permissions for requestLocationUpdates
    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coordinates, container, false);        // Inflate the layout for this fragment
        latView = (TextView) view.findViewById(R.id.latitude);
        longView = (TextView) view.findViewById(R.id.longitude);

        return view;
    }

    @Override
    public void onLocationChanged(Location location) {
        latView.setText(format("%.5f", location.getLatitude()));
        longView.setText(format("%5f", location.getLongitude()));
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
