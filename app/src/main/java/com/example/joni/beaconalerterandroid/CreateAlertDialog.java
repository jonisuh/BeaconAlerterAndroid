package com.example.joni.beaconalerterandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
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

import com.example.joni.beaconalerterandroid.Services.PostAlertService;
import com.example.joni.beaconalerterandroid.Services.UpdateAlertService;
import com.example.joni.beaconalerterandroid.jsonentities.Alert;
import com.example.joni.beaconalerterandroid.jsonentities.Settings;

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
    private SharedPreferences prefs;

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
        prefs = getActivity().getSharedPreferences("com.example.joni.beaconalerterandroid", Context.MODE_PRIVATE);

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
                                Alert newAlert;

                                //If alert object exists (edit mode) update its values
                                if(alert != null){
                                    newAlert = alert;
                                //Else create new empty alert and generate UUID for it
                                }else{
                                    newAlert = new Alert();
                                    String newId = UUID.randomUUID().toString();
                                    newAlert.setId(newId);
                                    newAlert.setIsEnabled(true);
                                }

                                String title = alertTitleField.getText().toString();
                                boolean repeating = showRepeating;

                                Date time;
                                Calendar calendar = Calendar.getInstance();
                                boolean[] days = new boolean[7];

                                //If alert is repeating get correct repeating days and set time
                                if(showRepeating){
                                    for(int i=0;i<7; i++){
                                        days[i] = selectedButtons.get(dayButtons[i].getId());
                                    }
                                    calendar.set(2000,0,1,repeatingTimePicker.getCurrentHour(),repeatingTimePicker.getCurrentMinute(),0);
                                }else{
                                    //If alert is not repeating set all days to false and get exact date / time
                                    for(int i=0; i<7; i++){
                                        days[i] = false;
                                    }
                                    calendar.set(nonRepeatingDatePicker.getYear(),nonRepeatingDatePicker.getMonth(),nonRepeatingDatePicker.getDayOfMonth(),nonRepeatingTimePicker.getCurrentHour(),nonRepeatingTimePicker.getCurrentMinute(),0);
                                }
                                //Create date object from calender
                                time = calendar.getTime();

                                newAlert.setTitle(title);
                                newAlert.setTime(time);
                                newAlert.setRepeating(repeating);
                                newAlert.setDays(days);

                                String alertJson = newAlert.generateJson();
                                Log.d("CreateAlertDialog", alertJson);

                                //If alert exists, update it
                                AlertScheduler scheduler = AlertScheduler.getInstance();
                                Intent alertIntent;

                                if(alert != null){
                                    alertIntent = new Intent(getActivity(), UpdateAlertService.class);
                                    alertIntent.putExtra("alert", newAlert.generateJson());

                                    if(newAlert.isEnabled()) {
                                        Log.d("AlertScheduler", newAlert.getTime().toString());
                                        scheduler.rescheduleAlert(newAlert);
                                    }
                                //Else create a new alert
                                }else{
                                    alertIntent = new Intent(getActivity(), PostAlertService.class);
                                    alertIntent.putExtra("action", "post");
                                    alertIntent.putExtra("alert", newAlert.generateJson());
                                }

                                getActivity().startService(alertIntent);

                            }
                        });

        thisDialog = alertDialogBuilder.create();

        //Setting initial state for dialog
        thisDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //Setting up initial views
                Button createButton = thisDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                createButton.setEnabled(false);
                if(alertID != null){
                    Log.d("CreateAlertDialog", "Alert found");
                    titleText.setText("Edit alert");
                    createButton.setText("Save");
                    //Queying alert info from Content provider asynchronously
                    AlertQueryHandler handler = new AlertQueryHandler(getActivity().getContentResolver());
                    handler.execute();

                    repeatingView.setVisibility(View.INVISIBLE);
                    nonRepeatingView.setVisibility(View.GONE);
                }else{
                    Log.d("CreateAlertDialog", "No alert found, creating new");
                    showRepeating = true;
                    titleText.setText("New alert");
                    changeAlertModeView();
                }


                String hourMode = prefs.getString(Settings.HOUR_MODE,"24");
                boolean set24HourMode;
                if(hourMode.equals("24")){
                    set24HourMode = true;
                }else{
                    set24HourMode = false;
                }
                nonRepeatingTimePicker.setIs24HourView(set24HourMode);
                repeatingTimePicker.setIs24HourView(set24HourMode);
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
        clickedButton.setBackground(getResources().getDrawable(R.drawable.rounded_button));

        clickedButton.setTextColor(Color.WHITE);
    }

    private void setButtonUnselected(View v){
        selectedButtons.put(v.getId(), false);

        Button clickedButton = (Button) v;
        //clickedButton.setBackgroundColor(Color.WHITE);

        clickedButton.setBackground(getResources().getDrawable(R.drawable.unselected_rounded_button));
        clickedButton.setTextColor(Color.parseColor("#0026ff"));
    }

    //Query handler for asynchronously getting alert data
    private class AlertQueryHandler extends AsyncQueryHandler {
        public AlertQueryHandler(ContentResolver c) {
            super(c);
        }

        public void execute() {
            Log.d("CreateAlertDialog", "Queryhandler execute");
            String[] PROJECTION = new String[]{
                    AlertsTable.COLUMN_TITLE,
                    AlertsTable.COLUMN_TIME,
                    AlertsTable.COLUMN_REPEATING,
                    AlertsTable.COLUMN_ISENABLED,
                    AlertsTable.COLUMN_ALERTID,
                    AlertsTable.COLUMN_ID,
                    AlertsTable.COLUMN_DAYS
            };

            startQuery(0, null, AlertsProvider.ALERTS_CONTENT_URI, PROJECTION, AlertsTable.COLUMN_ALERTID + "='" + alertID+"'", null, null);
        }

        public void onQueryComplete(int t, Object command, Cursor c) {
            Log.d("CreateAlertDialog", "Queryhandler complete");
            if (c != null && c.getCount() > 0) {
                c.moveToNext();
                alert = new Alert(c);
                c.close();

                if(alert.getTitle() != "" || alert.getTitle() != null) {
                    alertTitleField.setText(alert.getTitle());
                }

                showRepeating = alert.isRepeating();
                changeAlertModeView();

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(alert.getTime());

                if(showRepeating){
                    repeatingTimePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
                    repeatingTimePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
                    boolean[] days = alert.getDays();
                    for(int i=0; i < days.length; i++){
                        selectedButtons.put(dayButtons[i].getId(),days[i]);
                        if(days[i]){
                            setButtonSelected(dayButtons[i]);
                        }else{
                            setButtonUnselected(dayButtons[i]);
                        }
                    }
                }else{
                    nonRepeatingTimePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
                    nonRepeatingTimePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
                    nonRepeatingDatePicker.updateDate(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
                }

                updateCreateButtonState();

            }
        }
    }
}
