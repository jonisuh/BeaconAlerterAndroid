package com.example.joni.beaconalerterandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Joni on 25.2.2017.
 */
public class CreateAlertDialog extends DialogFragment implements View.OnClickListener{
    private String alertID;
    private Alert alert;

    private View dialogview;
    private AlertDialog thisDialog;

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
                updateCreateButtonState();
            }
        });

        repeatingView = (RelativeLayout) dialogview.findViewById(R.id.repeatingView);
        repeatingTimePicker = (TimePicker) dialogview.findViewById(R.id.repeatingTimePicker);

        dayButtons = new Button[7];
        selectedButtons = new HashMap<>();
        for(int i = 0; i<=6; i++){
            String buttonId = "dayButton" + i;
            int id = getResources().getIdentifier(buttonId,"id",dialogview.getContext().getPackageName());

            selectedButtons.put(id, false);

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
                updateCreateButtonState();
            }
        });

        nonRepeatingView = (LinearLayout) dialogview.findViewById(R.id.nonRepeatingView);
        nonRepeatingTimePicker = (TimePicker) dialogview.findViewById(R.id.nonRepeatingTimePicker);
        nonRepeatingDatePicker = (DatePicker) dialogview.findViewById(R.id.nonRepeatingDatePicker);

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

                                String title = alertTitleField.getText().toString();
                                boolean isEnabled = true;
                                boolean repeating = showRepeating;
                                //TODO: Generate locally unique UUID
                                String newId = UUID.randomUUID().toString();

                                Date time;
                                Calendar calendar = Calendar.getInstance();
                                boolean[] days = new boolean[7];

                                //If alert is repeating get correct repeating days and set time
                                if(showRepeating){
                                    for(int i=0;i<7; i++){
                                        days[i] = selectedButtons.get(dayButtons[i].getId());
                                    }
                                    calendar.set(2000,0,1,repeatingTimePicker.getCurrentHour(),repeatingTimePicker.getCurrentMinute());
                                }else{
                                    //If alert is not repeating set all days to false and get exact date / time
                                    for(int i=0; i<7; i++){
                                        days[i] = false;
                                    }
                                    calendar.set(nonRepeatingDatePicker.getYear(),nonRepeatingDatePicker.getMonth(),nonRepeatingDatePicker.getDayOfMonth(),nonRepeatingTimePicker.getCurrentHour(),repeatingTimePicker.getCurrentMinute());
                                }
                                //Create date object from calender
                                time = calendar.getTime();

                                alert = new Alert(title,time,repeating,isEnabled,newId,days);
                                String alertJson = alert.generateJson();
                                Log.d("CreateAlertDialog", alertJson);

                                ContentValues values = alert.generateContentValue();
                                getActivity().getContentResolver().insert(AlertsProvider.ALERTS_CONTENT_URI, values);

                                Cursor testCursor = getActivity().getContentResolver().query(AlertsProvider.ALERTS_CONTENT_URI,null,AlertsTable.COLUMN_ALERTID+"='"+alert.getId()+"'",null,null);

                                while(testCursor.moveToNext()){
                                    Log.d("CreateAlertDialog", testCursor.getString(testCursor.getColumnIndex(AlertsTable.COLUMN_TITLE)));
                                    Log.d("CreateAlertDialog", testCursor.getString(testCursor.getColumnIndex(AlertsTable.COLUMN_TIME)));
                                    Log.d("CreateAlertDialog", ""+testCursor.getInt(testCursor.getColumnIndex(AlertsTable.COLUMN_REPEATING)));
                                    Log.d("CreateAlertDialog", ""+testCursor.getInt(testCursor.getColumnIndex(AlertsTable.COLUMN_ISENABLED)));
                                    Log.d("CreateAlertDialog", testCursor.getString(testCursor.getColumnIndex(AlertsTable.COLUMN_ALERTID)));
                                    Log.d("CreateAlertDialog", testCursor.getString(testCursor.getColumnIndex(AlertsTable.COLUMN_DAYS)));

                                }
                                testCursor.close();
                                /*
                                Intent createAlertIntent = new Intent(getActivity(), CreateAlertService.class);

                                createAlertIntent.putExtra("alert", alertJson);

                                getActivity().startService(createAlertIntent);
                                */
                            }
                        });

        thisDialog = alertDialogBuilder.create();

        //Setting initial state for dialog
        thisDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //Setting up initial views
                //TODO: Set up alert based on settings and alert id here
                //TODO: Need to do initial config if alert exists
                if(alertID != null){
                    Log.d("CreateAlertDialog", "Alert found");
                }else{
                    Log.d("CreateAlertDialog", "No alert found, creating new");
                    showRepeating = true;
                    titleText.setText("New alert");
                    nonRepeatingTimePicker.setIs24HourView(true);
                    repeatingTimePicker.setIs24HourView(true);
                }
                changeAlertModeView();
                updateCreateButtonState();
            }
        });

        //Returning dialog
        return thisDialog;
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
        Button createButton = thisDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if(this.showRepeating){
            Boolean daysSelected = false;
            Iterator iterator = selectedButtons.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry entry = (Map.Entry)iterator.next();
                if((Boolean) entry.getValue()){
                    daysSelected = true;
                }
            }

            //If one or more buttons have been selected (alert has a valid repetition day) we enable creation
            if(daysSelected){
                createButton.setEnabled(true);
            }else{
                createButton.setEnabled(false);
            }
        }else{
            createButton.setEnabled(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onClick(View v) {

        if(selectedButtons.get(v.getId())){
            setButtonUnselected(v);
        }else{
            setButtonSelected(v);
        }
        updateCreateButtonState();
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
