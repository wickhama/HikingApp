package arc.com.arctrails;

import android.content.Intent;
import android.graphics.drawable.Drawable;
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

public abstract class DynamicScrollListActivity
        extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FilterDialog.FilterDialogListener
{
    //return result codes
    //result if the user presses back
    public static final int RESULT_BACK= 0;

    private Drawable easiest;
    private Drawable easy;
    private Drawable medium;
    private Drawable hard;
    private Drawable hardest;

    private List<Trail.Metadata> mTrailMetadata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_scroll_list);
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

        mTrailMetadata = new ArrayList<>();

        //Has this activity listen for menu events
        NavigationView navigationView = findViewById(R.id.nav_view_scroll_list);
        navigationView.setNavigationItemSelectedListener(this);

        //have this activity respond to the filter button
        findViewById(R.id.filter_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                (new FilterDialog()).show(getFragmentManager(), "filter");
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
}





























