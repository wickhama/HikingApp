package arc.com.arctrails;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**initAssets
 * Copies initial GPX files to phone so they can
 * be decoded for map usage when selected.
 * Created by Ayla Wickham
 * 2017-11-02. - Increment 2
 */

class initAssets {


    /** initAssets called at beginning of startup.
     * Loads preset trail files included in the app onto the phone's file system
     *
     * @param context - Context used to locate files
     */
    public static void initAssets(Context context) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        //String output = "";

        //Grabs the files within assets directory
        try{
            files = assetManager.list("");
        } catch(IOException e) {
            e.printStackTrace();
        }
        if(files != null) {
            InputStream in = null;
            OutputStream out = null;
            File outFile;

            //Iterate through each file and copyFile() to a new file in the phone's
            //internal storage
            for(String filename : files) {
                try {
                    in = assetManager.open(filename);
                    outFile = new File(context.getExternalFilesDir(null), filename);
                    out = new FileOutputStream(outFile);
                    copyFiles(in, out);
                    //finish with the streams
                    in.close();
                    in = null;
                    out.close();
                    out = null;
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
            //make sure resources are still closed in the case of an exception
            if(in != null) {
                try {
                    in.close();
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }
            if(out != null) {
                try{
                    out.close();
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** Copies the files from the in to the out stream
     *
     * @param in - InputStream
     * @param out - OutputStream
     * @throws IOException if the file cannot be read
     */
    public static void copyFiles(InputStream in, OutputStream out) throws IOException{
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
