package arc.com.arctrails;

/**
 * @author Ryley
 * @since 06-11-2017, Increment 1
 *
 * This interface is part of our system for allowing fragments to request permissions.
 * Permission windows are handle by the operating system, and when the user selects them,
 * the result is sent back to the current activity, but then either the activity has to handle
 * the specific actions for its fragments, or there needs to be a way to pass messages back to the
 * fragments so they can handle their own permission-required functionality.
 *
 * Android's framework has similar methods for handling permission requests, however they are
 * overly complicated because they are general methods for any subset of permission types.
 * Because this app only ever requires location permission, using this pair of interfaces
 * simplifies the message passing code
 *
 * @see LocationPermissionListener
 */

public interface LocationRequestListener {
    /**
     * Checks if permission is currently enabled without creating a request to enable
     * them. Useful for not pestering the user constantly.
     */
    boolean hasPermission();

    /**
     * Checks if permission is enabled, and creates a request popup if they are not.
     * Takes a LocationPermissionListener as a parameter which will handle the implementation of
     * the resulting event.
     * If a permission request is already on-screen, this method will add the listener to a list of
     * listeners waiting for the result, and they will all be notified when the result is returned.
     */
    boolean requestPermission(LocationPermissionListener listener);
}
