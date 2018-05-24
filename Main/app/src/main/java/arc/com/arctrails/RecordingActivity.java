package arc.com.arctrails;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ToggleButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class RecordingActivity extends AppCompatActivity
        implements LocationRequestListener, LocationPermissionListener,
        GoogleMap.OnMapClickListener, WaypointDialog.WaypointDialogListener{

    //Identifies the type of permission requests to identify which ones were granted
    //although we only need need fine location for this specific case
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 99;
    //keeps a track of the listeners waiting for permissions
    private Set<LocationPermissionListener> mListeners;

    //return result codes
    //result if the user presses back
    public static final int RESULT_BACK= 0;

    //identifies the result source when a child activity finishes
    //ID for NewTrailActivity results
    public static final int NEW_TRAIL_REQUEST_CODE = 3;

    //the data recorded in the user's most recent trail
    private Trail recordedTrail = null;

    //the selected location to place a waypoint
    private LatLng waypointLatLng;

    //Object to manage location tracking
    private Coordinates location;

    private CustomMapFragment map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListeners = new HashSet<>();

        setContentView(R.layout.activity_recording);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((ToggleButton)findViewById(R.id.recordButton)).isChecked()){
                    AlertUtils.showAlert(RecordingActivity.this,"Recording in progress", "Please finish the current recording before exiting.");
                }
                else{
                    setResult(RESULT_BACK);
                    finish();
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        if(((ToggleButton)findViewById(R.id.recordButton)).isChecked()){
            AlertUtils.showAlert(RecordingActivity.this,"Recording in progress", "Please finish the current recording before exiting.");
        }
        else{
            super.onBackPressed();
        }
    }

    public void onRecordClick(View v) {
        if(((ToggleButton)v).isChecked())
        {
            //temporarily disable the button until permission is granted
            ((ToggleButton)findViewById(R.id.recordButton)).setChecked(false);
            requestPermission(this);
        }
        else
        {
            addTrack(location.pauseRecording());
        }
    }

    public void onStopClick(View v) {
        if(recordedTrail != null)
            tryStopRecording();
        else
            Snackbar.make(findViewById(R.id.recording_layout),
                    "Must be recording in order to stop", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
    }

    /**
     * Created by Ryley
     * added for increment 3
     *
     * The only time the menu needs to check for permission is to begin recording a trail
     *
     * Bugfix:
     *  The coordinates and map would not be alerted to the new permissions if they were added
     *  this way, and the fragment would not begin recording properly. Now, the fragments are
     *  notified of the permission update.
     */
    @Override
    public void onPermissionResult(boolean result){
        if(result) {

            location = (Coordinates) getSupportFragmentManager().findFragmentById(R.id.coordinates);

            map = (CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

            //if this is the beginning of a new recording,
            if(recordedTrail == null) {
                //notifies the fragments about the permission update, so that it will track location data
                location.onPermissionResult(true);
                map.onPermissionResult(true);
                map.getMap().setOnMapClickListener(this);
                //map.getMap().setLocationSource(new LocationSource());

                //creates a new empty trail
                recordedTrail = new Trail();
            }
            //Register Reciever to draw path will tracking
            LocationReciever locationReciever = new LocationReciever();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Tracking.LOCATION_FOUND);
            registerReceiver(locationReciever, intentFilter);
            map.startRecording();

            //then tells the coordinate fragment to record
            location.record();
            ((ToggleButton)findViewById(R.id.recordButton)).setChecked(true);
        }
    }

    /*@Override
    public void onLocationChanged(Location location) {
        if(((ToggleButton)findViewById(R.id.recordButton)).isChecked()) {
            map.getMap().addPolyline(new PolylineOptions().add(new LatLng(location.getLatitude(), location.getLongitude())).width(5).color(16753920));
        }
    }*/

    @Override
    public void onMapClick(LatLng latLng) {
        addWaypoint(latLng);
    }

    public void onWaypointAddClick(View v) {
        Coordinates location = (Coordinates) getSupportFragmentManager()
                .findFragmentById(R.id.coordinates);
        addWaypoint(location.getLastLocation());
    }

    private void addWaypoint(LatLng latLng)
    {
        if(recordedTrail != null && latLng != null) {
            waypointLatLng = latLng;
            (new WaypointDialog()).show(getFragmentManager(), "waypoint");
        }
        else {
            Snackbar.make(findViewById(R.id.recording_layout),
                    "Cannot mark waypoints without recording", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public void onDialogPositiveClick(WaypointDialog dialog)
    {
        if(recordedTrail != null) {
            Trail.Waypoint w = new Trail.Waypoint();
            w.setWaypointName(dialog.getWaypointName());
            w.setComment(dialog.getWaypointComment());
            w.setWaypointType(dialog.getWaypointType());
            w.setLatitude(waypointLatLng.latitude);
            w.setLongitude(waypointLatLng.longitude);

            recordedTrail.addWaypoint(w);
            map.makeWaypoint(w, null);

            waypointLatLng = null;
        }
    }

    /**
     * Created by Ryley
     * added for increment 3
     *
     * Make sure the user did not press the button by accident before they stop recording.
     * If they did mean to stop, gets name and description info
     */
    private void tryStopRecording()
    {
        AlertUtils.showConfirm(this,"Finish Recording", "Are you sure you want to finish this trail?",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //if the user wants to stop recording...
                        Coordinates location = (Coordinates) getSupportFragmentManager()
                                .findFragmentById(R.id.coordinates);
                        //get the location data that was recorded
                        addTrack(location.stopRecord());
                        //if they actually did record data...
                        if(!recordedTrail.getTracks().isEmpty()) {

                            //get other GPX file information
                            Intent intent = new Intent(RecordingActivity.this, NewTrailActivity.class);
                            //starts an activity with the NEW TRAIL result code
                            startActivityForResult(intent, NEW_TRAIL_REQUEST_CODE);
                        }
                        else{
                            //otherwise don't bother saving an empty file
                            dialog.dismiss();
                            AlertUtils.showAlert(RecordingActivity.this,
                                    "Empty trail",
                                    "No location data was recorded.\n"
                                            +"Most likely, user has not moved.");
                        }
                        //make sure the menu changes
                        ((ToggleButton)findViewById(R.id.recordButton)).setChecked(false);
                        map.stopRecording();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_TRAIL_REQUEST_CODE) {
            if (resultCode == NewTrailActivity.RESULT_SAVE) {
                //the activity sends the information back through the intent
                String name = data.getStringExtra(NewTrailActivity.EXTRA_TRAIL_NAME);
                String location = data.getStringExtra(NewTrailActivity.EXTRA_TRAIL_LOCATION);
                int difficulty = data.getIntExtra(NewTrailActivity.EXTRA_TRAIL_DIFFICULTY,0);
                String description = data.getStringExtra(NewTrailActivity.EXTRA_TRAIL_DESCRIPTION);
                String notes = data.getStringExtra(NewTrailActivity.EXTRA_TRAIL_NOTES);
                String uuid = data.getStringExtra(NewTrailActivity.EXTRA_TRAIL_ID);
                String uri = data.getStringExtra(NewTrailActivity.EXTRA_TRAIL_URI);
                boolean hasImage = data.getBooleanExtra(NewTrailActivity.EXTRA_TRAIL_HAS_IMAGE, false);

                //make sure there's actually recorded data
                if (recordedTrail != null) {
                    recordedTrail.getMetadata().setName(name);
                    recordedTrail.getMetadata().setLocation(location);
                    recordedTrail.getMetadata().setDifficulty(difficulty);
                    recordedTrail.getMetadata().setDescription(description);
                    recordedTrail.getMetadata().setNotes(notes);
                    recordedTrail.getMetadata().setTrailID(uuid);
                    if(hasImage) {
                        recordedTrail.getMetadata().addImageID(uuid);
                        saveInternal(uri, uuid);
                    }
                    GPXFile.writeGPXFile(recordedTrail, getApplicationContext());
                }
                recordedTrail = null;
            }
        }
    }

    //Save image to internal storage.
    private void saveInternal(String imageUri, String fileName){

        try {
            Uri uri = Uri.parse(imageUri);
            System.out.println("**********************Saving to Internal***************");
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            new ImageFile(this).
                    setFileName(fileName+".jpg").
                            save(bitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTrack(ArrayList<LatLng> data){
        if(recordedTrail != null && data != null && !data.isEmpty()) {
            Trail.Waypoint w;
            Trail.Track t;
            ArrayList<Trail.Waypoint> track = new ArrayList<>();
            for (LatLng loc : data) {
                w = new Trail.Waypoint();
                w.setLatitude(loc.latitude);
                w.setLongitude(loc.longitude);
                track.add(w);
            }
            t = new Trail.Track();
            t.setTrackPoints(track);
            recordedTrail.addTrack(t);

//            w = new Trail.Waypoint();
//            w.setWaypointName("Start");
//            w.setLatitude(data.get(0)[0]);
//            w.setLongitude(data.get(0)[1]);
//            recordedTrail.addWaypoint(w);
//            w = new Trail.Waypoint();
//            w.setWaypointName("End");
//            w.setLatitude(data.get(data.size() - 1)[0]);
//            w.setLongitude(data.get(data.size() - 1)[1]);
//            recordedTrail.addWaypoint(w);
        }
    }

    // Permissions
    /**
     * Created by Ryley
     * added for increment 1
     * Checks permission without creating a popup to grant it
     */
    @Override
    public boolean hasPermission() {
        return (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }
    /**
     * Created by Caleigh, modified by Ryley
     * Added for increment 1
     *
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     *
     * PermissionListener is the class requesting permission, and when permissions
     * are accepted, calls onPermissionGranted(). This was added by Ryley.
     *
     * THIS IS SYNCHRONIZED SO THAT PEOPLE CAN'T ADD THEMSELVES TO THE SET OF LISTENERS
     * JUST BEFORE THE SET GETS EMPTIED
     */
    @Override
    public synchronized boolean requestPermission(LocationPermissionListener listener) {
        if (hasPermission()) {
            //if permission is already enabled, notify the listener
            listener.onPermissionResult(true);
            return true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            //if permission isnt enabled, create the popup and add the listener to the wait list
            mListeners.add(listener);
            return false;
        }
    }
    /**
     * Created by Caleigh, modified by Ryley
     * Added for increment 1
     *
     * Alerts the listeners that permission has been granted. Listeners added by Ryley.
     *
     * THIS IS SYNCHRONIZED SO THAT PEOPLE CAN'T ADD THEMSELVES TO THE SET OF LISTENERS
     * JUST BEFORE THE SET GETS EMPTIED
     */
    @Override
    public synchronized void onRequestPermissionsResult(int requestCode,
                                                        @NonNull String permissions[],
                                                        @NonNull int[] grantResults) {
        boolean permissionResult = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                permissionResult = (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED);

            }
        }
        //alert all the listeners
        for(LocationPermissionListener listener: mListeners)
            listener.onPermissionResult(permissionResult);
        //remove all permission listeners
        mListeners.clear();
    }

    /*@Override
    public void onLocationChanged(Location location) {
        if(((ToggleButton)findViewById(R.id.recordButton)).isChecked()) {
            map.drawPath(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }*/
    // End permissions

    private class LocationReciever extends BroadcastReceiver {

        private ArrayList<LatLng> path;

        @Override
        public void onReceive(Context context, Intent intent) {
            if(((ToggleButton)findViewById(R.id.recordButton)).isChecked()) {
                path = intent.getParcelableArrayListExtra("location");
                map.drawPath(path);
                /*double[] location = intent.getDoubleArrayExtra("location");
                map.drawPath(new LatLng(location[0], location[1]));*/
            }
        }
    }
}
