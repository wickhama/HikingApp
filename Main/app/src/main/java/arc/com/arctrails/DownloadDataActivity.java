package arc.com.arctrails;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class DownloadDataActivity extends AppCompatActivity implements RatingDialog.RatingDialogListener {

    //result codes
    //the user did nothing
    public static final int RESULT_BACK= 0;
    //the user wants to display the trail
    public static final int RESULT_START = 1;

    //extras
    //A tag for the ID of the trail that should be fetched from the database
    public static final String EXTRA_TRAIL_ID = "arc.com.arctrails.trailID";
    //A tag for the file name sent to TrailDataActivity
    public static final String EXTRA_FILE_NAME = "arc.com.arctrails.filename";

    //the name of the file the data is coming from
    private String trailID;
    //the trail information is being displayed from
    private Trail mTrail;
    //Storage Instance to Firebase Image Storage
    private FirebaseStorage storage = FirebaseStorage.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //build the layout
        setContentView(R.layout.activity_download_data);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //add a back button
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
        //build the display
        trailID = getIntent().getStringExtra(EXTRA_TRAIL_ID);

        Database.getDatabase().getTrailMetadata(trailID, new Database.MetadataListener() {
            @Override
            public void onMetadata(Trail.Metadata metadata) {
                if(metadata != null) {
                    mTrail = new Trail();
                    mTrail.setMetadata(metadata);
                    setInfo(mTrail);

                    //DOWNLOADING from Firebase Storage
                    ImageView displayImage = (ImageView) findViewById(R.id.imageView);
                    System.out.println("&&&&&&&&&&&&STARTING&&&&&&");
                    Database.getDatabase().getImageUrl(mTrail, displayImage, DownloadDataActivity.this);
                }
                else
                    AlertUtils.showAlert(DownloadDataActivity.this, "Database Error",
                            "There was a problem accessing this trail. Please try again later.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    setResult(RESULT_BACK);
                                    finish();
                                }
                            });
            }
        });

    }

    private void setInfo(Trail trail)
    {
        TextView nameView = findViewById(R.id.TrailName);
        TextView descriptionView = findViewById(R.id.Description);
        TextView locationView = findViewById(R.id.TrailLocationField);
        TextView difficultyView = findViewById(R.id.Difficulty);
        TextView notesView = findViewById(R.id.Notes);
        TextView ratingView = findViewById(R.id.RatingText);

        if(trail == null || trail.getMetadata() == null){
            AlertUtils.showAlert(this,"Database Error",
                    "There was a problem connecting to the database. Please try again later.",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing and leave
                            setResult(RESULT_BACK);
                            finish();
                        }
                    });
        }

        else {
            nameView.setText(trail.getMetadata().getName());
            locationView.setText("Location: "+trail.getMetadata().getLocation());
            difficultyView.setText("Difficulty: "+
                    getResources().getStringArray(R.array.difficulty_array)[trail.getMetadata().getDifficulty()]);
            descriptionView.setText(trail.getMetadata().getDescription());
            notesView.setText(trail.getMetadata().getNotes());

            if(trail.getMetadata().getNumRatings() > 0){
                long rating = Math.round(trail.getMetadata().getRating());
                String ratingText = "";
                int i = 0;
                for(;i < rating; i++)
                    ratingText += "★";
                for(;i < 5; i++)
                    ratingText += "☆";
                ratingText += " | "+trail.getMetadata().getNumRatings()+" Votes";

                ratingView.setText(ratingText);
            }

            SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
            boolean hasFlagged = wmbPreference.getBoolean("Flag-"+trail.getMetadata().getTrailID(), false);
            boolean hasRated = wmbPreference.getBoolean("Rate-"+trail.getMetadata().getTrailID(), false);

            setFlagHighlight(hasFlagged);
            setRateHighlight(hasRated);

            descriptionView.setMovementMethod(new ScrollingMovementMethod());
            notesView.setMovementMethod(new ScrollingMovementMethod());
        }
    }

    public void onStartPressed(View view)
    {
        Snackbar.make(findViewById(R.id.downloadDataLayout), "Downloading "+mTrail.getMetadata().getName(), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        if(mTrail.getMetadata().getImageIDs().size() > 0)
            Database.getDatabase().downloadTrailImages(mTrail, this);

        Database.getDatabase().getTrail(trailID, new Database.DataTrailListener() {
            @Override
            public void onDataTrail(Trail trail) {
                mTrail = trail;

                GPXFile.writeGPXFile(mTrail,DownloadDataActivity.this);
//                Snackbar.make(findViewById(R.id.downloadDataLayout), "Downloaded "+mTrail.getMetadata().getName(), Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                Intent intent = new Intent();
                intent.putExtra(EXTRA_FILE_NAME,mTrail.getMetadata().getTrailID()+".gpx");
                setResult(RESULT_START,intent);
                finish();
            }
        });
    }

    public void onFlagPressed(View view)
    {
        final SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hasFlagged = wmbPreference.getBoolean("Flag-"+mTrail.getMetadata().getTrailID(), false);

        if(hasFlagged){
            //if they've flagged it already, clicking removes the flag
            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putBoolean("Flag-"+mTrail.getMetadata().getTrailID(), false);
            editor.apply();

            setFlagHighlight(false);

            //send a message to the database
        }
        else{
            //otherwise, clicking asks them if they want to flag
            AlertUtils.showConfirm(this, "Flag this Trail",
                    "Trails can be flagged to notify administrators of inappropriate content. " +
                            "Are you sure you want to flag this trail?",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = wmbPreference.edit();
                            editor.putBoolean("Flag-"+mTrail.getMetadata().getTrailID(), true);
                            editor.apply();

                            setFlagHighlight(true);

                            //send a message to the database
                        }
                    });
        }
    }

    public void onRatePressed(View view)
    {
        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        int rated = wmbPreference.getInt("Rate-"+mTrail.getMetadata().getTrailID(), 0);

        if(rated > 0){
            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putInt("Rate-"+mTrail.getMetadata().getTrailID(), 0);
            editor.apply();

            setRateHighlight(false);
        }
        else{
            (new RatingDialog()).show(getFragmentManager(), "rating");
        }
    }

    @Override
    public void onDialogPositiveClick(RatingDialog dialog) {
        if(dialog.getRating() > 0) {
            SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);

            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putInt("Rate-" + mTrail.getMetadata().getTrailID(), dialog.getRating());
            editor.apply();

            setRateHighlight(true);
        }
    }

    public void setFlagHighlight(boolean highlight)
    {
        FloatingActionButton flag = findViewById(R.id.FlagButton);
        if(highlight) {
            flag.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark,null)));
        }
        else{
            flag.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.cardview_dark_background,null)));
        }
    }

    public void setRateHighlight(boolean highlight)
    {
        FloatingActionButton rate = findViewById(R.id.RateButton);
        if(highlight) {
            rate.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_orange_dark,null)));
        }
        else{
            rate.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.cardview_dark_background,null)));
        }
    }
}
