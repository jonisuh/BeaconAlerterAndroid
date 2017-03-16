package com.example.joni.beaconalerterandroid;

import android.app.AlertDialog;
        import android.app.Dialog;
        import android.app.DialogFragment;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.content.Context;
        import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
        import android.graphics.Color;
        import android.media.MediaPlayer;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.Button;
        import android.widget.ImageButton;
        import android.widget.TextView;

import com.example.joni.beaconalerterandroid.Services.DeleteAlertService;
import com.example.joni.beaconalerterandroid.beacons.Beacon;
        import com.example.joni.beaconalerterandroid.beacons.BeaconScanner;
        import com.example.joni.beaconalerterandroid.jsonentities.Alert;
        import com.example.joni.beaconalerterandroid.jsonentities.Settings;
        import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
        import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
        import com.neovisionaries.bluetooth.ble.advertising.EddystoneUID;

        import java.text.SimpleDateFormat;
        import java.util.Date;
        import java.util.List;

/**
 * Created by Joni on 6.3.2017.
 */
public class AlertPopupDialog extends DialogFragment{
    private SharedPreferences prefs;

    private Alert alert;
    private View dialogview;
    private AlertDialog thisDialog;

    private TextView alertTitleView;
    private TextView alertTimeView;
    private ImageButton stopAlertButton;
    private TextView alertProgressView;
    private Button snoozeButton;

    private boolean isSnooze;
    private int snoozeCount;

    private MediaPlayer mp;
    private BeaconScanner scanner;
    private String beaconID;

    static AlertPopupDialog newInstance(String alert) {
        AlertPopupDialog dialog = new AlertPopupDialog();

        Bundle args = new Bundle();
        args.putString("alert", alert);
        dialog.setArguments(args);

        return dialog;
    }

    static AlertPopupDialog newInstance(String alert, int snooze) {
        AlertPopupDialog dialog = new AlertPopupDialog();

        Bundle args = new Bundle();
        args.putString("alert", alert);
        args.putInt("snooze", snooze);
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
            if(getArguments().getInt("snooze") != 0){
                this.isSnooze = true;
                this.snoozeCount = getArguments().getInt("snooze");
            }else{
                this.isSnooze = false;
                this.snoozeCount = 0;
            }
        }
        BluetoothAdapter.getDefaultAdapter().disable();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Setting up views
        prefs = getActivity().getSharedPreferences("com.example.joni.beaconalerterandroid", Context.MODE_PRIVATE);

        alertTitleView = (TextView) dialogview.findViewById(R.id.popupTitleText);
        alertTimeView = (TextView) dialogview.findViewById(R.id.popupTimeView);
        stopAlertButton = (ImageButton) dialogview.findViewById(R.id.closeAlertButton);
        alertProgressView = (TextView) dialogview.findViewById(R.id.popupProgressText);
        snoozeButton = (Button) dialogview.findViewById(R.id.snoozeButton);

        alertTitleView.setText(alert.getTitle());

        String hourMode = prefs.getString(Settings.HOUR_MODE,"24");
        String hourModeFormat;

        if(hourMode.equals("24")){
            hourModeFormat = "HH:mm";
        }else{
            hourModeFormat = "hh:mm a";
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat(hourModeFormat);
        alertTimeView.setText(timeFormat.format(alert.getTime()));

        stopAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Removing alert from the database if it's a one time alert.
                if (!alert.isRepeating()) {
                    //getActivity().getContentResolver().delete(AlertsProvider.ALERTS_CONTENT_URI, AlertsTable.COLUMN_ALERTID + "='" + alert.getId() + "'", null);
                    Intent deleteAlertIntent = new Intent(getActivity(), DeleteAlertService.class);
                    deleteAlertIntent.putExtra("alert", alert.generateJson());
                    getActivity().startService(deleteAlertIntent);
                }
                thisDialog.cancel();
                scanner.stopScanning();
            }
        });
        snoozeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertScheduler scheduler = AlertScheduler.getInstance();
                snoozeCount++;
                int snoozeLength = prefs.getInt(Settings.SNOOZE_LENGTH, 5);
                scheduler.scheduleSnooze(alert, snoozeLength, snoozeCount);
                thisDialog.cancel();
                scanner.stopScanning();
            }
        });
        //Setting button states
        disableStopAlert();
        snoozeButton.setEnabled(checkSnoozeButtonState());
        if(!checkSnoozeButtonState()){
            snoozeButton.setTextColor(Color.GRAY);
        }

        AlertScheduler scheduler = AlertScheduler.getInstance();
        scheduler.cancelSnooze(alert);

        playSound();

        beaconID = prefs.getString(Settings.BEACON_ID, null);
        Log.d("AlertPopupDialog", beaconID );
        //TODO: Beacon checking
        scanner = new BeaconScanner();
        //If beaconID is valid, start scanning for the beacon
        if(beaconID != null) {
            //Converting to uppercase so it matches the uppercase id of the beacon
            beaconID = beaconID.toUpperCase();
            scanner.setScanIntervalMs(150);
            scanner.setOnBeaconFoundListener(new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    //Using a 3rd party library to parse the byte data from the advertisement into classes
                    List<ADStructure> structures = ADPayloadParser.getInstance().parse(scanRecord);
                    for (ADStructure structure : structures) {
                        if (structure instanceof EddystoneUID) {
                            // Eddystone UID
                            EddystoneUID eddyStone = (EddystoneUID) structure;
                            Log.d("AlertPopupDialog",eddyStone.getBeaconIdAsString() +" "+beaconID );
                            Log.d("AlertPopupDialog",rssi+" "+eddyStone.getTxPower());
                            if (eddyStone.getBeaconIdAsString().equals(beaconID)) {
                                if (rssi > eddyStone.getTxPower() - 41) {
                                    Log.d("AlertPopupDialog","Enabling stop alert");
                                    enableStopAlert();
                                }
                            }
                        }
                    }
                }
            });
        }else{
            //If beaconID cant be found, enable stop alert button.
            enableStopAlert();
        }
        scanner.beginScanning();

        //Builds the alert object
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(dialogview);
        alertDialogBuilder.setCancelable(false);

        thisDialog = alertDialogBuilder.create();
        //Returning dialog
        return thisDialog;
    }
    //TODO: Set images
    private void enableStopAlert(){
        if(!stopAlertButton.isEnabled()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("AlertPopupDialog","Enabled stop alert");
                    stopAlertButton.setBackground(getResources().getDrawable(R.drawable.alert_off_enabled));
                    stopAlertButton.setEnabled(true);
                    alertProgressView.setText("Beacon found!");
                    Log.d("AlertPopupDialog", alertProgressView.getText().toString());
                }
            });
        }
    }
    private void disableStopAlert(){

        if(stopAlertButton.isEnabled()) {
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Log.d("AlertPopupDialog","Disabling stop alert");
                    stopAlertButton.setBackground(getResources().getDrawable(R.drawable.alert_off_disabled));
                    stopAlertButton.setEnabled(false);
                    alertProgressView.setText("Scanning for beacon...");
                }
            });
        }
    }

            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                return null;
            }

            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d("AlertPopupDialog", "onDismiss");
                super.onDismiss(dialog);
                stopSound();
            }

            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d("AlertPopupDialog", "onCancel");
                super.onCancel(dialog);
                stopSound();
            }

            private boolean checkSnoozeButtonState() {
                boolean snoozeEnabled = prefs.getBoolean(Settings.SNOOZE_ON, true);
                if (snoozeEnabled) {
                    if (isSnooze) {
                        int maxSnoozes = prefs.getInt(Settings.SNOOZE_AMOUNT, 1);
                        if (snoozeCount < maxSnoozes) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return true;
                    }
                } else {
                    return false;
                }
            }

            private void stopSound() {
                //Stop the mediaplayer and make it null
                if (mp != null) {
                    mp.stop();
                    mp.release();
                    mp = null;
                }
            }

            private void playSound() {
                stopSound();

                float alertVolume = prefs.getFloat(Settings.SOUND_VOLUME, (float) 1.00);
                String selectedSound = prefs.getString(Settings.ALERT_SOUND, "clock");

                int sound = getResources().getIdentifier(selectedSound, "raw", getActivity().getPackageName());
                mp = MediaPlayer.create(getActivity(), sound);
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.reset();
                    }

                });
                mp.setVolume(alertVolume, alertVolume);
                mp.start();
                mp.setLooping(true);
            }


        }