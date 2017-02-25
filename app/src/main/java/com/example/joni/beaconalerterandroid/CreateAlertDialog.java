package com.example.joni.beaconalerterandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.joni.beaconalerterandroid.jsonentities.Alert;

/**
 * Created by Joni on 25.2.2017.
 */
public class CreateAlertDialog extends DialogFragment {
    private String alertID;
    private Alert alert;

    private View dialogview;

    //Used by both alert types
    private TextView titleText;
    private EditText alertTitleField;
    private TextView showAlertTypeText;
    private Button changeAlertTypeButton;

    //Used by repeating alert
    private RelativeLayout repeatingView;
    private TimePicker repeatingTimePicker;
    private Button[] dayButtons;
    private Button everyDayButton;

    //Used by one time alert
    private LinearLayout nonRepeatingView;
    private TimePicker nonRepeatingTimePicker;
    private DatePicker nonRepeatingDatePicker;

    static CreateAlertDialog newInstance(String alertID) {
        CreateAlertDialog dialog = new CreateAlertDialog();

        Bundle args = new Bundle();
        args.putString("alertID", alertID);
        dialog.setArguments(args);

        return dialog;
    }

    static CreateAlertDialog newInstance() {
        CreateAlertDialog dialog = new CreateAlertDialog();
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater li = LayoutInflater.from(getActivity());
        dialogview = li.inflate(R.layout.new_alert_dialog, null);

        if(!getArguments().isEmpty() && getArguments().getString("alertID") != null){
            alertID = getArguments().getString("userID");
        }else{
            Log.d("CreateAlertDialog", getArguments().getString("alertID"));
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Builds the alert object
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(dialogview);
        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .setPositiveButton("Create",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                               //Add alert creation
                            }
                        });


        return alertDialogBuilder.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }
}
