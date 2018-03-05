package arc.com.arctrails;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.alternativevision.gpx.beans.GPX;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseFileActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                    InternetRequestListener,
                    DatabaseListener{

    //Identifies the type of permission requests to identify which ones were granted
    //although we only need need fine location for this specific case
    public static final int PERMISSIONS_REQUEST_INTERNET = 100;

    //return result codes
    //result if the user presses back
    public static final int RESULT_BACK= 0;

    //request result codes
    //ID for DatabaseTrailActivity results
    private static final int DATABASE_TRAIL_CODE = 0;

    //keeps a track of the listeners waiting for permissions
    private Set<InternetPermissionListener> mListeners;

    private List<String> mTrailIDs;

    private DBTest trailDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_file);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //when the back button is pressed, return nothing
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_BACK);
                finish();
            }
        });

        //Has this activity listen for menu events
        NavigationView navigationView = findViewById(R.id.nav_view_database);
        navigationView.setNavigationItemSelectedListener(this);

        mListeners = new HashSet<>();
        mTrailIDs = new ArrayList<>();

        //loads the initial state of the menu
        trailDB = new DBTest();
        trailDB.trailNameRun(this);
    }

    @Override
    public void onDataList(List<String> entryIDs) {
        buildMenu(entryIDs);
    }

    /**
     * Created by Ryley
     * added for increment 2
     *
     * Updates the side menu to include all GPX files saved on the device
     */
    public void buildMenu(List<String> names)
    {
        NavigationView navView = findViewById(R.id.nav_view_database);
        Menu menu = navView.getMenu();

        //remove all previous menu options

        menu.clear();
        mTrailIDs.clear();

        System.out.println("Trails list: " + names.toString());


        if(names != null) {
            for(String trailID : names) {
                int id = mTrailIDs.size();
                menu.add(R.id.nav_group_database, id, Menu.NONE, trailID).setCheckable(true);
                mTrailIDs.add(trailID);
            }
        }else{
            Snackbar.make(findViewById(R.id.db_content_view), "Error conecting to DB", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //find the index of the selected file
        int id = item.getItemId();

        Snackbar.make(findViewById(R.id.db_content_view), mTrailIDs.get(id), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == DATABASE_TRAIL_CODE)
        {

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
    public synchronized boolean requestPermission(InternetPermissionListener listener) {
        if (hasPermission()) {
            //if permission is already enabled, notify the listener
            listener.onPermissionResult(true);
            return true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.INTERNET},
                    PERMISSIONS_REQUEST_INTERNET);
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
            case PERMISSIONS_REQUEST_INTERNET: {
                // If request is cancelled, the result arrays are empty.
                permissionResult = (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED);

            }
        }
        //alert all the listeners
        for(InternetPermissionListener listener: mListeners)
            listener.onPermissionResult(permissionResult);
        //remove all permission listeners
        mListeners.clear();
    }
    // End permissions
}
