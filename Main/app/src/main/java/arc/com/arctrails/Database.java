package arc.com.arctrails;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static android.widget.Toast.makeText;

public class Database extends AppCompatActivity {

    /**
     * Created by Graeme, edited by Ryley
     *
     * This class implements the JSON Database with the application
     */

    public interface DataListListener{
        void onDataList(List<Trail.Metadata> entryIDs);
    }

    public interface DataTrailListener{
        void onDataTrail(Trail trail);
    }

    public interface MetadataListener{
        void onMetadata(Trail.Metadata trail);
    }

    //a singleton instance of a database
    private static Database singleton;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private String trailID;
//    private ArrayList<String> trailList = new ArrayList<>();
    private DatabaseReference myRef;
    // Added for image storage
    private StorageReference storageReference;
    //Storage Reference to Firebase Storage
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef;
    private StorageReference imageRef;
    UploadTask uploadTask;




    //For Anonymous Authorization
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public static Database getDatabase(){
        if(singleton == null)
            singleton = new Database();
        return singleton;
    }

    public Database(){

        myRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();


        /**
         * Created by Graeme
         *
         * This signs-in an anonymous user using the Firebase Authentication.
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


    }


//    public void trailNameRun(final DataListListener DBlistener){
//
//        DatabaseReference rootRef = myRef;
//        DatabaseReference ref = rootRef.child("Trails");
//
//        ValueEventListener eventListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                trailList.clear();
//                for(DataSnapshot ds : dataSnapshot.getChildren()){
//                    String trail = ds.getKey();//ds.child("description").getValue(String.class);
//                    trailList.add(trail);
//                }
//
//                DBlistener.onDataList(trailList);
//                System.out.println("TrailList from ValueEventListener: " + trailList.toString());
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        };
//
//        ref.addListenerForSingleValueEvent(eventListener);
//    }

    //ADDED for new Download ScrollView : Read all Trail Meta Data
    public void trailMetaData(final DataListListener DBlistener){

        DatabaseReference rootRef = myRef;
        DatabaseReference ref = rootRef.child("Trails");

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Trail.Metadata> trailList = new ArrayList<>();

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Trail.Metadata trail = ds.child("metadata").getValue(Trail.Metadata.class);//ds.child("description").getValue(String.class);
                    trailList.add(trail);
                }

                DBlistener.onDataList(trailList);
                System.out.println("TrailList from ValueEventListener: " + trailList.toString());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        ref.addListenerForSingleValueEvent(eventListener);
    }

    public void getTrail(final String trailID, final DataTrailListener DBlistener){
//        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        DatabaseReference rootRef = myRef;
        DatabaseReference ref = rootRef.child("Trails");

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DBlistener.onDataTrail(dataSnapshot.child(trailID).getValue(Trail.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        ref.addListenerForSingleValueEvent(eventListener);
    }

    public void getTrailMetadata(final String trailID, final MetadataListener DBlistener){
//        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        DatabaseReference rootRef = myRef;
        DatabaseReference ref = rootRef.child("Trails");

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Trail.Metadata metadata = dataSnapshot.child(trailID).child("metadata").getValue(Trail.Metadata.class);
                DBlistener.onMetadata(metadata);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        ref.addListenerForSingleValueEvent(eventListener);
    }

    public void uploadTrail(String trailID, Trail trail){
        databaseReference = myRef;
        databaseReference.child("Trails").child(trailID).setValue(trail);
    }

    public void uploadImage(Uri imageUri, Trail trail, final Context context){
        if (imageUri != null) {
            //added for UUID
            String path = "images/" + trail.getMetadata().getTrailID() + ".jpg";
            storageRef = storage.getReference();
            imageRef = storageRef.child(path);

            uploadTask = storageRef.putFile(imageUri);

            imageRef.getName().equals(imageRef.getName());
            imageRef.getPath().equals(imageRef.getPath());

            Uri file = imageUri;

            //Sets path with UUID
            imageRef = storageRef.child(path);

            uploadTask = imageRef.putFile(file);


            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "File Upload Failure.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(context, "File Upload Success.", Toast.LENGTH_LONG).show();

                    //This will be needed when saving to the Trail object
                            /*
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            String url = downloadUrl.toString();
                            */
                }

            });
        }

    }

    //Called from DownloadDataActivity, this returns a working URL from the Trail Image.
    //TODO: fix this so that it can load any image in the image IDs, not just image 0
    public void getImageUrl(Trail trail, final ImageView displayImage, Context context){
        System.out.println(trail.getMetadata().getImageIDs());
        if (trail.getMetadata().getImageIDs() != null
                && trail.getMetadata().getImageIDs().size() > 0) {
            storageRef = storage.getReference();
            imageRef = storageRef.child("images/"+trail.getMetadata().getImageIDs().get(0)+".jpg");

            Glide.with(context)
                            .using(new FirebaseImageLoader())
                            .load(imageRef)
                            .into(displayImage);


        }
    }

    public void downloadTrailImages(Trail trail, Context context){

        for(String imageID : trail.getMetadata().getImageIDs()) {
            File localFile = new File(context.getExternalFilesDir(null), imageID + ".jpg");

            StorageReference islandRef = storageRef.child("images/" + trail.getMetadata().getImageIDs().get(0) + ".jpg");

            islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }
    }

}// end Database
