package arc.com.arctrails;

import android.content.Intent;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class DBTest extends AppCompatActivity {


    //Must insert a db object and a db reference object
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private String trailID;
    private ArrayList<String> trailList = new ArrayList<>();
//    private Button addToDB1;
//    private Button addToDB2;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbtest);

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
        databaseReference.child("DBTest").setValue("Congratulations, the DB is connected.");

    }

//    public ArrayList<String> getTrailList(){
//        System.out.print("From within the getTrailList method: " + trailList.toString());
//        return trailList;
//    }

    /**
     *This is a little sloppy, this is temporary and getter for the DatabaseFileActivity class
     * this should grab the Trail Names from the DB
     */
    public void trailNameRun(final DatabaseListener DBlistener){

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
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

    public void getTrail(final String trailID, final DatabaseListener DBlistener){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
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

    public void uploadTrail(String trailID, Trail trail){
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Trails").child(trailID).setValue(trail);
    }

    /**
     * This instantiates a few trail objects
     */
    public void onDB1Click(View v){

        databaseReference = FirebaseDatabase.getInstance().getReference();
        Trail trail1 = new Trail("Trail1", "Lakes and views", "Prince George");
        trailID = trail1.getName();
        databaseReference.child("Trails").child(trailID).setValue(trail1);

        Trail trail2 = new Trail("Trail2", "Tons of Views", "Quesnel");
        trailID = trail2.getName();
        databaseReference.child("Trails").child(trailID).setValue(trail2);

        Trail trail3 = new Trail("Trail3", "Very nice views", "The Heart");
        trailID = trail3.getName();
        databaseReference.child("Trails").child(trailID).setValue(trail3);

        Trail trail4 = new Trail("Trail4", "Very nice trail 4", "UNBC");
        trailID = trail4.getName();
        databaseReference.child("Trails").child(trailID).setValue(trail4);

        System.out.println("Worked: writing to db.");

    }

    /**
     * This is retrieving information from the database
     */
    public void onDB2Click(View v){

        ValueEventListener trailListener = new ValueEventListener() {
            public static final String TAG = "cancelled";

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Trail trail1 = dataSnapshot.child("Trails").child("Trail1").getValue(Trail.class);
                //Printing the data from Trail1

                System.out.println("Key: " + dataSnapshot.getChildrenCount());
                System.out.println("Key: " + dataSnapshot.getKey());


                if(trail1 != null) {
                    System.out.println("From Db: " + trail1.getName() + " " + trail1.getDescription()
                            + " " + trail1.getLocation());
                }else{
                    System.out.println("Retreived Null Object");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadTrail: onCancelled", databaseError.toException());
            }
        };

        databaseReference.addValueEventListener(trailListener);


        System.out.println("Worked: reading frm db.");
    }

//        ValueEventListener postListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // Get Post object and use the values to update the UI
//                Post post = dataSnapshot.getValue(Post.class);
//                // ...
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                // Getting Post failed, log a message
//                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
//                // ...
//            }
//        };
//        mPostReference.addValueEventListener(postListener);







//    //TEMP: link to DBDev activity -- Graeme
//    goToTesting = (Button) findViewById(R.id.databaseLink);
//        goToTesting.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            startActivity(new Intent(MenuActivity.this, DBTest.class));
//        }
//    });
}
