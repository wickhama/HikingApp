package arc.com.arctrails;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;

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
//        trailDB.trailNameRun(new Database.DataListListener() {
        trailDB.trailMetaData(new Database.DataListListener() {
            @Override
            public void onDataList(List<Trail.Metadata> entryIDs) {
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
    public void buildMenu(List<Trail.Metadata> metadataList)
    {
        NavigationView navView = findViewById(R.id.nav_view_database);
        Menu menu = navView.getMenu();

        //remove all previous menu options

        menu.clear();
        mTrailIDs.clear();

//        System.out.println("Trails list: " + names.toString());


        if(metadataList != null) {
            int i=0;
            for(Trail.Metadata metadata : metadataList) {
                int id = mTrailIDs.size();

                Drawable d = getDrawable(R.drawable.circle);
                d.mutate();

                int color = RED;

                if(i%3==0){
                    color = RED;
                }
                else if(i%3==1){
                    color = Color.YELLOW;
                }
                else if(i%3==2){
                    color = Color.GREEN;
                }

                d.setColorFilter(color, PorterDuff.Mode.SRC_ATOP );

                i++;

                String ratingText = "";

                if(metadata.getNumRatings() > 0){
                    long rating = Math.round(metadata.getRating());
                    int j = 0;
                    for(;j < rating; j++)
                        ratingText += "★";
                    for(;j < 5; j++)
                        ratingText += "☆";
                }else{
                    ratingText = "☆☆☆☆☆";
                }

                menu.add(R.id.nav_group_database, id, Menu.NONE, String.format("%-15s%s",ratingText, metadata.getName())).setCheckable(true).setIcon(d);
                mTrailIDs.add(metadata.getTrailID());
            }
        }else{
            Snackbar.make(findViewById(R.id.db_content_view), "Error connecting to DB", Snackbar.LENGTH_LONG)
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
