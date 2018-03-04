package arc.com.arctrails;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.alternativevision.gpx.beans.GPX;

import java.io.File;
import java.util.ArrayList;

public class DatabaseFileActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //return result codes
    //result if the user presses back
    public static final int RESULT_BACK= 0;

    //request result codes
    //ID for DatabaseTrailActivity results
    private static final int DATABASE_TRAIL_CODE = 0;

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

        //loads the initial state of the menu
        buildMenu();

        //Has this activity listen for menu events
        NavigationView navigationView = findViewById(R.id.nav_view_database);
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

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //find the index of the selected file
        int id = item.getItemId();

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == DATABASE_TRAIL_CODE)
        {

        }
    }
}
