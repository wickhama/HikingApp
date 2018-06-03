package arc.com.arctrails;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

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

    public static final int RESULT_CAPTURE = 2;
    public static final int RESULT_LOAD_IMAGE = 3;
    //result after the image is captured or selected
    public static final int PICTURE_CROP = 4;
    public static final int PICTURE_REMOVE = 5;

    //the IDs used in the result Intent to send back data
    public static final String EXTRA_FILE_NAME = "arc.com.arctrails.filename";
    public static final String EXTRA_TRAIL_NAME = "arc.com.arctrails.trailname";
    public static final String EXTRA_TRAIL_LOCATION = "arc.com.arctrails.traillocation";
    public static final String EXTRA_TRAIL_DESCRIPTION = "arc.com.arctrails.traildescription";
    public static final String EXTRA_TRAIL_NOTES = "arc.com.arctrails.trailnotes";
    public static final String EXTRA_TRAIL_DIFFICULTY = "arc.com.arctrails.traildifficulty";

    //For image uploading
    public static final String EXTRA_TRAIL_ID = "arc.com.arctrails.id";
    public static final String EXTRA_TRAIL_URI = "arc.com.arctrails.imageUri";
    public static final String EXTRA_TRAIL_HAS_IMAGE = "arc.com.arctrails.hasImage";
    private static final int GALLERY_CODE = 3;
    private Uri picUri;

    private Trail mTrail;
    private LinkedList<Bitmap> mImages = new LinkedList<>();
    private int currentImage = -1;

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
                onBackPressed();
            }
        });

        String fileName = getIntent().getStringExtra(EXTRA_FILE_NAME);

        if(fileName == null)
            mTrail = new Trail();
        else {
            mTrail = GPXFile.getGPX(fileName, this);
            updateFields(mTrail.getMetadata());
        }

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
        findViewById(R.id.addCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCamera();
            }
        });
        findViewById(R.id.addGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addGallery();
            }
        });
        findViewById(R.id.removePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePhoto();
            }
        });
    }

    private void updateFields(Trail.Metadata metadata) {
        EditText nameField = findViewById(R.id.TrailNameField);
        EditText locationField = findViewById(R.id.TrailLocationField);
        EditText descriptionField = findViewById(R.id.TrailDescriptionField);
        EditText notesField = findViewById(R.id.TrailNotesField);
        Spinner difficultySpinner = findViewById(R.id.editDifficulty);

        mImages.clear();
        if(!metadata.getImageIDs().isEmpty())
        {
            ImageView displayImage = (ImageView) findViewById(R.id.imageView);
            for(String imageID : metadata.getImageIDs()) {
                Bitmap bitmap = new ImageFile(this).
                        setFileName(imageID + ".jpg").
                        load();
                if(bitmap != null)
                    mImages.add(bitmap);
            }

            if(!mImages.isEmpty()) {
                displayImage.setImageBitmap(mImages.get(0));
                displayImage.setMaxHeight(mImages.get(0).getHeight());
            }
        }

        nameField.setText(metadata.getName());
        locationField.setText(metadata.getLocation());
        descriptionField.setText(metadata.getDescription());
        notesField.setText(metadata.getNotes());
        difficultySpinner.setSelection(metadata.getDifficulty());
    }

    @Override
    public void onBackPressed(){
        AlertUtils.showConfirm(NewTrailActivity.this, "Closing Trail",
                "You will lose all unsaved changes. Continue?",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NewTrailActivity.super.onBackPressed();
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

        //trims whitespace from the user input
        final String name = nameField.getText().toString().trim();

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
                        sendSaveResult();
                    }
                });
        else
        {
            sendSaveResult();
        }
    }

    /**
     * Created by Ryley
     * added in Increment 3
     *
     * Wraps the information in an intent and sends the result
     */
    private void sendSaveResult()
    {
        EditText nameField = findViewById(R.id.TrailNameField);
        EditText locationField = findViewById(R.id.TrailLocationField);
        EditText descriptionField = findViewById(R.id.TrailDescriptionField);
        EditText notesField = findViewById(R.id.TrailNotesField);
        Spinner difficultySpinner = findViewById(R.id.editDifficulty);

        //trims whitespace from the user input
        String name = nameField.getText().toString().trim();
        String location = locationField.getText().toString().trim();
        String description = descriptionField.getText().toString().trim();
        String notes = notesField.getText().toString().trim();
        int difficulty = difficultySpinner.getSelectedItemPosition();

        //only generate a new ID if the trail does not exist
        //otherwise use the old ID and replace the file
        String id;
        if(mTrail == null)
            id = UUID.randomUUID().toString();
        else
            id = mTrail.getMetadata().getTrailID();

        Intent intent = new Intent();
        intent.putExtra(EXTRA_TRAIL_NAME,name);
        intent.putExtra(EXTRA_TRAIL_LOCATION,location);
        intent.putExtra(EXTRA_TRAIL_DESCRIPTION, description);
        intent.putExtra(EXTRA_TRAIL_NOTES, notes);
        intent.putExtra(EXTRA_TRAIL_DIFFICULTY, difficulty);
        intent.putExtra(EXTRA_TRAIL_ID, id);
        if(picUri != null)
            intent.putExtra(EXTRA_TRAIL_URI, picUri.toString());
        intent.putExtra(EXTRA_TRAIL_HAS_IMAGE, picUri != null);
        setResult(RESULT_SAVE,intent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ImageView imageView = findViewById(R.id.imageView);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        if(requestCode == RESULT_LOAD_IMAGE && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap bitmap = null;
            try {
                bitmap = getBitmapFromUri(selectedImage);
            }
            catch (IOException e){
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            imageView.setImageBitmap(bitmap);

            picUri = data.getData();
            performCrop();
        }
        else if(requestCode == PICTURE_CROP && data != null) {
            Bundle extras = data.getExtras();
            Bitmap bitmap = extras.getParcelable("data");

            imageView.setImageBitmap(bitmap);
        }
        else if(requestCode == RESULT_CAPTURE && data != null){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);

            picUri = data.getData();
            performCrop();
        }
        else {

        }

    }

    public void prevImage() {
        //move the imageview to the prev image
    }

    public void nextImage() {
        //move the imageview to the next image
    }

    public void addCamera() {
        Intent intentCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intentCapture, RESULT_CAPTURE);
    }

    public void addGallery() {
        Intent intentGallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intentGallery.setType("image/*");
        startActivityForResult(intentGallery, RESULT_LOAD_IMAGE);
    }

    public void removePhoto() {
        ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(null);
    }

    private void performCrop() {
        Intent intentCrop = new Intent("com.android.camera.action.CROP");
        intentCrop.setDataAndType(picUri, "image/*");
        intentCrop.putExtra("crop", "true");
        intentCrop.putExtra("aspectX", 16);
        intentCrop.putExtra("aspectY", 10);
        intentCrop.putExtra("outputX", 256);
        intentCrop.putExtra("outputY", 160);
        intentCrop.putExtra("return-data", true);
        startActivityForResult(intentCrop, PICTURE_CROP);
    }

    //This method is to assist in getting an image from the gallery
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
}
