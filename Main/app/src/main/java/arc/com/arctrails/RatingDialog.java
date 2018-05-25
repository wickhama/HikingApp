package arc.com.arctrails;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

/**
 * Created by Ryley on 2018-03-18.
 */

public class RatingDialog extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
    * implement this interface in order to receive event callbacks.
    * Each method passes the DialogFragment in case the host needs to query it. */
    public interface RatingDialogListener {
        void onDialogPositiveClick(RatingDialog dialog);
    }

    // Use this instance of the interface to deliver action events
    private RatingDialogListener mListener;
    private View mView;
    private int rating;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the WaypointDialogListener so we can send events to the host
            mListener = (RatingDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement RatingDialogListener");
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
        mView = inflater.inflate(R.layout.dialog_rating, null);

        mView.findViewById(R.id.rate1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRate1(v);
            }
        });

        mView.findViewById(R.id.rate2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRate2(v);
            }
        });

        mView.findViewById(R.id.rate3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRate3(v);
            }
        });

        mView.findViewById(R.id.rate4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRate4(v);
            }
        });

        mView.findViewById(R.id.rate5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRate5(v);
            }
        });

        builder.setView(mView);
        builder.setTitle(R.string.RateTrail)
                //add the waypoint if they confirm
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mListener.onDialogPositiveClick(RatingDialog.this);
                            }
                        })
                //do nothing if they dont confirm
                .setNegativeButton(android.R.string.no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RatingDialog.this.getDialog().cancel();
                            }
                        });
        return builder.create();
    }

    public void onRate1(View v) {
        rating = 1;
        setRatingDisplay(rating);
    }

    public void onRate2(View v) {
        rating = 2;
        setRatingDisplay(rating);
    }

    public void onRate3(View v) {
        rating = 3;
        setRatingDisplay(rating);
    }

    public void onRate4(View v) {
        rating = 4;
        setRatingDisplay(rating);
    }

    public void onRate5(View v) {
        rating = 5;
        setRatingDisplay(rating);
    }

    private void setRatingDisplay(int rating) {
        ImageButton[] rateButton = {
                mView.findViewById(R.id.rate1),
                mView.findViewById(R.id.rate2),
                mView.findViewById(R.id.rate3),
                mView.findViewById(R.id.rate4),
                mView.findViewById(R.id.rate5)
        };

        int i = 0;
        for(; i < rating; i++)
            setButtonHighlight(rateButton[i], true);
        for(; i < 5; i++)
            setButtonHighlight(rateButton[i], false);
    }

    private void setButtonHighlight(ImageButton button, boolean highlight)
    {
        if(highlight) {
            button.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_orange_dark,null)));
        }
        else{
            button.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.cardview_dark_background,null)));
        }
    }

    public int getRating() {
        return rating;
    }
}
