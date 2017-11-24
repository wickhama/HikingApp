package arc.com.arctrails;

/**
 * Created by Ryley
 * 06-11-17
 */

public interface LocationRequestListener {
    boolean hasPermission();
    boolean requestPermission(LocationPermissionListener listener);
}
