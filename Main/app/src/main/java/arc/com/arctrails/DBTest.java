package arc.com.arctrails;

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
    private Button addToDB1;
    private Button addToDB2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbtest);

    }

    public void onDB1Click(View v){

        Trail trail1 = new Trail("Trail1", "Lakes and views", "Prince George");

        databaseReference = FirebaseDatabase.getInstance().getReference();


        trailID = trail1.getName();
        databaseReference.child("Trails").child(trailID).setValue(trail1);

        Trail trail2 = new Trail("Trail2", "Tons of Views", "Quesnel");
        trailID = trail2.getName();


        databaseReference.child("Trails").child(trailID).setValue(trail2);

        System.out.println("Worked");


    }
}
