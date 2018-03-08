package arc.com.arctrails;

/**
 * @author Ryley
 * @since 06-11-2017, Increment 4
 *
 * This interface is part of our system for allowing fragments to request permissions.
 * To keep fragment implementation details contained within the fragment, permission requests are
 * sent to a handler, and the result is sent back to the fragment so that it can handle specific
 * implementation details itself.
 *
 * @see InternetRequestListener
 */

public interface InternetPermissionListener {
    /**
     * Gets called by the permission handler when the user responds to a permission request.
     * takes the result of the request, whether it was accepted or denied, and should check this
     * before beginning location-sensitive actions.
     *
     * There is a possible race condition if the user manages to disable permissions again after
     * selecting the option to allow, but this should be virtually impossible
     *
     * @see InternetRequestListener#requestPermission(InternetPermissionListener)
     */
    void onPermissionResult(boolean result);
}
