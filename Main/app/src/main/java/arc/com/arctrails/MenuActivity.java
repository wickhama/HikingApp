package arc.com.arctrails;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

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
        LocationRequestListener, LocationPermissionListener{

    //Identifies the type of permission requests to identify which ones were granted
    //although we only need need fine location for this specific case
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 99;

    //identifies the result source when a child activity finishes
    public static final int LOAD_LOCAL_FILE_CODE = 0;
    public static final int DATABASE_FILE_CODE = 1;
    public static final int RECORD_TRAIL_CODE = 2;

    //A tag for the preference property recording if the app has been opened before
    //This is used so that assets only get saved to the phone the first time the app is run
    public static final String PREFERENCE_FIRST_RUN = "arc.com.arctrails.firstrun";

    //keeps a track of the listeners waiting for permissions
    private Set<LocationPermissionListener> mListeners;

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
//        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
//        boolean isFirstRun = wmbPreference.getBoolean(PREFERENCE_FIRST_RUN, true);
//        if (isFirstRun)
//        {
//            // Code to run once
//            SharedPreferences.Editor editor = wmbPreference.edit();
//            editor.putBoolean(PREFERENCE_FIRST_RUN, false);
//            editor.apply();

//            //Adds Files into phone storage - aw
//            initAssets.initAssets(this);
//        }

        mListeners = new HashSet<>();

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
        Menu menu = navigationView.getMenu();
        //initially there is no map, so remove the options
        menu.findItem(R.id.trail_menu).getSubMenu().setGroupVisible(R.id.map_options,false);

        //create a database instance to improve load times later
        Database.getDatabase();
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
     * added for increment 2
     *
     * When the user selects a file from the side menu,
     * displays the information for that trail in a new activity
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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

            //once the trail is gone, remove the options from the menu
            NavigationView navigationView = findViewById(R.id.nav_view);
            Menu menu = navigationView.getMenu();
            menu.findItem(R.id.trail_menu).getSubMenu().setGroupVisible(R.id.map_options,false);

        } else if (id == R.id.nav_locate) {
            CustomMapFragment map = (CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            map.moveCameraToTrail();

        } else if (id == R.id.nav_record) {
            //allows a user to download files
            Intent intent = new Intent(this, RecordingActivity.class);
            //starts the activity with the LOAD_LOCAL_FILE_CODE result code
            startActivityForResult(intent, RECORD_TRAIL_CODE);

        } else if (id == R.id.nav_info) {
            Intent intent = new Intent(this, InformationActivity.class);
            startActivity(intent);
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
        if(requestCode == LOAD_LOCAL_FILE_CODE)
        {
            //if the trail was started, alert the map
            if(resultCode == TrailDataActivity.RESULT_START)
            {
                //filename sent back through intent
                String fileName = data.getStringExtra(TrailDataActivity.EXTRA_FILE_NAME);
                Trail trail = GPXFile.getGPX(fileName,this);
                //draw the trail
                CustomMapFragment map = (CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                map.makeTrail(trail);
                //enable map options in the menu
                NavigationView navigationView = findViewById(R.id.nav_view);
                Menu menu = navigationView.getMenu();
                menu.findItem(R.id.trail_menu).getSubMenu().setGroupVisible(R.id.map_options,true);
            }
        }
        else if(requestCode == DATABASE_FILE_CODE)
        {
            //when it returns from the database menu, should anything happen?
        }
        else if(requestCode == RECORD_TRAIL_CODE)
        {
            //if the user enabled permissions while in the recording activity
            if(hasPermission())
                requestPermission(this);
        }
    }

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
