package arc.com.arctrails;

import java.util.List;

public interface DatabaseListener {
    void onDataList(List<String> entryIDs);
    void onDataTrail(Trail trail);
}
