package arc.com.arctrails;

/**
 * Created by ryley_000 on 2017-11-06.
 */

public interface LocationRequestListener {
    boolean hasPermission();
    boolean requestPermission(LocationPermissionListener listener);
}
