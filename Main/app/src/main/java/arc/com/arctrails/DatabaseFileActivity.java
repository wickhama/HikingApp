package arc.com.arctrails;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class DatabaseFileActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    //return result codes
    //result if the user presses back
    public static final int RESULT_BACK= 0;

    //request result codes
    //ID for DatabaseTrailActivity results
    private static final int DATABASE_TRAIL_CODE = 0;

    private List<String> mTrailIDs;

    private Database trailDB;

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

        mTrailIDs = new ArrayList<>();

        //loads the initial state of the menu
        trailDB = Database.getDatabase();
        trailDB.trailNameRun(new Database.DataListListener() {
            @Override
            public void onDataList(List<String> entryIDs) {
                buildMenu(entryIDs);
            }
        });
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

        Intent intent = new Intent(this, DownloadDataActivity.class);
        intent.putExtra(DownloadDataActivity.EXTRA_TRAIL_ID, mTrailIDs.get(id));
        startActivityForResult(intent,DATABASE_TRAIL_CODE);

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == DATABASE_TRAIL_CODE)
        {
            //if the trail was started, alert the map
            if(resultCode == DownloadDataActivity.RESULT_START)
            {
                setResult(DownloadDataActivity.RESULT_START,data);
                finish();
            }
        }
    }
}
