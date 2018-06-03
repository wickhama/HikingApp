package arc.com.arctrails;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class EditFileActivity extends LocalFileActivity {

    public static final int EDIT_DATA_CODE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onTrailSelected(Trail.Metadata metadata) {
        Intent intent = new Intent(this, RecordingActivity.class);
        //tell the activity which file to use. sending the file name as an extra is preferable
        //to sending the file itself as the file would have to be serialized and deserialized
        //in the other activity, which is an expensive process
        intent.putExtra(RecordingActivity.EXTRA_FILE_NAME, metadata.getTrailID()+".gpx");
        //starts the activity with the DATA_REQUEST result code
        startActivityForResult(intent,EDIT_DATA_CODE);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == EDIT_DATA_CODE)
        {
            //?????
        }
    }
}
