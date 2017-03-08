package com.example.joni.beaconalerterandroid;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.joni.beaconalerterandroid.jsonentities.Settings;

public class SettingsActivity extends AppCompatActivity {
    private Button backToAlertsButton;

    private Button hourModeButton;
    private Button dateFormatButton;

    private Switch enableSnoozeSwitch;
    private Button snoozeLengthPlus;
    private Button snoozeLengthMinus;
    private Button maxSnoozesPlus;
    private Button maxSnoozesMinus;
    private TextView maxSnoozesText;
    private TextView snoozeLengthText;

    private SeekBar alertVolumeSlider;
    private Button alertSoundButton;

    private Switch enableSyncSwitch;

    private Button beaconIDButton;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("com.example.joni.beaconalerterandroid", Context.MODE_PRIVATE);


        backToAlertsButton = (Button) findViewById(R.id.backToAlertsButton);
        backToAlertsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openMainActivityIntent = new Intent(SettingsActivity.this, MainActivity.class);
                openMainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                SettingsActivity.this.startActivity(openMainActivityIntent);
                //TODO: Send to server here
            }
        });

        hourModeButton = (Button) findViewById(R.id.hourModeButton);
        hourModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentHourMode = hourModeButton.getText().toString();
                String newHourMode;
                if(currentHourMode.equals("12")){
                    newHourMode = "24";
                }else{
                    newHourMode = "12";
                }

                hourModeButton.setText(newHourMode);
                prefs.edit().putString(Settings.HOUR_MODE, newHourMode).apply();
            }
        });

        dateFormatButton = (Button) findViewById(R.id.dateFormatButton);
        dateFormatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = AlertSoundDialog.newInstance();
                dialog.show(getFragmentManager(), "DateFormatDialog");
            }
        });

        enableSnoozeSwitch = (Switch) findViewById(R.id.enableSnoozeSwitch);
        enableSnoozeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean(Settings.SNOOZE_ON,isChecked).apply();
                Log.d("SettingsActivity", "" + prefs.getBoolean("snoozeOn", false));
            }
        });

        snoozeLengthPlus = (Button) findViewById(R.id.snoozeLengthPlus);
        snoozeLengthPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentLength = prefs.getInt(Settings.SNOOZE_LENGTH,5);
                Log.d("SettingsActivity", "currentLength " + currentLength);
                int newLength = currentLength+1;
                Log.d("SettingsActivity", "newLength " + currentLength);
                if(newLength <= 15){
                    Log.d("SettingsActivity", "newLength was less");
                    snoozeLengthText.setText(newLength + " minutes");
                    prefs.edit().putInt(Settings.SNOOZE_LENGTH, newLength).apply();
                    Log.d("SettingsActivity", ""+prefs.getInt(Settings.SNOOZE_LENGTH,5));
                }
            }
        });

        snoozeLengthMinus = (Button) findViewById(R.id.snoozeLengthMinus);
        snoozeLengthMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentLength = prefs.getInt(Settings.SNOOZE_LENGTH,5);
                int newLength = currentLength-1;
                if(newLength >= 1){
                    snoozeLengthText.setText(newLength+" minutes");
                    prefs.edit().putInt(Settings.SNOOZE_LENGTH, newLength).apply();
                }
            }
        });

        maxSnoozesPlus = (Button) findViewById(R.id.maxSnoozesPlus);
        maxSnoozesPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentAmount = prefs.getInt(Settings.SNOOZE_AMOUNT,1);
                int newAmount = currentAmount+1;
                if(newAmount <= 3){
                    maxSnoozesText.setText(""+newAmount);
                    prefs.edit().putInt(Settings.SNOOZE_AMOUNT, newAmount).apply();
                }
            }
        });

        maxSnoozesMinus = (Button) findViewById(R.id.maxSnoozesMinus);
        maxSnoozesMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentAmount = prefs.getInt(Settings.SNOOZE_AMOUNT,1);
                int newAmount = currentAmount-1;
                if(newAmount >= 1){
                    maxSnoozesText.setText(""+newAmount);
                    prefs.edit().putInt(Settings.SNOOZE_AMOUNT, newAmount).apply();
                }
            }
        });

        maxSnoozesText = (TextView) findViewById(R.id.maxSnoozesText);
        snoozeLengthText = (TextView) findViewById(R.id.snoozeLengthText);

        alertVolumeSlider = (SeekBar) findViewById(R.id.volumeSlider);
        alertVolumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                prefs.edit().putFloat(Settings.SOUND_VOLUME, (float)progress/100).apply();
                Log.d("SettingsActivity", "" + progress +"\n"+(float)progress/100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        alertSoundButton = (Button) findViewById(R.id.alertSoundButton);
        alertSoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = AlertSoundDialog.newInstance();
                dialog.show(getFragmentManager(), "AlertSoundDialog");
            }
        });

        enableSyncSwitch = (Switch) findViewById(R.id.automaticSyncSwitch);
        enableSyncSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean(Settings.AUTOMATIC_SYNC, isChecked).apply();
                Log.d("SettingsActivity", ""+prefs.getBoolean("automaticSync", false));
            }
        });

        beaconIDButton = (Button) findViewById(R.id.beaconIDButton);
        beaconIDButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        updateViews();
    }

    public void updateViews(){
        hourModeButton.setText(prefs.getString(Settings.HOUR_MODE,"24"));
        dateFormatButton.setText(prefs.getString(Settings.DATE_FORMAT, "dd/MMM/yyyy"));

        enableSnoozeSwitch.setChecked(prefs.getBoolean(Settings.SNOOZE_ON, true));

        maxSnoozesText.setText("" + prefs.getInt(Settings.SNOOZE_AMOUNT, 1));
        snoozeLengthText.setText(prefs.getInt(Settings.SNOOZE_LENGTH,5)+" minutes");
        alertVolumeSlider.setProgress((int) (prefs.getFloat(Settings.SOUND_VOLUME, (float) 1.00) * 100));
        Log.d("SettingsActivity", ""+(int) (prefs.getFloat(Settings.SOUND_VOLUME, (float) 1.00) * 100));

        alertSoundButton.setText(prefs.getString(Settings.ALERT_SOUND, "clock"));
        enableSyncSwitch.setChecked(prefs.getBoolean(Settings.AUTOMATIC_SYNC, true));
        beaconIDButton.setText(prefs.getString(Settings.BEACON_ID,"No beacon set."));

    }
}
