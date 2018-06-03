package arc.com.arctrails;

public class FilterOptions {

    private boolean useDifficulty;
    private int difficulty;
    private boolean useRating;
    private int rating;
    private boolean useDistance;
    private int distance;
    private boolean useLength;
    private int length;

    public void reset() {
        useDifficulty = false;
        difficulty = 0;
        useRating = false;
        rating = 0;
        useDistance = false;
        distance = 0;
        useLength = false;
        length = 0;
    }

    public boolean useDifficulty() {
        return useDifficulty;
    }

    public void setUseDifficulty(boolean useDifficulty) {
        this.useDifficulty = useDifficulty;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public boolean useRating() {
        return useRating;
    }

    public void setUseRating(boolean useRating) {
        this.useRating = useRating;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public boolean useDistance() {
        return useDistance;
    }

    public void setUseDistance(boolean useDistance) {
        this.useDistance = useDistance;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public boolean useLength() {
        return useLength;
    }

    public void setUseLength(boolean useLength) {
        this.useLength = useLength;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
