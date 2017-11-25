package arc.com.arctrails;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Track;

import java.io.File;

public class TrailDataActivity extends AppCompatActivity {
    public static final int RESULT_BACK= 0;
    public static final int RESULT_START = 1;
    public static final int RESULT_DELETE= 2;

    public static final String EXTRA_FILE_NAME = "arc.com.arctrails.filename";

    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trail_data);

        Toolbar toolbar = findViewById(R.id.toolbar);
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

        fileName = getIntent().getStringExtra(MenuActivity.EXTRA_FILE_NAME);
        setInfo(fileName);
    }

    private void setInfo(String fileName)
    {
        GPX trail = GPXFile.getGPX(fileName,this);

        TextView nameView = findViewById(R.id.TrailName);
        TextView descriptionView = findViewById(R.id.Description);

        if(trail == null){
            final String currentFile = fileName;
            nameView.setText("N/A");
            descriptionView.setText("N/A");

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
                            //do nothing if they dont want to delete
                            setResult(RESULT_BACK);
                            finish();
                        }
                    });

            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.show();
        }
        else {
            //there should only be one track
            Track track = trail.getTracks().iterator().next();
            String name = track.getName();
            String description = track.getDescription();

            nameView.setText(name);
            descriptionView.setText(description);
        }
    }

    public void onStartPressed(View view)
    {
        //tell the main activity to start showing the trail
        Intent intent = new Intent();
        intent.putExtra(EXTRA_FILE_NAME,fileName);
        setResult(RESULT_START,intent);
        finish();
    }

    public void onDeletePressed(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Trail")
                .setMessage("Are you sure you want to delete this trail?");

        builder.setPositiveButton(android.R.string.yes,
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

        builder.setNegativeButton(android.R.string.no,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //do nothing if they dont want to delete
                }
            });

        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();
    }

    private void deleteTrailFile(String fileName){
        File file = new File(getExternalFilesDir(null),fileName);
        try{
            file.delete();
        }catch(SecurityException e){
            showAlert("SecurityException",e.getLocalizedMessage(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
    }

    private void showAlert(String title, String message, DialogInterface.OnClickListener onClick)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, onClick);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();
    }
}
