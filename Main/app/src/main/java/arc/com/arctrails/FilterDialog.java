package arc.com.arctrails;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

/**
 * Created by Ryley on 2018-03-18.
 */

public class FilterDialog extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
    * implement this interface in order to receive event callbacks.
    * Each method passes the DialogFragment in case the host needs to query it. */

    public interface FilterDialogListener {
        void onDialogPositiveClick(FilterDialog dialog);
    }

    // Use this instance of the interface to deliver action events
    FilterDialogListener mListener;
    View mView;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the FilterDialogListener so we can send events to the host
            mListener = (FilterDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement FilterDialogListener");
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
        mView = inflater.inflate(R.layout.dialog_filter, null);
        builder.setView(mView);

        //add the Filter if they confirm
        builder.setPositiveButton(R.string.filterButton,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mListener.onDialogPositiveClick(FilterDialog.this);
                            }
                        })
                //do nothing if they don't confirm
                .setNegativeButton(android.R.string.no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FilterDialog.this.getDialog().cancel();
                            }
                        });

        mView.findViewById(R.id.difficultySwitch).setOnClickListener(
                linkSwitchAndSpinner(
                        (Switch)mView.findViewById(R.id.difficultySwitch),
                        (Spinner)mView.findViewById(R.id.difficultySpinner)
        ));

        mView.findViewById(R.id.ratingSwitch).setOnClickListener(
                linkSwitchAndSpinner(
                        (Switch)mView.findViewById(R.id.ratingSwitch),
                        (Spinner)mView.findViewById(R.id.ratingSpinner)
        ));

        mView.findViewById(R.id.distanceSwitch).setOnClickListener(
                linkSwitchAndSpinner(
                        (Switch)mView.findViewById(R.id.distanceSwitch),
                        (Spinner)mView.findViewById(R.id.distanceSpinner)
        ));


        mView.findViewById(R.id.lengthSwitch).setOnClickListener(
                linkSwitchAndSpinner(
                        (Switch)mView.findViewById(R.id.lengthSwitch),
                        (Spinner)mView.findViewById(R.id.lengthSpinner)
        ));

//        mView.findViewById(R.id.nameSwitch).setOnClickListener(
//                linkSwitchAndEditText(
//                        (Switch)mView.findViewById(R.id.nameSwitch),
//                        (EditText)mView.findViewById(R.id.nameFilterInput)
//                ));



        return builder.create();
    }

    /**
     *This hides the spinner selection until the switch is selected.
     */
    private View.OnClickListener linkSwitchAndSpinner(final Switch toggle, final Spinner spinner) {
        if(toggle.isChecked()){
            spinner.setVisibility(View.VISIBLE);
        }else{
            spinner.setVisibility(View.GONE);
        }
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toggle.isChecked()){
                    spinner.setVisibility(View.VISIBLE);
                }else{
                    spinner.setVisibility(View.GONE);
                }
            }
        };
    }

//    /**
//     *This hides EditText box until the switch is selected.
//     */
//    private View.OnClickListener linkSwitchAndEditText(final Switch toggle, final EditText trailInput) {
//        if(toggle.isChecked()){
//            trailInput.setVisibility(View.VISIBLE);
//        }else{
//            trailInput.setVisibility(View.GONE);
//        }
//        return new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(toggle.isChecked()){
//                    trailInput.setVisibility(View.VISIBLE);
//                }else{
//                    trailInput.setVisibility(View.GONE);
//                }
//            }
//        };
//    }


    /**
     * NONE OF THESE CHANGES HAVE BEEN PUSHED! Including the GUI for the filtering and the following
     * checkers and getters.
     */


    /**
     * Difficulty
     */
    public boolean useDifficulty(){
        Switch diffswitch = mView.findViewById(R.id.difficultySwitch);
        return diffswitch.isChecked();
    }

    public int getDifficulty(){
        Spinner spinner = mView.findViewById(R.id.difficultySpinner);
        return spinner.getSelectedItemPosition();
    }


    /**
     * Rating
     */
    public boolean useRating(){
        Switch rateSwitch = mView.findViewById(R.id.ratingSwitch);
        return rateSwitch.isChecked();

    }

    public int getRating(){
        Spinner spinner = mView.findViewById(R.id.ratingSpinner);
        return spinner.getSelectedItemPosition();
    }

    /**
     * Distance
     */
    public boolean useDistance(){
        Switch distSwitch = mView.findViewById(R.id.distanceSwitch);
        return distSwitch.isChecked();
    }

    public int getDistance(){
        Spinner spinner = mView.findViewById(R.id.distanceSpinner);
        return spinner.getSelectedItemPosition();
    }

    /**
     * Length
     */
    public boolean useLength(){
        Switch lengthSwitch = mView.findViewById(R.id.lengthSwitch);
        return lengthSwitch.isChecked();
    }

    public int getLength(){
        Spinner spinner = mView.findViewById(R.id.lengthSpinner);
        return spinner.getSelectedItemPosition();
    }



    //    /**
//     * TrailName
//     */
//    public boolean useTrailName(){
//        Switch nameSwitch = mView.findViewById(R.id.nameSwitch);
//        return nameSwitch.isChecked();
//    }
//
//    public String getTrailName(){
//        EditText editText = mView.findViewById(R.id.nameFilterInput);
//        return editText.toString();
//    }





}
