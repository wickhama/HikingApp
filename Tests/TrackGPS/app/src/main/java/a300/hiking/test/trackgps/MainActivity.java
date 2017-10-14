package a300.hiking.test.trackgps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private LocationManager locationManager;
    private double lat;
    private double longt;
    private TextView latView;
    private TextView longView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, this);
        latView = (TextView) findViewById(R.id.latDisplay);
        longView = (TextView) findViewById(R.id.longDisplay);

        latView.setText((new DecimalFormat(".#####").format(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude())).toString());
        longView.setText((new DecimalFormat(".#####").format(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude())).toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        //lat = location.getLatitude();
        //longt = location.getLongitude();
        latView.setText((new DecimalFormat(".#####").format(location.getLatitude())).toString());
        longView.setText((new DecimalFormat(".#####").format(location.getLongitude())).toString());

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
