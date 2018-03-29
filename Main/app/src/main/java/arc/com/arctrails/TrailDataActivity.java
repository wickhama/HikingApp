package arc.com.arctrails;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import java.io.File;

/**
 * @author Ryley
 * @since 18-11-2017 Increment 2
 *
 * This activity displays information about a trail file to the user, and asks for input on
 * what to do with the file. Currently, the user can display the trail, delete the file, or ignore
 * the file and go back
 *
 * Modified in Increment 3:
 *      In increment 2 this activity assumed that each file would consist of a single trail path,
 *      and loaded name and description information from there. Now, with multiple trail paths as
 *      an option, the data had to be moved to the top level of the GPX file, and this activity
 *      had to be modified to read from the new location.
 */
public class TrailDataActivity extends AppCompatActivity {
    //result codes
    //the user did nothing
    public static final int RESULT_BACK= 0;
    //the user wants to display the trail
    public static final int RESULT_START = 1;
    //the user deleted this file
    public static final int RESULT_DELETE= 2;


    //extras
    //A tag for the file name sent to TrailDataActivity
    public static final String EXTRA_FILE_NAME = "arc.com.arctrails.filename";
    //the name of the file the data is coming from
    private String fileName;
    //the trail information is being displayed from
    private Trail trail;

    /**
     * Created by Ryley
     * added for increment 2
     *
     * Called when the activity is created
     * Builds the layout and the toolbar
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //build the layout
        setContentView(R.layout.activity_trail_data);

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
        fileName = getIntent().getStringExtra(EXTRA_FILE_NAME);
        //read the GPX file into an object
        trail = GPXFile.getGPX(fileName,this);
        setInfo(trail);
    }

    /**
     * Created by Ryley, modified by Ayla
     * added for increment 2
     *
     * Displays the name and description of the given file.
     * If the file is corrupt in some way, allow the user to delete it, and prevent them
     * from starting it on the map.
     *
     * Modified to get the name and description from the creator and version respectively
     */
    private void setInfo(Trail trail)
    {
        TextView nameView = findViewById(R.id.TrailName);
        TextView descriptionView = findViewById(R.id.Description);
        TextView locationView = findViewById(R.id.TrailLocationField);
        TextView difficultyView = findViewById(R.id.Difficulty);
        TextView notesView = findViewById(R.id.Notes);

        //if the file could not be parsed, ask them if they would like to delete
        if(trail == null){
            //creates a final string so it can be used in the listeners
            final String currentFile = fileName;
            nameView.setText("N/A");
            descriptionView.setText("N/A");
            //alert the user
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Illegal Trail Format")
                    .setMessage("This file is formatted incorrectly.\n"
                            +"Would you like to delete this trail?");

            builder.setPositiveButton(R.string.delete,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            //delete the file
                            deleteTrailFile(currentFile);
                            //send a delete message back to the parent
                            setResult(RESULT_DELETE);
                            finish();
                        }
                    });

            builder.setNegativeButton(android.R.string.no,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing and leave if they dont want to delete
                            setResult(RESULT_BACK);
                            finish();
                        }
                    });

            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.show();
        }

        /*HERE*/

        else {
            //Info for preset trails are stored in gpx Version and Creator as
            //GPX Parser does not allow access to any other fields

            nameView.setText(trail.getMetadata().getName());
            locationView.setText("Location: "+trail.getMetadata().getLocation());
            difficultyView.setText("Difficulty: "+trail.getMetadata().getDifficulty());
            descriptionView.setText(trail.getMetadata().getDescription());
            notesView.setText(trail.getMetadata().getNotes());

            descriptionView.setMovementMethod(new ScrollingMovementMethod());
            notesView.setMovementMethod(new ScrollingMovementMethod());
        }
    }

    /**
     * Created by Ryley
     * added for Increment 2
     *
     * Called when the start button is pressed. Sends the filename back as a result
     */
    public void onStartPressed(View view)
    {
        //tell the main activity to start showing the trail
        Intent intent = new Intent();
        intent.putExtra(EXTRA_FILE_NAME,fileName);
        setResult(RESULT_START,intent);
        finish();
    }

    /**
     * Created by Ryley
     * added for Increment 2
     *
     * Called when the delete button is pressed. asks the user if they're certain,
     * then deletes the file and returns to the main activity
     */
    public void onDeletePressed(View view)
    {
        AlertUtils.showConfirm(this, "Delete Trail",
            "Are you sure you want to delete this trail?",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    //delete the file
                    deleteTrailFile(fileName);
                    //send a delete message back to the parent
                    setResult(RESULT_DELETE);
                    finish();
                }
            });
    }

    public void onUploadPressed(View view)
    {
        AlertUtils.showConfirm(this, "Upload Trail",
                "Are you sure you want to upload this trail?",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Database database = Database.getDatabase();
                        database.uploadTrail(trail.getMetadata().getName(), trail);
                        Snackbar.make(findViewById(R.id.trail_data_layout), "Uploading Trail", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
    }

    /**
     * Created by Ryley
     * added for Increment 2
     *
     * Tries to delete a file, and alerts the user if it fails
     */
    private void deleteTrailFile(String fileName){
        File file = new File(getExternalFilesDir(null),fileName);
        try{
            file.delete();
        }catch(SecurityException e){
            AlertUtils.showAlert(this, "SecurityException",e.getLocalizedMessage());
        }
    }
}
