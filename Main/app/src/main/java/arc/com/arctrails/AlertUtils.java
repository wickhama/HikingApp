package arc.com.arctrails;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

/**
 * @author Ryley
 * @since 2017-11-25
 *
 * I got tired of having to write this piece of code everywhere to notify the user of things,
 * so I gave it its own static class
 */
public class AlertUtils
{
    /**
     * Displays a message to the user, generally used if they gave some kind of invalid input.
     * Calls the onClickListener when the user dismisses it
     */
    public static void showAlert(Context context, String title, String message, DialogInterface.OnClickListener onClick)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, onClick);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();
    }

    /**
     * Displays a message to the user, and does nothing when they click it
     */
    public static void showAlert(Context context, String title, String message)
    {
        showAlert(context,title,message,
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    /**
     * Displays a message to the user, and asks them to confirm. if they do, does some action.
     * if they don't, does nothing
     */
    public static void showConfirm(Context context, String title, String message, DialogInterface.OnClickListener onClick)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                //do the onClick action if they confirm
                .setPositiveButton(android.R.string.yes,onClick)
                //do nothing if they dont confirm
                .setNegativeButton(android.R.string.no,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) { }
                    });
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();
    }
}
