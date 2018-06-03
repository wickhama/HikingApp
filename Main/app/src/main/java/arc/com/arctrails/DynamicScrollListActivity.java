package arc.com.arctrails;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class DynamicScrollListActivity
        extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        FilterDialog.FilterDialogListener, LocationRequestListener
{
    //Identifies the type of permission requests to identify which ones were granted
    //although we only need need fine location for this specific case
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 99;
    //keeps a track of the listeners waiting for permissions
    private Set<LocationPermissionListener> mListeners;
    //Object to manage location tracking
    private Coordinates location;

    //return result codes
    //result if the user presses back
    public static final int RESULT_BACK= 0;

    private Drawable easiest;
    private Drawable easy;
    private Drawable medium;
    private Drawable hard;
    private Drawable hardest;

    private List<Trail.Metadata> metaList;
    private List<Trail.Metadata> mTrailMetadata;
    private FilterDialog filterDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_scroll_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mListeners = new HashSet<>();

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

        location = (Coordinates) getSupportFragmentManager().findFragmentById(R.id.location);
        metaList = new ArrayList<>();
        mTrailMetadata = new ArrayList<>();

        //Has this activity listen for menu events
        NavigationView navigationView = findViewById(R.id.nav_view_scroll_list);
        navigationView.setNavigationItemSelectedListener(this);

        filterDialog = new FilterDialog();
        //have this activity respond to the filter button
        findViewById(R.id.filter_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                filterDialog.show(getFragmentManager(), "filter");
            }
        });
    }

    private void initDrawables() {
        easiest  = getDrawable(R.drawable.circle_outline);
        easy     = getDrawable(R.drawable.circle_solid);
        medium   = getDrawable(R.drawable.square);
        hard     = getDrawable(R.drawable.single_black_diamond);
        hardest  = getDrawable(R.drawable.double_black_diamond);
    }

    public String getMenuItemTitle(Trail.Metadata metadata) {
        return metadata.getName();
    }

    public Drawable getMenuItemIcon(Trail.Metadata metadata) {
        //first time the icons need to be drawn, create the icons
        if(easiest == null)
            initDrawables();

        //Sets the Difficulty in the list.
        Drawable icon;

        switch(metadata.getDifficulty()){
            case 0 : icon = easiest;
                break;
            case 1 : icon = easy;
                break;
            case 2 : icon = medium;
                break;
            case 3 : icon = hard;
                break;
            case 4 : icon = hardest;
                break;
            default : icon = null;
                break;
        }

        return icon;
    }

    public void setMetadataList(List<Trail.Metadata> metadataList) {
        metaList = metadataList;
        buildMenu(metaList);
    }

    public void buildMenu(List<Trail.Metadata> metadataList)
    {
        NavigationView navView = findViewById(R.id.nav_view_scroll_list);
        Menu menu = navView.getMenu();

        if(metadataList != null) {
            //remove all previous menu options
            menu.clear();
            mTrailMetadata.clear();

            for(Trail.Metadata metadata : metadataList) {
                int id = mTrailMetadata.size();

                menu.add(R.id.nav_group_database, id, Menu.NONE, getMenuItemTitle(metadata))
                        .setCheckable(true).setIcon(getMenuItemIcon(metadata));

                //copy the trails added to the menu into the global list
                mTrailMetadata.add(metadata);
            }
        }else{
            Snackbar.make(findViewById(R.id.list_content_view), "Error Creating Menu", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //find the index of the selected file
        int index = item.getItemId();

        return onTrailSelected(mTrailMetadata.get(index));
    }

    public abstract boolean onTrailSelected(Trail.Metadata metadata);

    @Override
    public void onDialogPositiveClick(FilterDialog dialog) {
        if(dialog.useDifficulty()) {
            //Inset Query code for the db.
            dialog.getDifficulty();
        }

        List<Trail.Metadata> filteredList = new ArrayList<>();
        for(Trail.Metadata m : metaList){
            if((!dialog.useDifficulty() || m.getDifficulty() == dialog.getDifficulty()) &&
                    (!dialog.useRating() || m.getRating() == dialog.getRating())
            //(!dialog.useDistance() || matchesCategory())
            ){
                filteredList.add(m);
            }
        }

        buildMenu(filteredList);
    }

    /**
     * This additional method compares the location of the user against the distance of trail heads
     * in the database. This is calculated 'as a crow flies' and is not 100% accurate.
     */

    public boolean matchesCategory(int myX, int myY, int trailX, int trailY, int categories){

        boolean accept = false;

        //There may be a way to do this using Google libraries?
        double dist = Math.sqrt( ((myX - trailX)^2) + ((myY - trailY)^2) );

        int distanceCategories = 4;

        switch(distanceCategories){

            case 1 : accept = dist < 1;
            break;

            case 2 : accept = dist > 1 && dist < 5;
            break;

            case 3 : accept = dist > 5 && dist < 10;
            break;

            case 4 : accept = dist > 10;
            break;

            default: accept = false;
                break;
        }

        return accept;
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
}





























