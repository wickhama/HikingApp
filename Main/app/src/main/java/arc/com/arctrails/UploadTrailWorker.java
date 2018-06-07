package arc.com.arctrails;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.net.URI;
import java.util.Map;

import androidx.work.Data;
import androidx.work.Worker;

public class UploadTrailWorker extends Worker {

    public UploadTrailWorker() {}

    @Override
    public Worker.WorkerResult doWork() {
        uploadToDatabase(getInputData());
        return WorkerResult.RETRY;
    }

    private void uploadToDatabase(Data data) {
        Database database = Database.getDatabase();
        String fileName = ((String) data.getKeyValueMap().get("Trail"))+".gpx";
        Trail trail = GPXFile.getGPX(fileName, this.getApplicationContext());
        database.uploadTrail(trail.getMetadata().getTrailID(), trail);

        //aside from the trail ID, the data map should contain parsable Uri Strings for locally stored images
        for(String imageID: trail.getMetadata().getImageIDs()) {
            if(data.getKeyValueMap().get(imageID) != null)
                database.uploadImage(Uri.parse((String)data.getKeyValueMap().get(imageID)), imageID);
        }

        /*if(trail.getMetadata().getImageIDs().size() > 0){
            File uploadFile = new File(context.getExternalFilesDir(null), trail.getMetadata().getImageIDs().get(0)+".jpg");
            database.uploadImage(Uri.parse(uploadFile.toURI().toString()), trail, (Context) data.getKeyValueMap().get("Context"));

        }*/
    }
}
