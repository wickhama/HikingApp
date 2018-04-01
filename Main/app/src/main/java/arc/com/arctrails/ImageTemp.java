package arc.com.arctrails;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.UUID;

public class ImageTemp extends AppCompatActivity {

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef;
    private StorageReference folderRef;
    private StorageReference imageRef;
    private ImageButton addImage;
    private Button uploadImage;
    private ProgressDialog mProgress;
    private static final int GALLERY_CODE = 1;
    private Uri imageUri;
    UploadTask uploadTask;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;


    private ImageButton imageContainer;
    private ProgressBar progressBar;
    private TextView downloadUrl;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_temp);

        //this next line might be wrong
        imageContainer = findViewById(R.id.imageButtonTest);
        uploadImage = (Button) findViewById(R.id.uploadImageTest);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();








        /**
         * Anonymous Auth Again -- this will not be needed later
         */

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            System.out.println("***Anonymous User Authentication successful.");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            System.out.println("***Anonymous User Authentication failed.");
                        }

                        // ...
                    }
                });




        uploadImage = (Button) findViewById(R.id.uploadImageTest);
        addImage = (ImageButton) findViewById(R.id.imageButtonTest);

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_CODE);
            }
        });


            //THIS is causing a crash when no image to upload.
            uploadImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(imageUri != null) {
                        //added for UUID
                        String path = "images/" + UUID.randomUUID() + ".jpg";
                        storageRef = storage.getReference();
                        imageRef = storageRef.child(path);

                        uploadTask = storageRef.putFile(imageUri);

                        imageRef.getName().equals(imageRef.getName());
                        imageRef.getPath().equals(imageRef.getPath());

                        Uri file = imageUri;
                        //Sets path with UUID
                        imageRef = storageRef.child(path);

                        System.out.println("********URI*********" + file);

                        uploadTask = imageRef.putFile(file);


                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "File Upload Failure.", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Toast.makeText(getApplicationContext(), "File Upload Success.", Toast.LENGTH_LONG).show();

                                //This will be needed when saving to the Trail object
                            /*
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            String url = downloadUrl.toString();
                            */
                            }
                        });
                    }
                }
            });


        /**
         * Picasso library is an add-on for Firebase and it allows the imageview to display images
         * from the Database.
         */



        //Hardcoded to get things working.
        String url = "https://firebasestorage.googleapis.com/v0/b/arctrails-b1a84.appspot.com/o/" +
                "images%2F0e9f9b66-7088-4294-9be7-00eb4026c5cb.jpg?alt=media&token=2de1ec5e-088d-" +
                "4db8-8ba7-760193083325";

        StorageReference displayRef = storage.getReferenceFromUrl(url);
//        StorageReference displayRefImage = storageRef.
        ImageView displayImage = (ImageView) findViewById(R.id.photoHolder);

        Glide.with(ImageTemp.this)
                .using(new FirebaseImageLoader())
                .load(displayRef)
                .into(displayImage);



    }// end onCreate


    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_CODE && resultCode == RESULT_OK){
            imageUri = data.getData();
            addImage.setImageURI(imageUri);
        }

    }

}// end class

