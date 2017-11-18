package arc.com.arctrails;

import android.content.Context;

import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Waypoint;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by SamTheTurdBurgler on 2017-11-18.
 */

public class GPXFile {

    //Returns the GPX object of the file parsed
    public static GPX getGPX(String filename, Context context) {
        GPXParser gpxParser = new GPXParser();
        FileInputStream in = null;

        File file = new File(context.getExternalFilesDir(null), filename);
        try {
            in = new FileInputStream(file);
            if(in != null) {
                GPX gpx = gpxParser.parseGPX(in);
                return gpx;
            }
        } catch(FileNotFoundException e) {

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void  writeGPXFile(String trackName, String description, ArrayList<Double[]> trackPoints, Context context) {
        ArrayList<Waypoint> track = new ArrayList<Waypoint>();
        for(Double[] list : trackPoints) {
            Waypoint point = new Waypoint();
            point.setLatitude(list[0]);
            point.setLongitude(list[1]);
            track.add(point);
        }

        File file = new File(context.getExternalFilesDir(null), trackName);
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            GPX gpx = new GPX();
            //gpx.
        } catch(FileNotFoundException e) {

        }
    }

}
