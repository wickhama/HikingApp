package arc.com.arctrails;

import android.content.Context;
import android.net.Uri;

import java.io.File;

import androidx.work.Data;
import androidx.work.Worker;

class UploadTrailWorker extends Worker {

    @Override
    public Worker.WorkerResult doWork() {
        uploadToDatabase(getInputData());
        return WorkerResult.RETRY;
    }

    private void uploadToDatabase(Data data) {
        Database database = Database.getDatabase();
        Trail trail = (Trail) data.getKeyValueMap().get("Trail");
        Context context = (Context) data.getKeyValueMap().get("Context");
        database.uploadTrail(trail.getMetadata().getTrailID(), trail);

        if(trail.getMetadata().getImageIDs().size() > 0){
            File uploadFile = new File(context.getExternalFilesDir(null), trail.getMetadata().getImageIDs().get(0)+".jpg");
            database.uploadImage(Uri.parse(uploadFile.toURI().toString()), trail, (Context) data.getKeyValueMap().get("Context"));

        }
    }
}
