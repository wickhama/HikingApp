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
 * Created by SamTheTurdBurgler on 2017-11-02.
 */

class initAssets {

<<<<<<< HEAD
    /** initAssets called at beginning of startup
     *
     * @param context - Context
     */
    public static void initAssets(Context context) {
=======
    static void initAssets(Context context) {
>>>>>>> 318b2b15a3b6b45f1f7b9d26c8d3d10d08845c85
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
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
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
     * @throws IOException
     */
    private static void copyFiles(InputStream in, OutputStream out) throws IOException{
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
