package arc.com.arctrails;

import android.content.Context;

import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Track;
import org.alternativevision.gpx.beans.Waypoint;
import org.alternativevision.gpx.extensions.DummyExtensionParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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

    private static String   TRAIL_EXTENSION = "trail",
            TRAIL_NAME = "name",
            TRAIL_DESCRIPTION = "desc",
            TRAIL_LOCATION = "location",
            TRAIL_DIFFICULTY = "difficulty",
            TRAIL_NOTES = "notes";

    private static GPXParser gpxParser;

    private static void buildParser(){
        gpxParser = new GPXParser();
        gpxParser.addExtensionParser(new DummyExtensionParser(){
            @Override
            public String getId(){
                return TRAIL_EXTENSION;
            }

            @Override
            public Object parseGPXExtension(Node node){
                Trail trail = new Trail();

                NodeList childNodes = node.getChildNodes();
                Node currentNode;
                if(childNodes != null){
                    for(int i = 0; i < childNodes.getLength(); i++){
                        currentNode = childNodes.item(i);
                        if(TRAIL_NAME.equals(currentNode.getNodeName())){
                            trail.getMetadata().setName(currentNode.getFirstChild().getNodeValue());
                        }
                        if(TRAIL_DESCRIPTION.equals(currentNode.getNodeName())){
                            trail.getMetadata().setDescription(currentNode.getFirstChild().getNodeValue());
                        }
                        if(TRAIL_LOCATION.equals(currentNode.getNodeName())){
                            trail.getMetadata().setLocation(currentNode.getFirstChild().getNodeValue());
                        }
                        if(TRAIL_DIFFICULTY.equals(currentNode.getNodeName())){
                            trail.getMetadata().setDifficulty(currentNode.getFirstChild().getNodeValue());
                        }
                        if(TRAIL_NOTES.equals(currentNode.getNodeName())){
                            trail.getMetadata().setNotes(currentNode.getFirstChild().getNodeValue());
                        }
                    }
                }
                return trail;
            }

            @Override
            public void writeGPXExtensionData(Node extensionNode, GPX gpx, Document doc){
                Trail trail = (Trail)gpx.getExtensionData(TRAIL_EXTENSION);
                Node node;

                if(trail.getMetadata().getName() != null) {
                    node = doc.createElement(TRAIL_NAME);
                    node.appendChild(doc.createTextNode(trail.getMetadata().getName()));
                    extensionNode.appendChild(node);
                }
                if(trail.getMetadata().getDescription() != null) {
                    node = doc.createElement(TRAIL_DESCRIPTION);
                    node.appendChild(doc.createTextNode(trail.getMetadata().getDescription()));
                    extensionNode.appendChild(node);
                }
                if(trail.getMetadata().getLocation() != null) {
                    node = doc.createElement(TRAIL_LOCATION);
                    node.appendChild(doc.createTextNode(trail.getMetadata().getLocation()));
                    extensionNode.appendChild(node);
                }
                if(trail.getMetadata().getDifficulty() != null) {
                    node = doc.createElement(TRAIL_DIFFICULTY);
                    node.appendChild(doc.createTextNode(trail.getMetadata().getDifficulty()));
                    extensionNode.appendChild(node);
                }
                if(trail.getMetadata().getNotes() != null) {
                    node = doc.createElement(TRAIL_NOTES);
                    node.appendChild(doc.createTextNode(trail.getMetadata().getNotes()));
                    extensionNode.appendChild(node);
                }
            }
        });
    }

    private static Trail parseGPXtoTrail(GPX gpx){
        Trail trail = (Trail)gpx.getExtensionData(TRAIL_EXTENSION);
        ArrayList<Trail.Waypoint> waypoints = new ArrayList<>();
        ArrayList<Trail.Track> tracks = new ArrayList<>();
        ArrayList<Trail.Waypoint> trackPoints;
        Trail.Waypoint waypoint;
        Trail.Track track;

        if(gpx.getWaypoints() != null) {
            for (Waypoint w : gpx.getWaypoints()) {
                waypoint = new Trail.Waypoint();
                waypoint.setWaypointName(w.getName());
                waypoint.setLatitude(w.getLatitude());
                waypoint.setLongitude(w.getLongitude());
                waypoint.setComment(w.getComment());
                waypoint.setWaypointType(w.getType());
                waypoints.add(waypoint);
            }
        }
        if(gpx.getTracks() != null) {
            for (Track t : gpx.getTracks()) {
                track = new Trail.Track();
                trackPoints = new ArrayList<>();

                for (Waypoint w : t.getTrackPoints()) {
                    waypoint = new Trail.Waypoint();
                    waypoint.setWaypointName(w.getName());
                    waypoint.setLatitude(w.getLatitude());
                    waypoint.setLongitude(w.getLongitude());
                    trackPoints.add(waypoint);
                }
                track.setTrackPoints(trackPoints);
                tracks.add(track);
            }
        }
        trail.setWaypoints(waypoints);
        trail.setTracks(tracks);

        return trail;
    }

    private static GPX parseTrailtoGPX(Trail trail)
    {
        GPX gpx = new GPX();
        Waypoint waypoint;
        Track track;
        ArrayList<Waypoint> trackPoints;

        gpx.setVersion("1.1");
        gpx.setCreator("ArcTrails");
        gpx.addExtensionData(TRAIL_EXTENSION, trail);
        if(trail.getWaypoints() != null) {
            for (Trail.Waypoint w : trail.getWaypoints()) {
                waypoint = new Waypoint();
                waypoint.setName(w.getWaypointName());
                waypoint.setLatitude(w.getLatitude());
                waypoint.setLongitude(w.getLongitude());
                waypoint.setComment(w.getComment());
                waypoint.setType(w.getWaypointType());
                gpx.addWaypoint(waypoint);
            }
        }
        if(trail.getTracks() != null) {
            for (Trail.Track t : trail.getTracks()) {
                track = new Track();
                trackPoints = new ArrayList<>();

                for (Trail.Waypoint w : t.getTrackPoints()) {
                    waypoint = new Waypoint();
                    waypoint.setName(w.getWaypointName());
                    waypoint.setLatitude(w.getLatitude());
                    waypoint.setLongitude(w.getLongitude());
                    trackPoints.add(waypoint);
                }
                track.setTrackPoints(trackPoints);
                gpx.addTrack(track);
            }
        }

        return gpx;
    }

    /**Created by Ayla Wickham for Increment 2
     * Returns the GPX object from the file parsed from the given file name
     *
     * Modified by Ryley to alert user to exceptions - *hopefully* never actually gets seen
     */
    static Trail getGPX(String filename, Context context) {
        if(gpxParser == null)
            buildParser();

        FileInputStream in = null;

        File file = new File(context.getExternalFilesDir(null), filename);
        try {
            in = new FileInputStream(file);
            if(in != null) {
                GPX gpxData = gpxParser.parseGPX(in);
                return parseGPXtoTrail(gpxData);
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
    static void  writeGPXFile(Trail trail, Context context) {
        if(gpxParser == null)
            buildParser();

        GPX gpx = parseTrailtoGPX(trail);

        //create a new file with the given name
        File file = new File(context.getExternalFilesDir(null), trail.getMetadata().getName()+".gpx");
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
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
