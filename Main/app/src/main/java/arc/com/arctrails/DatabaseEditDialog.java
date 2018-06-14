package arc.com.arctrails;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Created by Ryley on 2018-03-18.
 */
public class DatabaseEditDialog extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
    * implement this interface in order to receive event callbacks.
    * Each method passes the DialogFragment in case the host needs to query it. */
    public interface DatabaseEditDialogListener {
        void onDialogPositiveClick(DatabaseEditDialog dialog);
    }

    // Use this instance of the interface to deliver action events
    DatabaseEditDialogListener mListener;
    View mView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the DatabaseEditDialogListener so we can send events to the host
            mListener = (DatabaseEditDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement DatabaseEditDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        // remembers the view for later
        mView = inflater.inflate(R.layout.dialog_database_edit, null);
        builder.setView(mView);
        builder.setTitle(R.string.database_edit_title)
                //add the DatabaseEdit if they confirm
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mListener.onDialogPositiveClick(DatabaseEditDialog.this);
                            }
                        })
                //do nothing if they dont confirm
                .setNegativeButton(android.R.string.no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DatabaseEditDialog.this.getDialog().cancel();
                            }
                        });
        return builder.create();
    }

    public boolean allowEdit() {
        CheckBox field = mView.findViewById(R.id.database_edit_check);
        return field.isChecked();
    }
}
