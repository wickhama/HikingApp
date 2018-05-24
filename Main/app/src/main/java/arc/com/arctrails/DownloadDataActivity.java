package arc.com.arctrails;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

public class DownloadDataActivity extends AppCompatActivity {

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
                mTrail = new Trail();
                mTrail.setMetadata(metadata);
                setInfo(mTrail);

                //DOWNLOADING from Firebase Storage
                ImageView displayImage = (ImageView)findViewById(R.id.imageView);
                System.out.println("&&&&&&&&&&&&STARTING&&&&&&");
                Database.getDatabase().getImageUrl(mTrail, displayImage, DownloadDataActivity.this);
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

        if(trail == null){
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
            difficultyView.setText("Difficulty: "+trail.getMetadata().getDifficulty());
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
                intent.putExtra(EXTRA_FILE_NAME,mTrail.getMetadata().getName()+".gpx");
                setResult(RESULT_START,intent);
                finish();
            }
        });
    }

    public void onFlagPressed(View view)
    {
        Snackbar.make(findViewById(R.id.downloadDataLayout), "Flag Pressed", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void onRatePressed(View view)
    {
        Snackbar.make(findViewById(R.id.downloadDataLayout), "Rate Pressed", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}
