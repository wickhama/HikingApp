package arc.com.arctrails;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by graememorgan on 2018-04-01.
 */


/**
 * TO SAVE:
 * new ImageSaver(context).
 setFileName("myImage.png").
 setDirectoryName("images").
 save(bitmap);
 */

/**
 * TO LOAD:
 * Bitmap bitmap = new ImageSaver(context).
 setFileName("myImage.png").
 setDirectoryName("images").
 load();
 */

/**
 * Created by Ilya Gazman on 3/6/2016.
 * https://stackoverflow.com/questions/17674634/saving-and-reading-bitmaps-images-from-internal-
 * memory-in-android
 */
public class ImageFile {

    private String directoryName = "images";
    private String fileName = "image.jpg";
    private Context context;
    private boolean external;

    public ImageFile(Context context){
        this.context = context;
    }

    public ImageFile setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public ImageFile setExternal(boolean external) {
        this.external = external;
        return this;
    }

    public ImageFile setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
        return this;
    }

    public void save(Bitmap bitmapImage) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(createFile());
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @NonNull
    private File createFile() {
        File directory;
        if(external){
            directory = getAlbumStorageDir(directoryName);
        }
        else {
            directory = context.getDir(directoryName, Context.MODE_PRIVATE);
        }
        if(!directory.exists() && !directory.mkdirs()){
            Log.e("ImageSaver","Error creating directory " + directory);
        }

        return new File(directory, fileName);
    }

    private File getAlbumStorageDir(String albumName) {
        return new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public Bitmap load() {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(createFile());
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
