package arc.com.arctrails;

import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

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
 *
 * 3rd increment:
 *      Set up the drop-down menu to allow beginning/ending recording
 *      Added event handling when a trail ends
 *          Creates an intent and spawns a NewTrailActivity
 *      added popups for alerting the user to unexpected behaviour
 */
public class MenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                    LocationPermissionListener, LocationRequestListener{

    //Identifies the type of permission requests to identify which ones were granted
    //although we only need need fine location for this specific case
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 99;

    //Identifies message types when the dropdown menu is clicked
    public static final int MENU_START_RECORD = 0;
    public static final int MENU_STOP_RECORD = 1;
    public static final int MENU_CLEAR = 2;

    //identifies the result source when a child activity finishes
    //ID for TrailDataActivity results
    public static final int DATA_REQUEST_CODE = 0;
    //ID for NewTrailActivity results
    public static final int NEW_TRAIL_REQUEST_CODE = 1;

    //A tag for the file name sent to TrailDataActivity
    public static final String EXTRA_FILE_NAME = "arc.com.arctrails.filename";
    //A tag for the preference property recording if the app has been opened before
    //This is used so that assets only get saved to the phone the first time the app is run
    public static final String PREFERENCE_FIRST_RUN = "arc.com.arctrails.firstrun";

    private Set<LocationPermissionListener> mListeners;
    private ArrayList<File> mTrailFiles;
    private boolean isRecording;
    private ArrayList<Double[]> recordedData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isFirstRun = wmbPreference.getBoolean(PREFERENCE_FIRST_RUN, true);
        //if (isFirstRun)
        {
            // Code to run once
            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putBoolean(PREFERENCE_FIRST_RUN, false);
            editor.apply();

            //Adds Files into phone storage - aw
            initAssets.initAssets(this);
        }

        ArrayList<Double[]> list = new ArrayList<>();
        for(int i=0; i<10; i++) {
            Double[] waypoint = {i+3.0, 22.0};
            list.add(waypoint);
        }
        //GPXFile.writeGPXFile("ICanWalk", "This is a test", list, this);

        mListeners = new HashSet<>();
        mTrailFiles = new ArrayList<>();
        isRecording = false;

        setContentView(R.layout.activity_menu);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        buildSideMenu();
    }

    // The following is for the menu
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.clear();

        if(!isRecording)
            menu.add(Menu.NONE,MENU_START_RECORD,Menu.NONE,"Start Recording");
        else
            menu.add(Menu.NONE,MENU_STOP_RECORD,Menu.NONE,"Stop Recording");
        menu.add(Menu.NONE, MENU_CLEAR,Menu.NONE,"Clear Map");
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case MENU_START_RECORD:
                requestPermission(this);
                return true;
            case MENU_STOP_RECORD:
                tryStopRecording();
                return true;
            case MENU_CLEAR:
                CustomMapFragment map = (CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                map.makeTrail(null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * The only time the menu needs to check for permission is to begin recording a trail
     */
    @Override
    public void onPermissionResult(boolean result){
        if(result) {
            Coordinates location;

            location = (Coordinates) getSupportFragmentManager().findFragmentById(R.id.coordinates);
            location.record();

            isRecording = true;
        }
    }

    private void tryStopRecording()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Finish Recording")
                .setMessage("Are you sure you want to finish this trail?");

        builder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Coordinates location = (Coordinates) getSupportFragmentManager()
                                .findFragmentById(R.id.coordinates);
                        //stop recording
                        recordedData = location.stopRecord();
                        if(recordedData.size()>0) {
                            //get other GPX data
                            Intent intent = new Intent(MenuActivity.this, NewTrailActivity.class);
                            startActivityForResult(intent, NEW_TRAIL_REQUEST_CODE);
                        }
                        else{
                            dialog.dismiss();
                            showAlert(
                                    "Empty trail",
                                    "No location data was recorded.\n"
                                    +"Most likely, user has not moved.");
                        }

                        isRecording = false;
                    }
                });

        builder.setNegativeButton(android.R.string.no,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing if they dont want to finish
                    }
                });

        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();
    }

    private void showAlert(String title, String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();
    }

    public void buildSideMenu()
    {
        NavigationView navView = findViewById(R.id.nav_view);
        Menu menu = navView.getMenu();

        menu.clear();
        mTrailFiles.clear();

        File dir = getExternalFilesDir(null);
        if (dir != null) {
            for(File trailFile: dir.listFiles())
            {
                String[] tokens = trailFile.getName().split("\\.");
                if(tokens.length == 2 && tokens[1].equals("gpx")) {
                    int id = mTrailFiles.size();
                    menu.add(R.id.nav_group, id, Menu.NONE, tokens[0]).setCheckable(true);
                    mTrailFiles.add(trailFile);
                }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        File trailFile = mTrailFiles.get(id);
        Intent intent = new Intent(this, TrailDataActivity.class);
        intent.putExtra(EXTRA_FILE_NAME, trailFile.getName());
        startActivityForResult(intent,DATA_REQUEST_CODE);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == NEW_TRAIL_REQUEST_CODE)
        {
            if(resultCode == NewTrailActivity.RESULT_SAVE)
            {
                String name = data.getStringExtra(NewTrailActivity.EXTRA_TRAIL_NAME);
                String description = data.getStringExtra(NewTrailActivity.EXTRA_TRAIL_DESCRIPTION);
                if(recordedData != null)
                    GPXFile.writeGPXFile(name,description,recordedData,getApplicationContext());
                recordedData = null;
                buildSideMenu();
            }
        }
        else if(requestCode == DATA_REQUEST_CODE)
        {
            if(resultCode == TrailDataActivity.RESULT_START)
            {
                String fileName = data.getStringExtra(TrailDataActivity.EXTRA_FILE_NAME);
                GPX trail = GPXFile.getGPX(fileName,this);
                CustomMapFragment map = (CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                map.makeTrail(trail);
            }
            if(resultCode == TrailDataActivity.RESULT_DELETE)
                buildSideMenu();
        }
    }
    // End menu stuff

    // Permissions
    /*
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

     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.

     * PermissionListener is the class requesting permission, and when permissions
     * are accepted, calls onPermissionGranted(). This was added by Ryley.

     * THIS IS SYNCHRONIZED SO THAT PEOPLE CAN'T ADD THEMSELVES TO THE SET OF LISTENERS
     * JUST BEFORE THE SET GETS EMPTIED
     */
    @Override
    public synchronized boolean requestPermission(LocationPermissionListener listener) {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
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

     * Alerts the listeners that permission has been granted. Listeners added by Ryley.

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
        for(LocationPermissionListener listener: mListeners)
            listener.onPermissionResult(permissionResult);
        //remove all permission listeners
        mListeners.clear();
    }
    // End permissions
}
