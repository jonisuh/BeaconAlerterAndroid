package com.example.joni.beaconalerterandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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

import java.util.HashMap;

/**
 * Created by Joni on 25.2.2017.
 */
public class CreateAlertDialog extends DialogFragment implements View.OnClickListener{
    private String alertID;
    private Alert alert;

    private View dialogview;

    //Determines which view is shown
    private boolean showRepeating;

    //Used by both alert types
    private TextView titleText;
    private EditText alertTitleField;
    private TextView showAlertTypeText;
    private Button changeAlertTypeButton;

    //Used by repeating alert
    private RelativeLayout repeatingView;
    private TimePicker repeatingTimePicker;
    private Button[] dayButtons;
    private HashMap<Integer, Boolean> selectedButtons;
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

        if(getArguments() != null && getArguments().getString("alertID") != null){
            alertID = getArguments().getString("alertID");
        }else{
            //Log.d("CreateAlertDialog", getArguments().getString("alertID"));
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        titleText = (TextView) dialogview.findViewById(R.id.titleText);
        alertTitleField = (EditText) dialogview.findViewById(R.id.alertTitleField);
        showAlertTypeText = (TextView) dialogview.findViewById(R.id.showAlertTypeText);

        changeAlertTypeButton = (Button) dialogview.findViewById(R.id.changeAlertTypeButton);
        changeAlertTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRepeating = !showRepeating;
                changeAlertModeView();
            }
        });
        repeatingView = (RelativeLayout) dialogview.findViewById(R.id.repeatingView);
        repeatingTimePicker = (TimePicker) dialogview.findViewById(R.id.repeatingTimePicker);

        dayButtons = new Button[7];
        selectedButtons = new HashMap<>();
        for(int i = 0; i<=6; i++){
            String buttonId = "dayButton" + i;
            int id = getResources().getIdentifier(buttonId,"id",dialogview.getContext().getPackageName());

            selectedButtons.put(id,false);

            Button dayBtn = (Button) dialogview.findViewById(id);
            dayButtons[i] = dayBtn;
            dayBtn.setOnClickListener(this);
        }


        everyDayButton = (Button) dialogview.findViewById(R.id.everyDayButton);
        everyDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i<=6; i++){
                    setButtonSelected(dayButtons[i]);
                }
            }
        });

        nonRepeatingView = (LinearLayout) dialogview.findViewById(R.id.nonRepeatingView);
        nonRepeatingTimePicker = (TimePicker) dialogview.findViewById(R.id.nonRepeatingTimePicker);
        nonRepeatingDatePicker = (DatePicker) dialogview.findViewById(R.id.nonRepeatingDatePicker);

        //TODO: Set up alert based on settings and alert id here
        //TODO: Need to do initial config if alert exists
        if(alertID != null){
            Log.d("CreateAlertDialog", "Alert found");
        }else{
            Log.d("CreateAlertDialog", "No alert found, creating new");
            showRepeating = true;
            nonRepeatingTimePicker.setIs24HourView(true);
            repeatingTimePicker.setIs24HourView(true);
        }
        changeAlertModeView();

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
                               //TODO: Add alert creation
                            }
                        });


        return alertDialogBuilder.create();
    }

    private void changeAlertModeView(){
        if(this.showRepeating){
            showAlertTypeText.setText("Create a repeating alert");
            changeAlertTypeButton.setText("One time");
            repeatingView.setVisibility(View.VISIBLE);
            nonRepeatingView.setVisibility(View.GONE);
        }else{
            showAlertTypeText.setText("Create a one time alert");
            changeAlertTypeButton.setText("Repeating");
            repeatingView.setVisibility(View.GONE);
            nonRepeatingView.setVisibility(View.VISIBLE);
        }
    }

    private void updateCreateButtonState(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onClick(View v) {
        Log.d("CreateAlertDialog",""+v.getId());

        if(selectedButtons.get(v.getId())){

            setButtonUnselected(v);
        }else{
            setButtonSelected(v);
        }
    }

    private void setButtonSelected(View v){
        selectedButtons.put(v.getId(), true);

        Button clickedButton = (Button) v;
        clickedButton.setBackgroundColor(Color.parseColor("#0026ff"));
        clickedButton.setTextColor(Color.WHITE);
    }

    private void setButtonUnselected(View v){
        selectedButtons.put(v.getId(), false);

        Button clickedButton = (Button) v;
        clickedButton.setBackgroundColor(Color.WHITE);
        clickedButton.setTextColor(Color.parseColor("#0026ff"));
    }
}
