package arc.com.arctrails;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.DebugUtils;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

/**
 * This Service runs in the background and records the user's coordinates while the user walks.
 * Bind activity to access methods for recording.
 * Methods: pauseRecording(), resumeRecording(), stop_Recording()
 * List<Location> is returned when stop_Recording() is called.
 */

public class Tracking extends Service {

    private FusedLocationProviderClient flocatClient;
    private ArrayList<Location> trail;
    private LocationRequest locationRequest = new LocationRequest();
    private LocationCallback locationCallback;
    private final IBinder locationBinder = new LocalBinder();

    @Override
    public void onCreate() {
        trail = new ArrayList();
        flocatClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    trail.add(location);
                }
            }
        };
    }

    public LatLng getLastLocation() {
        //Asks for permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
        }
        double lat = flocatClient.getLastLocation().getResult().getLatitude();
        double lon = flocatClient.getLastLocation().getResult().getLongitude();
        return new LatLng(lat, lon);
    }

    public ArrayList<Location> pauseRecording() {
        flocatClient.removeLocationUpdates(locationCallback);
        return trail;
    }

    public void resumeRecording() {
        //Asks for permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
        }
        trail.clear();
        flocatClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    /* stop_Recording
    @Returns List<Location> : trail Coordinates
    Ayla
     */
    public ArrayList<Location> stop_Recording() {
        return trail;
    }

    public boolean isTrailEmpty() {
        return trail.isEmpty();
    }

    /* Clean up:
        clears trail
     */
    @Override
    public void onDestroy() {
        flocatClient.removeLocationUpdates(locationCallback);
        trail.clear();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //Asks for permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();//TODO: Change to request permission
        }
        flocatClient.requestLocationUpdates(locationRequest, locationCallback, null);
        return locationBinder;
    }

    /* LocalBinder extends Binder to allow us to
    access methods: pauseRecording, resumeRecording, stop_Recording
    Ayla
     */
    public class LocalBinder extends Binder {
        Tracking getService(){
            return Tracking.this;
        }
    }
}