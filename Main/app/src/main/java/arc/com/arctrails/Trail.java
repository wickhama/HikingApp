package arc.com.arctrails;

import android.graphics.Bitmap;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by graememorgan on 2018-02-26.
 *
 * Simplified verion of GPX data that can be stored in the firebase database
 */

public class Trail {
    private Metadata metadata;
    private List<Waypoint> waypoints;
    private List<Track> tracks;

    public static class Metadata{
        private String name;
        private String description;
        private String location;
        private String difficulty;
        private String notes;
        private String id;
        private Boolean hasImage;

        public Boolean hasImage() {
            return hasImage;
        }

        public Boolean getHasImage() {
            return hasImage;
        }

        public void setHasImage(Boolean hasImage) {
            this.hasImage = hasImage;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }


        public String getName() { return name; }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(String difficulty) {
            this.difficulty = difficulty;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    public static class Waypoint {
        private String waypointName;
        private String type;
        private String comment;
        private double latitude;
        private double longitude;

        public Waypoint(){}

        public Waypoint(String name, double lat, double lng) {
            waypointName = name;
            latitude = lat;
            longitude = lng;
        }

        public String getWaypointName() {
            return waypointName;
        }

        public void setWaypointName(String name) {
            this.waypointName = name;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public String getWaypointType() {
            return type;
        }

        public void setWaypointType(String type) {
            this.type = type;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }

    public static class Track {
        private List<Waypoint> trackPoints;

        public Track(){
            this( new ArrayList<Waypoint>() );
        }

        public Track(List<Waypoint> list){
            trackPoints = list;
        }

        public List<Waypoint> getTrackPoints() {
            return trackPoints;
        }

        public void setTrackPoints(List<Waypoint> trackPoints) {
            this.trackPoints = trackPoints;
        }
    }

    public Trail() {
        this(null,null,null);
    }

    public Trail(String name, String description, String location) {
        this.metadata = new Metadata();
        metadata.setName(name);
        metadata.setDescription(description);
        metadata.setLocation(location);
        this.waypoints = new ArrayList<>();
        this.tracks = new ArrayList<>();
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata){
        this.metadata = metadata;
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<Waypoint> waypoints) {
        this.waypoints = waypoints;
    }

    public void addWaypoint(Waypoint waypoint){ waypoints.add(waypoint); }

    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }

    public void addTrack(Track track){ tracks.add(track); }
}
