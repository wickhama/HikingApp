package arc.com.arctrails;

import android.Manifest;
import android.app.FragmentManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * This Service runs in the background and records the user's coordinates while the user walks.
 * Bind activity to access methods for recording.
 * Methods: pauseRecording(), resumeRecording(), stop_Recording()
 * List<Location> is returned when stop_Recording() is called.
 */

public class Tracking extends Service {

    final static String LOCATION_FOUND = "LOCATION_FOUND";

    private FusedLocationProviderClient flocatClient;
    private final IBinder locationBinder = new LocalBinder();
    private LocationRequest locationRequest = new LocationRequest();
    private LocationCallback locationCallback;

    private ArrayList<LatLng> trail;
    private GoogleMap map;
    private PolylineOptions polylineOptions;

    private boolean mAllowRebind;

    @Override
    public void onCreate() {
        //Asks for permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
        }
        trail = new ArrayList();

        flocatClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            private Intent intent = new Intent();
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                intent.setAction(LOCATION_FOUND);
                for (Location location : locationResult.getLocations()) {
                    trail.add(new LatLng(location.getLatitude(), location.getLongitude()));
                    intent.putExtra("location", trail);
                    sendBroadcast(intent);
                }
            }
        };
    }

    public LatLng getLastLocation() {
        //Asks for permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
        }
        /*double lat, lon;
        lat = trail.get(trail.size()-1).getLatitude();
        lon = trail.get(trail.size()-1).getLongitude();
        return new LatLng(lat, lon);*/
        return trail.get(trail.size()-1);
    }

    public ArrayList<LatLng> pauseRecording() {
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
    public ArrayList<LatLng> stopRecording() {
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
        flocatClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful() && task.getResult() != null)
                    trail.add(new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude()));
            }
        });

        flocatClient.requestLocationUpdates(locationRequest, locationCallback, null);
        return locationBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
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