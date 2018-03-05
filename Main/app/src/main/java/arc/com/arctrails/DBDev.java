package arc.com.arctrails;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by graememorgan on 2018-02-26.
 *
 * UPDATE Mar 4: This is an unnecessary Class and can be removed.
 */

public class DBDev {




    public void main(){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Message");

        myRef.setValue("Hello, World!");

    }


}// end class
