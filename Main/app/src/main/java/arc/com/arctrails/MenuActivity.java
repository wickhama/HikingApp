package arc.com.arctrails;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.alternativevision.gpx.beans.GPX;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ryley
 * @since 28-10-2017 Increment 1
 * modified: Increment 2, Increment 3
 *
 * This activity acts as the main entry point for the app, and handles initial set-up, menu systems,
 * and permission requests.
 *
 * 1st increment:
 *      Sets up the layout defined in activity_menu.xml.
 *      Acts as a permission handler for map and coordinate fragments
 *      Set up dummy menus that do nothing
 *
 * 2nd increment:
 *      Set up the NavigationMenu to dynamically add menu items based on saved files
 *      Added event handling when a user selects a trail from the menu.
 *          Creates an intent and spawns a TrailDataActivity
 *      Communicates with the map fragment to display GPX trails
 *
 * 3rd increment:
 *      Set up the drop-down menu to allow beginning/ending recording
 *      Added event handling when a trail ends
 *          Creates an intent and spawns a NewTrailActivity
 *      Sends info to GPXFile for saving
 *      added popups for alerting the user to unexpected behaviour
 */
public class MenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                    LocationPermissionListener, LocationRequestListener{

    //Identifies the type of permission requests to identify which ones were granted
    //although we only need need fine location for this specific case
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 99;

    //identifies the result source when a child activity finishes
    //ID for NewTrailActivity results
    public static final int LOAD_LOCAL_FILE_CODE = 0;
    public static final int DATABASE_FILE_CODE = 1;
    public static final int NEW_TRAIL_REQUEST_CODE = 2;

    //A tag for the preference property recording if the app has been opened before
    //This is used so that assets only get saved to the phone the first time the app is run
    public static final String PREFERENCE_FIRST_RUN = "arc.com.arctrails.firstrun";

    //keeps a track of the listeners waiting for permissions
    private Set<LocationPermissionListener> mListeners;

    //flag for whether the user is currently recording. used to change menu behaviour
    private boolean isRecording;
    //the data recorded in the user's most recent trail
    private ArrayList<Double[]> recordedData = null;

    /**
     * Created by Ryley, modified by Ayla, Caleigh
     * Added for increment 1
     *
     * Part of the startup process for activities. acts like a constructor.
     *
     * Increment 1:
     *      Connects this activity to its layout
     *      Connects this activity to the toolbar and side-menu as a listener
     *          listener methods not implemented
     *
     * Increment 2:
     *      Builds the initial state for the side menu
     *      Loads files from the APK onto the file system on startup
     *
     * Increment 3:
     *      Now only loads files on *first* startup, instead of every time
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //checks if this is the first time the app has been run
        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = wmbPreference.getBoolean(PREFERENCE_FIRST_RUN, true);
        if (isFirstRun)
        {
            // Code to run once
            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putBoolean(PREFERENCE_FIRST_RUN, false);
            editor.apply();

            //Adds Files into phone storage - aw
            initAssets.initAssets(this);
        }

        mListeners = new HashSet<>();
        isRecording = false;

        //loads the layout
        setContentView(R.layout.activity_menu);
        //uses the toolbar defined in the layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //connects the side menu to the toolbar
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //Has this activity listen for menu events
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    // The following section is for the menu

    /**
     * Created by Ryley (Auto-Generated)
     * added for increment 1
     *
     * The back button has been replaced with the trail menu, so when they press back
     * it should open or close the menu
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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

            isRecording = true;
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
                    if(recordedData.size()>0) {
                        //get other GPX file information
                        Intent intent = new Intent(MenuActivity.this, NewTrailActivity.class);
                        //starts an activity with the NEW TRAIL result code
                        startActivityForResult(intent, NEW_TRAIL_REQUEST_CODE);
                    }
                    else{
                        //otherwise don't bother saving an empty file
                        dialog.dismiss();
                        AlertUtils.showAlert(MenuActivity.this,
                                "Empty trail",
                                "No location data was recorded.\n"
                                +"Most likely, user has not moved.");
                    }
                    //make sure the menu changes
                    isRecording = false;
                }
            });
    }


    private boolean AWFUL_CODE_TEMP_THING = true;
    /**
     * Created by Ryley
     * added for increment 2
     *
     * When the user selects a file from the side menu,
     * displays the information for that trail in a new activity
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //closes the menu
        int id = item.getItemId();

        if (id == R.id.nav_load) {
            //allows a user to select a file
            Intent intent = new Intent(this, LocalFileActivity.class);
            //starts the activity with the LOAD_LOCAL_FILE_CODE result code
            startActivityForResult(intent, LOAD_LOCAL_FILE_CODE);
        } else if (id == R.id.nav_database) {
            //allows a user to download files
            Intent intent = new Intent(this, DatabaseFileActivity.class);
            //starts the activity with the LOAD_LOCAL_FILE_CODE result code
            startActivityForResult(intent, DATABASE_FILE_CODE);
        } else if (id == R.id.nav_clear) {
            //clear existing trails from the map and center on the current location
            CustomMapFragment map = (CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            map.clearTrail(true);
        } else if (id == R.id.nav_record) {
            if(AWFUL_CODE_TEMP_THING){
                AWFUL_CODE_TEMP_THING = false;
                requestPermission(this);
                item.setIcon(R.drawable.ic_fiber_manual_record_black_24px);
                item.setTitle("Stop Recording");
            }
            else{
                AWFUL_CODE_TEMP_THING = true;
                item.setIcon(R.drawable.ic_add_circle_black_24px);
                item.setTitle("Record a trail");
                tryStopRecording();
            }
        } else if (id == R.id.nav_edit) {

        } else if (id == R.id.nav_settings) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Created by Ryley
     * added for increment 2
     *
     * Responds to return values from other activities
     *
     * Increment 2:
     *      When the trail data activity returns, if the user chose start,
     *      draw the trail on the map. if the user chose delete, update the menu
     * Increment 3:
     *      when the new trail activity returns, use the information along with the
     *      location data to build a GPX file
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == NEW_TRAIL_REQUEST_CODE)
        {
            if(resultCode == NewTrailActivity.RESULT_SAVE)
            {
                //the activity sends the information back through the intent
                String name = data.getStringExtra(NewTrailActivity.EXTRA_TRAIL_NAME);
                String description = data.getStringExtra(NewTrailActivity.EXTRA_TRAIL_DESCRIPTION);
                //make sure there's actually recorded data
                if(recordedData != null)
                    GPXFile.writeGPXFile(name,description,recordedData,getApplicationContext());
                recordedData = null;
            }
        }
        else if(requestCode == LOAD_LOCAL_FILE_CODE)
        {
            //if the trail was started, alert the map
            if(resultCode == TrailDataActivity.RESULT_START)
            {
                //filename sent back through intent
                String fileName = data.getStringExtra(TrailDataActivity.EXTRA_FILE_NAME);
                GPX trail = GPXFile.getGPX(fileName,this);
                //draw the trail
                CustomMapFragment map = (CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                map.makeTrail(trail);
            }
        }
        else if(requestCode == DATABASE_FILE_CODE)
        {

        }
    }
    // End menu stuff

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
