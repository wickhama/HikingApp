package arc.com.arctrails;

public class FilterOptions {

    private int difficulty;
    private int rating;
    private int distance;
    private int length;

    public void setDifficulty(int difficulty){
        this.difficulty = difficulty;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
