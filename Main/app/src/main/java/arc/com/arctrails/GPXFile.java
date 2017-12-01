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
 *  18-11-17 Increment 2
 *  GPX File has a static parser to contain the use and avoid multiple instances of the GPX Parser.
 *  Handles all accesses and writes of GPX files using a third party GPX Library
 *  offered by AlternativeVision
 *  http://gpxparser.alternativevision.ro/
 */

class GPXFile {

    private static GPXParser gpxParser = new GPXParser();

    /**Created by Ayla Wickham for Increment 2
     * Returns the GPX object from the file parsed from the given file name
     *
     * Modified by Ryley to alert user to exceptions - *hopefully* never actually gets seen
     */
    static GPX getGPX(String filename, Context context) {
        FileInputStream in = null;

        File file = new File(context.getExternalFilesDir(null), filename);
        try {
            in = new FileInputStream(file);
            if(in != null) {
                return gpxParser.parseGPX(in);
            }
        } catch(FileNotFoundException e) {
            AlertUtils.showAlert(context,"File not found","Please notify the developers.");
        } catch (ParserConfigurationException e) {
            AlertUtils.showAlert(context,"Parser Exception","Please notify the developers.");
        } catch (IOException e) {
            AlertUtils.showAlert(context,"IO Exception","Please notify the developers.");
        } catch (SAXException e) {
            AlertUtils.showAlert(context,"SAXException","Please notify the developers.");
        }

        return null;
    }

    /**Created by Ayla Wickham for Increment 3
     *
     * Takes
     *
     * Modified by Ryley to alert user to exceptions - *hopefully* never actually gets seen
     */
    static void  writeGPXFile(String trackName, String description, ArrayList<Double[]> trackPoints, Context context) {
        //convert pairs of doubles to GPX waypoints
        ArrayList<Waypoint> waypoints = new ArrayList<>();
        for(Double[] list : trackPoints) {
            Waypoint point = new Waypoint();
            point.setLatitude(list[0]);
            point.setLongitude(list[1]);
            waypoints.add(point);
        }
        //builds the track out of the waypoints
        Track track = new Track();
        track.setTrackPoints(waypoints);
        //create a new file with the given name
        File file = new File(context.getExternalFilesDir(null), trackName+".gpx");
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            //create a GPX object and set the correct data
            GPX gpx = new GPX();
            //writes the name and description to the version and the creator fields
            //because those are the only fields we can access using the GPXParser
            //that are properties of the GPX file itself, and not a specific path
            gpx.setVersion(trackName);
            gpx.setCreator(description);
            //adds the track
            gpx.addTrack(track);
            //adds waypoints for start and finish
            gpx.addWaypoint(waypoints.get(0));
            gpx.addWaypoint(waypoints.get(waypoints.size()-1));
            //writes file
            gpxParser.writeGPX(gpx, out);
        } catch(FileNotFoundException e) {
            AlertUtils.showAlert(context,"File not found","Please notify the developers.");
        } catch (TransformerException e) {
            AlertUtils.showAlert(context,"Transformer Exception","Please notify the developers.");
        } catch (ParserConfigurationException e) {
            AlertUtils.showAlert(context,"Parser Exception","Please notify the developers.");
        }
    }
}
