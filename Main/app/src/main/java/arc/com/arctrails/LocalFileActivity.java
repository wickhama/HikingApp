package arc.com.arctrails;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.alternativevision.gpx.beans.GPX;

import java.io.File;
import java.util.ArrayList;

public class LocalFileActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    //return result codes
    //result if the user presses back
    public static final int RESULT_BACK= 0;

    //request result codes
    //ID for TrailDataActivity results
    public static final int DATA_REQUEST_CODE = 2;

    //a list of files currently displayed in the menu
    private ArrayList<File> mTrailFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_file);
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

        mTrailFiles = new ArrayList<>();
        //loads the initial state of the menu
        buildMenu();

        //Has this activity listen for menu events
        NavigationView navigationView = findViewById(R.id.nav_view_local);
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * Created by Ryley
     * added for increment 2
     *
     * Updates the side menu to include all GPX files saved on the device
     */
    public void buildMenu()
    {
        NavigationView navView = findViewById(R.id.nav_view_local);
        Menu menu = navView.getMenu();

        //remove all previous menu options
        menu.clear();
        //empty the list of files
        mTrailFiles.clear();
        //get the app's directory on the phone
        File dir = getExternalFilesDir(null);
        if (dir != null) {
            for(File trailFile: dir.listFiles())
            {
                //separate the file name from the file extension
                String[] tokens = trailFile.getName().split("\\.");
                //only display GPX files in the menu
                if(tokens.length >= 2 && tokens[tokens.length-1].equals("gpx")) {
                    //when a user clicks a file, we only get an int ID in the callback
                    //so we add the file to an array, and use the index as the ID
                    int id = mTrailFiles.size();
                    menu.add(R.id.nav_group_local, id, Menu.NONE, tokens[0]).setCheckable(true);
                    mTrailFiles.add(trailFile);
                }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //find the index of the selected file
        int id = item.getItemId();

        File trailFile = mTrailFiles.get(id);
        Intent intent = new Intent(this, TrailDataActivity.class);
        //tell the activity which file to use. sending the file name as an extra is preferable
        //to sending the file itself as the file would have to be serialized and deserialized
        //in the other activity, which is an expensive process
        intent.putExtra(TrailDataActivity.EXTRA_FILE_NAME, trailFile.getName());
        //starts the activity with the DATA_REQUEST result code
        startActivityForResult(intent,DATA_REQUEST_CODE);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == DATA_REQUEST_CODE)
        {
            //if the trail was started, alert the map
            if(resultCode == TrailDataActivity.RESULT_START)
            {
                setResult(TrailDataActivity.RESULT_START,data);
                finish();
            }
            //if a file was deleted, repopulate the menu
            if(resultCode == TrailDataActivity.RESULT_DELETE)
                buildMenu();
        }
    }
}
