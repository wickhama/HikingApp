package arc.com.arctrails;

import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import java.util.List;

public class LocalFileActivity extends DynamicScrollListActivity
{
    //request result codes
    //ID for TrailDataActivity results
    public static final int DATA_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //loads the initial state of the menu
        buildMenu(getMetadataList());
    }

    public List<Trail.Metadata> getMetadataList()
    {
        List<Trail.Metadata> metadataList = new ArrayList<>();

        //get the app's directory on the phone
        File dir = getExternalFilesDir(null);
        if (dir != null) {
            for(File trailFile: dir.listFiles())
            {
                //separate the file name from the file extension
                String[] tokens = trailFile.getName().split("\\.");
                //only display GPX files in the menu
                if(tokens.length >= 2 && tokens[tokens.length-1].equals("gpx")) {

                    Trail trail = GPXFile.getGPX(trailFile.getName(), this);

                    metadataList.add(trail.getMetadata());
                }
            }
        }
        return metadataList;
    }

    @Override
    public boolean onTrailSelected(Trail.Metadata metadata) {
        Intent intent = new Intent(this, TrailDataActivity.class);
        //tell the activity which file to use. sending the file name as an extra is preferable
        //to sending the file itself as the file would have to be serialized and deserialized
        //in the other activity, which is an expensive process
        intent.putExtra(TrailDataActivity.EXTRA_FILE_NAME, metadata.getTrailID()+".gpx");
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
                buildMenu(getMetadataList());
        }
    }

    @Override
    public void onDialogPositiveClick(FilterDialog dialog) {
        if(dialog.useDifficulty()) {
            //Inset Query code for the db.
            dialog.getDifficulty();
        }
    }
}
