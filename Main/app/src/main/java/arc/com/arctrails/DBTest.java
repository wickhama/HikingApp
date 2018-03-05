package arc.com.arctrails;

import android.content.Intent;
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

public class DBTest extends AppCompatActivity {


    //Must insert a db object and a db reference object
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private String trailID;
//    private Button addToDB1;
//    private Button addToDB2;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbtest);

        //TEMP: This is to test the db connection
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("DB Test");
        databaseReference.setValue("Congratulations, the DB is connected.");

    }

    /**
     * This block instantiates a few trail objects
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
     * This block is retrieving information from the database
     */
    public void onDB2Click(View v){


        ValueEventListener trailListener = new ValueEventListener() {
            public static final String TAG = "cancelled";

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Trail trail1 = dataSnapshot.child("Trails").child("Trail1").getValue(Trail.class);
                //Printing the data from Trail1

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
