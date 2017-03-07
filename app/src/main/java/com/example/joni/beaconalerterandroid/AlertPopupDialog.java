package com.example.joni.beaconalerterandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.AsyncQueryHandler;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.joni.beaconalerterandroid.jsonentities.Alert;

import java.text.SimpleDateFormat;

/**
 * Created by Joni on 6.3.2017.
 */
public class AlertPopupDialog extends DialogFragment{
    private Alert alert;
    private View dialogview;
    private AlertDialog thisDialog;

    private TextView alertTitleView;
    private TextView alertTimeView;
    private ImageButton stopAlertButton;
    private TextView alertProgressView;
    private Button snoozeButton;

    static AlertPopupDialog newInstance(String alert) {
        AlertPopupDialog dialog = new AlertPopupDialog();

        Bundle args = new Bundle();
        args.putString("alert", alert);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater li = LayoutInflater.from(getActivity());
        dialogview = li.inflate(R.layout.alert_dialog, null);

        if(getArguments() != null && getArguments().getString("alert") != null){
            this.alert = new Alert(getArguments().getString("alert"));
        }else{
            //Log.d("CreateAlertDialog", getArguments().getString("alertID"));
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Setting up views

        alertTitleView = (TextView) dialogview.findViewById(R.id.popupTitleText);
        alertTimeView = (TextView) dialogview.findViewById(R.id.popupTimeView);
        stopAlertButton = (ImageButton) dialogview.findViewById(R.id.closeAlertButton);
        alertProgressView = (TextView) dialogview.findViewById(R.id.popupProgressText);
        snoozeButton = (Button) dialogview.findViewById(R.id.snoozeButton);

        //TODO: Use settings here
        alertTitleView.setText(alert.getTitle());

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        alertTimeView.setText(timeFormat.format(alert.getTime()));

        stopAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: close alert here, delete onetime
                thisDialog.cancel();
            }
        });
        alertProgressView.setText("Searching for a beacon...");
        snoozeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: snooze here
                thisDialog.cancel();
            }
        });
        snoozeButton.setEnabled(false);
        //Builds the alert object
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(dialogview);
        alertDialogBuilder
                .setCancelable(false);

        thisDialog = alertDialogBuilder.create();

        //Setting initial state for dialog
        thisDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //Setting up initial views

            }
        });

        //Returning dialog
        return thisDialog;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }



}