package arc.com.arctrails;

import android.content.Context;

import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Track;
import org.alternativevision.gpx.beans.Waypoint;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * Created by Ayla Wickham
 *  18-11-17
 */

class GPXFile {

    private static GPXParser gpxParser = new GPXParser();

    //Returns the GPX object of the file parsed
    static GPX getGPX(String filename, Context context) {
        FileInputStream in = null;

        File file = new File(context.getExternalFilesDir(null), filename);
        try {
            in = new FileInputStream(file);
            if(in != null) {
                return gpxParser.parseGPX(in);
            }
        } catch(FileNotFoundException e) {
        //e.printStackTrace(); should this be here?
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return null;
    }

    static void  writeGPXFile(String trackName, String description, ArrayList<Double[]> trackPoints, Context context) {
        ArrayList<Waypoint> waypoints = new ArrayList<>();
        for(Double[] list : trackPoints) {
            Waypoint point = new Waypoint();
            point.setLatitude(list[0]);
            point.setLongitude(list[1]);
            waypoints.add(point);
        }
        Track track = new Track();
        track.setTrackPoints(waypoints);
        track.setName(trackName);
        track.setDescription(description);

        File file = new File(context.getExternalFilesDir(null), trackName+".gpx");
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            GPX gpx = new GPX();
            //adds the track
            gpx.addTrack(track);
            //adds waypoints for start and finish
            gpx.addWaypoint(waypoints.get(0));
            gpx.addWaypoint(waypoints.get(waypoints.size()-1));
            //writes file
            gpxParser.writeGPX(gpx, out);
        } catch(FileNotFoundException e) {
            //TODO: Error handling
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

    }

}
