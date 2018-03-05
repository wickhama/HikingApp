package arc.com.arctrails;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

        System.out.println("Worked: writing to db.");

    }

    /**
     * This block is retrieving information from the database
     */
    public void onDB2Click(View v){



        System.out.println("Worked: reading frm db.");
    }



//    //TEMP: link to DBDev activity -- Graeme
//    goToTesting = (Button) findViewById(R.id.databaseLink);
//        goToTesting.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            startActivity(new Intent(MenuActivity.this, DBTest.class));
//        }
//    });
}
