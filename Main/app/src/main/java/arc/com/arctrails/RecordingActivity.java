package arc.com.arctrails;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ToggleButton;

import java.util.HashSet;
import java.util.Set;

public class RecordingActivity extends AppCompatActivity implements LocationRequestListener {

    //Identifies the type of permission requests to identify which ones were granted
    //although we only need need fine location for this specific case
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 99;
    //keeps a track of the listeners waiting for permissions
    private Set<LocationPermissionListener> mListeners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mListeners = new HashSet<>();
    }

    public void onRecordClick(View v) {
        if(((ToggleButton)v).isChecked())
        {

        }
        else
        {
            Snackbar.make(findViewById(R.id.recording_layout), "Pausing not yet implemented", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            ((ToggleButton)v).setChecked(true);
        }
    }

    public void onStopClick(View v) {

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
