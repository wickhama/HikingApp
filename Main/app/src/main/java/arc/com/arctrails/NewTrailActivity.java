package arc.com.arctrails;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import java.io.File;

/**
 * @author Ryley
 * @since 19-11-2017 Increment 3
 *
 * This activity gets information from the user for creating a GPX file.
 * Currently only includes a Trail name and a description, but by having this
 * as a separate activity it is easily extendable by adding more text fields
 */
public class NewTrailActivity extends AppCompatActivity {
    //result codes
    //result if the user chooses not to save the file
    public static final int RESULT_BACK= 0;
    //result if the user chooses to save the file
    public static final int RESULT_SAVE= 1;

    //the IDs used in the result Intent to send back data
    public static final String EXTRA_TRAIL_NAME = "arc.com.arctrails.trailname";
    public static final String EXTRA_TRAIL_DESCRIPTION = "arc.com.arctrails.traildescription";

    /**
     * Created by Ryley
     * added for increment 3
     *
     * Called when the activity is created
     * Builds the layout and the toolbar
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_trail);

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
    }

    /**
     * Created by Ryley
     * added for increment 3
     *
     * When the user wants to save, check that their input is valid. if it is, send the information
     * somewhere that it can be used to build a GPX.
     * If it's not, alert them, and wait for more input
     *
     * Future increment:
     * If the filename is an existing trail, allow the user to add their path into the trail?
     */
    public void onSavePressed(View view)
    {
        EditText nameField = findViewById(R.id.TrailNameField);
        EditText descriptionField = findViewById(R.id.TrailDescriptionField);

        //trims whitespace from the user input
        final String name = nameField.getText().toString().trim();
        final String description = descriptionField.getText().toString().trim();

        //file names cannot be empty
        if(name.equals("")) {
            AlertUtils.showAlert(this, "No File Name", "Trails must be given a name before they can be saved.");
            return;
        }
        //file names cannot contain .
        if(name.contains(".")){
            AlertUtils.showAlert(this, "Illegal File Name", "File names cannot contain period (.)");
            return;
        }

        File file = new File(getExternalFilesDir(null), name+".gpx");
        //if the user is about to overwrite a file, ask if they're sure that's what they want
        if(file.exists())
            AlertUtils.showConfirm(this, "Replace Trail",
                "A trail with that name already exists.\n"+
                "Would you like to overwrite the trail?",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //if they're sure they want to overwrite, send the info
                        sendSaveResult(name, description);
                    }
                });
        else
        {
            sendSaveResult(name, description);
        }
    }

    /**
     * Created by Ryley
     * added in Increment 3
     *
     * Wraps the information in an intent and sends the result
     */
    private void sendSaveResult(String name, String description)
    {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_TRAIL_NAME,name);
        intent.putExtra(EXTRA_TRAIL_DESCRIPTION, description);
        setResult(RESULT_SAVE,intent);
        finish();
    }
}
