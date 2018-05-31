package arc.com.arctrails;

import android.content.Intent;
import android.os.Bundle;
import java.util.List;

public class DatabaseFileActivity extends DynamicScrollListActivity
{
    //request result codes
    //ID for DatabaseTrailActivity results
    private static final int DATABASE_TRAIL_CODE = 0;

    private Database trailDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //loads the initial state of the menu
        trailDB = Database.getDatabase();
        trailDB.trailMetaData(new Database.DataListListener() {
            @Override
            public void onDataList(List<Trail.Metadata> entryIDs) {
                buildMenu(entryIDs);
            }
        });
    }

    public String getMenuItemTitle(Trail.Metadata metadata) {
        //Sets the rating in the list.
        String ratingText = "";

        if(metadata.getNumRatings() > 0){
            long rating = Math.round(metadata.getRating());
            int i = 0;
            for(;i < rating; i++)
                ratingText += "★";
            for(;i < 5; i++)
                ratingText += "☆";
        }else{
            ratingText = "☆☆☆☆☆";
        }

        return String.format("%-15s%s",ratingText, metadata.getName());
    }

    @Override
    public boolean onTrailSelected(Trail.Metadata metadata) {
        Intent intent = new Intent(this, DownloadDataActivity.class);
        intent.putExtra(DownloadDataActivity.EXTRA_TRAIL_ID, metadata.getTrailID());
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

    @Override
    public void onDialogPositiveClick(FilterDialog dialog) {
        if(dialog.useDifficulty()) {
            //Inset Query code for the db.
            dialog.getDifficulty();
        }
    }
}
