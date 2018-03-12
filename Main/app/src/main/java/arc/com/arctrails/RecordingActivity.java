package arc.com.arctrails;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class RecordingActivity extends AppCompatActivity
        implements LocationRequestListener, LocationPermissionListener{

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
    private ArrayList<Location> recordedData = null;

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
            Snackbar.make(findViewById(R.id.recording_layout), "Pausing not yet implemented", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            ((ToggleButton)v).setChecked(true);
        }
    }

    public void onStopClick(View v) {
        tryStopRecording();
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
            Coordinates location;
            location = (Coordinates) getSupportFragmentManager().findFragmentById(R.id.coordinates);
            CustomMapFragment map;
            map = (CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

            //notifies the fragments about the permission update, so that it will track location data
            location.onPermissionResult(true);
            map.onPermissionResult(true);
            //then tells the coordinate fragment to record
            location.record();

            ((ToggleButton)findViewById(R.id.recordButton)).setChecked(true);
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
                        recordedData = location.stopRecord();
                        //if they actually did record data...
                        if(recordedData.size() > 1) {
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
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_TRAIL_REQUEST_CODE) {
            if (resultCode == NewTrailActivity.RESULT_SAVE) {
                //the activity sends the information back through the intent
                String name = data.getStringExtra(NewTrailActivity.EXTRA_TRAIL_NAME);
                String description = data.getStringExtra(NewTrailActivity.EXTRA_TRAIL_DESCRIPTION);
                //make sure there's actually recorded data
                if (recordedData != null) {
                    Trail trail = buildTrailFromData(name, description, recordedData);
                    GPXFile.writeGPXFile(trail, getApplicationContext());
                }
                recordedData = null;
            }
        }
    }

    private Trail buildTrailFromData(String name, String description, ArrayList<Location> data){
        Trail trail = new Trail();
        trail.setName(name);
        trail.setDescription(description);

        Trail.Waypoint w;
        Trail.Track t;
        ArrayList<Trail.Track> tracks = new ArrayList<>();
        ArrayList<Trail.Waypoint> track = new ArrayList<>();
        for(Location loc : data)
        {
            w = new Trail.Waypoint();
            w.setLatitude(loc.getLatitude());
            w.setLongitude(loc.getLongitude());
            track.add(w);
        }
        t = new Trail.Track();
        t.setTrackPoints(track);
        tracks.add(t);

        ArrayList<Trail.Waypoint> waypoints = new ArrayList<>();
        w = new Trail.Waypoint();
        w.setWaypointName("Start");
        w.setLatitude(data.get(0).getLatitude());
        w.setLongitude(data.get(0).getLongitude());
        waypoints.add(w);
        w = new Trail.Waypoint();
        w.setWaypointName("End");
        w.setLatitude(data.get(data.size()-1).getLatitude());
        w.setLongitude(data.get(data.size()-1).getLongitude());
        waypoints.add(w);

        trail.setName(name);
        trail.setDescription(description);
        trail.setTracks(tracks);
        trail.setWaypoints(waypoints);

        return trail;
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
    // End permissions
}
