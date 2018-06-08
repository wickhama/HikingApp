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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
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

    public interface FlagTransactionListener{
        void onComplete(boolean success, long numFlags);
    }

    public interface RatingTransactionListener{
        void onComplete(boolean success, long numRatings, double rating);
    }

    public interface TrailTransactionListener{
        void onComplete(boolean success, Trail trail);
    }

    //a singleton instance of a database
    private static Database singleton;
//    private ArrayList<String> trailList = new ArrayList<>();
    private DatabaseReference myRef;
    // Added for image storage
    private StorageReference storageReference;
    //Storage Reference to Firebase Storage
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef;
    private StorageReference imageRef;
    UploadTask uploadTask;

    private boolean isConnected;


    //For Anonymous Authorization
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public static Database getDatabase(){
        if(singleton == null) {
            singleton = new Database();
        }
        return singleton;
    }

    public Database(){

        myRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //listener that detects connection / disconnection
        FirebaseDatabase.getInstance().getReference(".info/connected").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        isConnected = dataSnapshot.getValue(Boolean.class);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.err.println("Connection Listener was cancelled");
                    }
                }
        );

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

        if(!isConnected) {
            if(DBlistener != null)
                DBlistener.onDataList(null);
            return;
        }

        DatabaseReference rootRef = myRef;
        DatabaseReference ref = rootRef.child("Trails");

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Trail.Metadata> trailList = new ArrayList<>();

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Trail.Metadata trail = ds.child("metadata").getValue(Trail.Metadata.class);//ds.child("description").getValue(String.class);
                    trailList.add(trail);

                    if(ds.hasChild("tracks") && ds.child("tracks").child("0").hasChild("trackPoints")) {
                        trail.setHeadLat(((Number)ds.child("tracks").child("0").child("trackPoints").child("0").child("latitude").getValue()).doubleValue());
                        trail.setHeadLong(((Number)ds.child("tracks").child("0").child("trackPoints").child("0").child("longitude").getValue()).doubleValue());
                    }
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

        if(!isConnected) {
            if(DBlistener != null)
                DBlistener.onDataTrail(null);
            return;
        }

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

        if(!isConnected) {
            if(DBlistener != null)
                DBlistener.onMetadata(null);
            return;
        }

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

    public void addFlag(String trailID, final FlagTransactionListener listener) {

        if(!isConnected) {
            if(listener != null)
                listener.onComplete(false, 0);
            return;
        }

        DatabaseReference metadataRef = myRef.child("Trails").child(trailID).child("metadata").child("numFlags");

        metadataRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                long currentVotes = 0;
                if(mutableData.getValue() != null)
                    currentVotes = (long)mutableData.getValue();

                mutableData.setValue(currentVotes + 1);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean  success, DataSnapshot dataSnapshot) {
                if( listener != null) {
                    if(success)
                        listener.onComplete (success, (long)dataSnapshot.getValue());
                    else
                        listener.onComplete (success, 0);
                }
            }
        });
    }

    public void removeFlag(String trailID, final FlagTransactionListener listener) {
        if(!isConnected) {
            if(listener != null)
                listener.onComplete(false, 0);
            return;
        }

        DatabaseReference metadataRef = myRef.child("Trails").child(trailID).child("metadata").child("numFlags");

        metadataRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                long currentVotes = 0;
                if(mutableData.getValue() != null)
                    currentVotes = (long)mutableData.getValue();

                mutableData.setValue(currentVotes - 1);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean  success, DataSnapshot dataSnapshot) {
                if( listener != null) {
                    if(success)
                        listener.onComplete (success, (long)dataSnapshot.getValue());
                    else
                        listener.onComplete (success, 0);
                }
            }
        });
    }

    public void addRating(String trailID, final int rating, final RatingTransactionListener listener) {
        if(!isConnected) {
            if(listener != null)
                listener.onComplete(false, 0, 0);
            return;
        }

        DatabaseReference metadataRef = myRef.child("Trails").child(trailID).child("metadata");

        metadataRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                long currentVotes = 0;
                double currentRating = 0;
                //System.out.println("@@@@@@@"+mutableData.getValue());
                if(mutableData.getValue() != null) {
                    currentVotes = (long) mutableData.child("numRatings").getValue();
                    currentRating = ((Number)mutableData.child("rating").getValue()).doubleValue();
                }

                double newRating = Math.round(currentRating * currentVotes);
                newRating += rating;
                newRating = newRating / (currentVotes + 1);

                mutableData.child("numRatings").setValue(currentVotes + 1);
                mutableData.child("rating").setValue(newRating);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean  success, DataSnapshot dataSnapshot) {
                if( listener != null) {
                    if (success)
                        listener.onComplete(success,
                                (long) dataSnapshot.child("numRatings").getValue(),
                                ((Number) dataSnapshot.child("rating").getValue()).doubleValue());
                    else
                        listener.onComplete(success,0,0);
                }
            }
        });
    }

    public void removeRating(String trailID, final int rating, final RatingTransactionListener listener) {
        if(!isConnected) {
            if(listener != null)
                listener.onComplete(false, 0, 0);
            return;
        }

        DatabaseReference metadataRef = myRef.child("Trails").child(trailID).child("metadata");

        metadataRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                long currentVotes = 0;
                double currentRating = 0;
                //System.out.println("@@@@@@@"+mutableData.getValue());
                if(mutableData.getValue() != null) {
                    currentVotes = (long) mutableData.child("numRatings").getValue();
                    currentRating = ((Number)mutableData.child("rating").getValue()).doubleValue();
                }

                if(currentVotes > 1) {
                    double newRating = Math.round(currentRating * currentVotes);
                    newRating -= rating;
                    newRating = newRating / (currentVotes - 1);

                    mutableData.child("numRatings").setValue(currentVotes - 1);
                    mutableData.child("rating").setValue(newRating);
                }
                else {
                    mutableData.child("numRatings").setValue(0);
                    mutableData.child("rating").setValue(0.0);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean  success, DataSnapshot dataSnapshot) {
                if( listener != null) {
                    if (success)
                        listener.onComplete(success,
                                (long) dataSnapshot.child("numRatings").getValue(),
                                ((Number) dataSnapshot.child("rating").getValue()).doubleValue());
                    else
                        listener.onComplete(success,0,0);
                }
            }
        });
    }

    public void uploadTrail(String trailID, Trail trail){
        uploadTrail(trailID, trail, null);
    }

    public void uploadTrail(String trailID, Trail trail, final TrailTransactionListener listener){
        if(!isConnected) {
            if(listener != null)
                listener.onComplete(false, null);
            return;
        }

        final Trail newTrail = trail;
        DatabaseReference trailRef = myRef.child("Trails").child(trailID);

        trailRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                System.out.println("mutableData = " + mutableData.getValue());

                if(mutableData.getValue() != null) {
                    Trail.Metadata oldMetadata = mutableData.child("metadata").getValue(Trail.Metadata.class);
                    newTrail.getMetadata().setNumRatings(oldMetadata.getNumRatings());
                    newTrail.getMetadata().setRating(oldMetadata.getRating());
                    newTrail.getMetadata().setNumFlags(oldMetadata.getNumFlags());
                }

                mutableData.setValue(newTrail);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean  success, DataSnapshot dataSnapshot) {
                if( listener != null) {
                    if(success)
                        listener.onComplete (success, dataSnapshot.getValue(Trail.class));
                    else
                        listener.onComplete(success, null);
                }
            }
        });
    }

    public void uploadImage(Uri imageUri, String imageID){
        if(!isConnected) {
            return;
        }

        if (imageUri != null) {
            System.out.println("@@@@@@@@@@@@@"+imageUri);

            //added for UUID
            String path = "images/" + imageID + ".jpg";
            storageRef = storage.getReference();
            imageRef = storageRef.child(path);
            uploadTask = imageRef.putFile(imageUri);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //Toast.makeText(context, "File Upload Failure.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Toast.makeText(context, "File Upload Success.", Toast.LENGTH_LONG).show();

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
    public void getImageUrl(String imageID, final ImageView displayImage, Context context){
        if(!isConnected) {
            return;
        }

        storageRef = storage.getReference();
        imageRef = storageRef.child("images/"+imageID+".jpg");

        Glide.with(context)
                        .using(new FirebaseImageLoader())
                        .load(imageRef)
                        .into(displayImage);

    }

    public void downloadImage(String imageID, Context context){
        if(!isConnected) {
            return;
        }

        File localFile = new File(context.getExternalFilesDir(null), imageID + ".jpg");

        StorageReference islandRef = storageRef.child("images/" + imageID + ".jpg");

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

}// end Database
