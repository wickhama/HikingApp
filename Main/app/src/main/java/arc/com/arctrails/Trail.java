package arc.com.arctrails;

/**
 * Created by graememorgan on 2018-02-26.
 */

public class Trail {
    private String name;
    private String description;
    private String location;
    private String difficulty;
    private String notes;
    private String gpxFile;

    public Trail() {
    }

    public Trail(String name, String description, String location, String difficulty, String notes, String gpxFile) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.difficulty = difficulty;
        this.notes = notes;
        this.gpxFile = gpxFile;
    }

    public String getName() {
        return name;
    }

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

    public String getGpxFile() {
        return gpxFile;
    }

    public void setGpxFile(String gpxFile) {
        this.gpxFile = gpxFile;
    }
}
