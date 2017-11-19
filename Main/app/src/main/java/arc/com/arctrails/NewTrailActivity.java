package arc.com.arctrails;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import java.io.File;

public class NewTrailActivity extends AppCompatActivity {
    public static final int RESULT_BACK= 0;
    public static final int RESULT_SAVE= 1;

    public static final String EXTRA_TRAIL_NAME = "arc.com.arctrails.trailname";
    public static final String EXTRA_TRAIL_DESCRIPTION = "arc.com.arctrails.traildescription";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_trail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_BACK);
                finish();
            }
        });
    }

    public void onSavePressed(View view)
    {
        EditText nameField = findViewById(R.id.TrailNameField);
        EditText descriptionField = findViewById(R.id.TrailDescriptionField);

        final String name = nameField.getText().toString();
        final String description = descriptionField.getText().toString();

        File file = new File(getExternalFilesDir(null), name+".gpx");
        if(file.exists())
            askToReplace(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_TRAIL_NAME,name);
                    intent.putExtra(EXTRA_TRAIL_DESCRIPTION, description);
                    setResult(RESULT_SAVE,intent);
                    finish();
                }
            });
        else
        {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_TRAIL_NAME,name);
            intent.putExtra(EXTRA_TRAIL_DESCRIPTION, description);
            setResult(RESULT_SAVE,intent);
            finish();
        }
    }

    public void askToReplace(DialogInterface.OnClickListener confirmListener)
    {
        if(confirmListener == null)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Replace Trail")
                .setMessage("A trail with that name already exists.\n"+
                            "Would you like to replace the trail?");

        builder.setPositiveButton(android.R.string.yes,confirmListener);

        builder.setNegativeButton(android.R.string.no,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing if they dont want to overwrite
                    }
                });

        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();
    }
}
