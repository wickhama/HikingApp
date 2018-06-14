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
    private int currentImage = -1;


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

                    if(mTrail.getMetadata().getImageIDs().size() > 0) {
                        currentImage = 0;
                        Database.getDatabase().getImageUrl(mTrail.getMetadata().getImageIDs().get(0),
                                displayImage, DownloadDataActivity.this);
                    }
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

        findViewById(R.id.prevImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevImage();
            }
        });
        findViewById(R.id.nextImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextImage();
            }
        });
    }

    public void prevImage() {
        //move the imageview to the prev image
        if(currentImage > 0)
        {
            ImageView displayImage = findViewById(R.id.imageView);
            currentImage = currentImage - 1;
            Database.getDatabase().getImageUrl(mTrail.getMetadata().getImageIDs().get(currentImage),
                    displayImage, DownloadDataActivity.this);
        }
    }

    public void nextImage() {
        //move the imageview to the next image
        if(currentImage + 1 < mTrail.getMetadata().getImageIDs().size())
        {
            ImageView displayImage = findViewById(R.id.imageView);
            currentImage = currentImage + 1;
            Database.getDatabase().getImageUrl(mTrail.getMetadata().getImageIDs().get(currentImage),
                    displayImage, DownloadDataActivity.this);
        }
    }

    private void setInfo(Trail trail)
    {
        TextView nameView = findViewById(R.id.TrailName);
        TextView descriptionView = findViewById(R.id.Description);
        TextView locationView = findViewById(R.id.TrailLocationField);
        TextView difficultyView = findViewById(R.id.Difficulty);
        TextView lengthView = findViewById(R.id.LengthCategory);
        TextView notesView = findViewById(R.id.Notes);
        TextView flagView = findViewById(R.id.FlagText);

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
            lengthView.setText("Length: "+
                    getResources().getStringArray(R.array.length_array)[trail.getMetadata().getLengthCategory()]);
            descriptionView.setText(trail.getMetadata().getDescription());
            notesView.setText(trail.getMetadata().getNotes());
            flagView.setText(""+trail.getMetadata().getNumFlags());

            setRatingText(trail.getMetadata().getNumRatings(), trail.getMetadata().getRating());

            SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
            boolean hasFlagged = wmbPreference.getBoolean("Flag-"+trail.getMetadata().getTrailID(), false);
            int hasRated = wmbPreference.getInt("Rate-"+trail.getMetadata().getTrailID(), 0);

            setFlagHighlight(hasFlagged);
            setRateHighlight(hasRated != 0);

            descriptionView.setMovementMethod(new ScrollingMovementMethod());
            notesView.setMovementMethod(new ScrollingMovementMethod());
        }
    }

    public void setRatingText(int numRatings, double trailRating) {
        TextView ratingView = findViewById(R.id.RatingText);
        if(numRatings > 0){
            long rating = Math.round(trailRating);
            String ratingText = "";
            int i = 0;
            for(;i < rating; i++)
                ratingText += "★";
            for(;i < 5; i++)
                ratingText += "☆";
            ratingText += " | "+numRatings+" Votes";

            ratingView.setText(ratingText);
        }
        else
            ratingView.setText(R.string.null_rating);
    }

    public void onStartPressed(View view)
    {
        Snackbar.make(findViewById(R.id.downloadDataLayout), "Downloading "+mTrail.getMetadata().getName(), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        if(mTrail.getMetadata().getImageIDs().size() > 0)
            for(String imageID: mTrail.getMetadata().getImageIDs())
                Database.getDatabase().downloadImage(imageID, this);

        Database.getDatabase().getTrail(trailID, new Database.DataTrailListener() {
            @Override
            public void onDataTrail(Trail trail) {
                if(trail != null) {
                    mTrail = trail;

                    GPXFile.writeGPXFile(mTrail, DownloadDataActivity.this);
//                Snackbar.make(findViewById(R.id.downloadDataLayout), "Downloaded "+mTrail.getMetadata().getName(), Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_FILE_NAME, mTrail.getMetadata().getTrailID() + ".gpx");
                    setResult(RESULT_START, intent);
                    finish();
                }
                else {
                    Snackbar.make(findViewById(R.id.downloadDataLayout), "Could not connect", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    public void onFlagPressed(View view)
    {
        final SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hasFlagged = wmbPreference.getBoolean("Flag-"+mTrail.getMetadata().getTrailID(), false);

        if(hasFlagged){
            //if they've flagged it already, clicking removes the flag
            //send a message to the database
            Database.getDatabase().removeFlag(mTrail.getMetadata().getTrailID(),
                    new Database.FlagTransactionListener() {
                        @Override
                        public void onComplete(boolean success, long newValue) {
                            if(success) {
                                SharedPreferences.Editor editor = wmbPreference.edit();
                                editor.putBoolean("Flag-"+mTrail.getMetadata().getTrailID(), false);
                                editor.apply();

                                setFlagHighlight(false);
                                ((TextView)findViewById(R.id.FlagText)).setText(""+newValue);
                            }
                            else {
                                AlertUtils.showAlert(DownloadDataActivity.this,
                                        "Database Error",
                                        "There was a problem connecting to the database. Please try again later");
                            }
                        }
                    });
        }
        else{
            //otherwise, clicking asks them if they want to flag
            AlertUtils.showConfirm(this, "Flag this Trail",
                    "Trails can be flagged to notify administrators of inappropriate content. " +
                            "Are you sure you want to flag this trail?",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //send a message to the database
                            Database.getDatabase().addFlag(mTrail.getMetadata().getTrailID(),
                                    new Database.FlagTransactionListener() {
                                        @Override
                                        public void onComplete(boolean success, long newValue) {
                                            if(success) {
                                                SharedPreferences.Editor editor = wmbPreference.edit();
                                                editor.putBoolean("Flag-"+mTrail.getMetadata().getTrailID(), true);
                                                editor.apply();

                                                setFlagHighlight(true);
                                                ((TextView)findViewById(R.id.FlagText)).setText(""+newValue);
                                            }
                                            else {
                                                AlertUtils.showAlert(DownloadDataActivity.this,
                                                        "Database Error",
                                                        "There was a problem connecting to the database. Please try again later");
                                            }
                                        }
                                    });
                        }
                    });
        }
    }

    public void onRatePressed(View view)
    {
        final SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        final int rated = wmbPreference.getInt("Rate-"+mTrail.getMetadata().getTrailID(), 0);

        if(rated > 0){
            Database.getDatabase().removeRating(mTrail.getMetadata().getTrailID(), rated,
                    new Database.RatingTransactionListener() {
                        @Override
                        public void onComplete(boolean success, long numRatings, double rating) {
                            if(success) {
                                SharedPreferences.Editor editor = wmbPreference.edit();
                                editor.putInt("Rate-"+mTrail.getMetadata().getTrailID(), 0);
                                editor.apply();

                                setRatingText((int)numRatings, rating);
                                setRateHighlight(false);
                            }
                            else {
                                AlertUtils.showAlert(DownloadDataActivity.this,
                                        "Database Error",
                                        "There was a problem connecting to the database. Please try again later");
                            }
                        }
                    });
        }
        else{
            (new RatingDialog()).show(getFragmentManager(), "rating");
        }
    }

    @Override
    public void onDialogPositiveClick(RatingDialog dialog) {
        final SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        final int newRating = dialog.getRating();

        if(newRating > 0) {
            Database.getDatabase().addRating(mTrail.getMetadata().getTrailID(), newRating,
                    new Database.RatingTransactionListener() {
                        @Override
                        public void onComplete(boolean success, long numRatings, double rating) {
                            if(success) {
                                SharedPreferences.Editor editor = wmbPreference.edit();
                                editor.putInt("Rate-" + mTrail.getMetadata().getTrailID(), newRating);
                                editor.apply();

                                setRatingText((int)numRatings, rating);
                                setRateHighlight(true);
                            }
                            else {
                                AlertUtils.showAlert(DownloadDataActivity.this,
                                        "Database Error",
                                        "There was a problem connecting to the database. Please try again later");
                            }
                        }
                    });
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
