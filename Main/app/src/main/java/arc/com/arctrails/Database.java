package arc.com.arctrails;

import android.content.Intent;
import android.nfc.Tag;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.widget.Toast.makeText;

public class Database extends AppCompatActivity {

    /**
     * Created by Graeme, edited by Ryley
     *
     * This class implements the JSON Database with the application
     */

    public interface DataListListener{
        void onDataList(List<String> entryIDs);
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
    private ArrayList<String> trailList = new ArrayList<>();
    private DatabaseReference myRef;


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


    public void trailNameRun(final DataListListener DBlistener){

        DatabaseReference rootRef = myRef;
        DatabaseReference ref = rootRef.child("Trails");

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String trail = ds.getKey();//ds.child("description").getValue(String.class);
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

}
