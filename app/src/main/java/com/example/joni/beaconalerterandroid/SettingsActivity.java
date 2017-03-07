package com.example.joni.beaconalerterandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
            }
        });

        dateFormatButton = (Button) findViewById(R.id.dateFormatButton);
        dateFormatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        enableSnoozeSwitch = (Switch) findViewById(R.id.enableSnoozeSwitch);
        enableSnoozeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });

        snoozeLengthPlus = (Button) findViewById(R.id.snoozeLengthPlus);
        snoozeLengthPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        snoozeLengthMinus = (Button) findViewById(R.id.snoozeLengthMinus);
        snoozeLengthMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        maxSnoozesPlus = (Button) findViewById(R.id.maxSnoozesPlus);
        maxSnoozesPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        maxSnoozesMinus = (Button) findViewById(R.id.maxSnoozesMinus);
        maxSnoozesMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        maxSnoozesText = (TextView) findViewById(R.id.maxSnoozesText);
        snoozeLengthText = (TextView) findViewById(R.id.snoozeLengthText);

        alertVolumeSlider = (SeekBar) findViewById(R.id.volumeSlider);
        alertVolumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //TODO: Use this
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
            }
        });

        enableSyncSwitch = (Switch) findViewById(R.id.automaticSyncSwitch);
        enableSyncSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });

        beaconIDButton = (Button) findViewById(R.id.beaconIDButton);
        beaconIDButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    public void updateViews(){

    }
}
