package com.example.joni.beaconalerterandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.joni.beaconalerterandroid.jsonentities.Settings;

import java.lang.reflect.Field;


/**
 * Created by Joni on 8.3.2017.
 */
public class AlertSoundDialog  extends DialogFragment {

    private View dialogview;
    private AlertDialog thisDialog;
    private String[] soundNames;
    private ListView alertSoundsListView;
    private ArrayAdapter<String> arrayAdapter;
    private String selectedSound;
    private SharedPreferences prefs;
    private MediaPlayer mp;

    private float alertVolume;

    static AlertSoundDialog newInstance() {
        AlertSoundDialog dialog = new AlertSoundDialog();
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater li = LayoutInflater.from(getActivity());
        dialogview = li.inflate(R.layout.alertsound_dialog, null);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Setting up views

        prefs = getActivity().getSharedPreferences("com.example.joni.beaconalerterandroid", Context.MODE_PRIVATE);
        selectedSound = prefs.getString(Settings.ALERT_SOUND, "clock");

        alertVolume = prefs.getFloat(Settings.SOUND_VOLUME, (float) 1.00);

        Field[] fields=R.raw.class.getFields();
        soundNames = new String[fields.length];
        for(int count=0; count < fields.length; count++){
            soundNames[count] = fields[count].getName();
        }

        arrayAdapter = new SoundListAdapter(getActivity(), soundNames);

        alertSoundsListView = (ListView) dialogview.findViewById(R.id.alertSoundListView);
        alertSoundsListView.setAdapter(arrayAdapter);

        //Builds the alert object
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(dialogview);
        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                stopSound();
                                dialog.cancel();
                            }
                        })
                .setPositiveButton("Select",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                stopSound();
                                prefs.edit().putString(Settings.ALERT_SOUND, selectedSound).apply();
                                ((SettingsActivity) getActivity()).updateViews();
                            }
                        });

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

    @Override
    public void onDismiss(DialogInterface dialog){
        super.onDismiss(dialog);
        stopSound();
    }

    @Override
    public void onCancel(DialogInterface dialog){
        super.onCancel(dialog);
        stopSound();
    }

    private void stopSound(){
        //Stop the mediaplayer and make it null
        if(mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    private void playSound(){
        stopSound();

        int sound = getResources().getIdentifier(selectedSound , "raw", getActivity().getPackageName());
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

    class SoundListAdapter extends ArrayAdapter<String> {
        SoundListAdapter(Context context, String[] sounds) {
            super(context, R.layout.soundlist_cell, sounds);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row;
            final int rowPosition = position;
            final ViewGroup parentView = parent;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.soundlist_cell, parent, false);
            } else {
                row = convertView;
            }
            TextView soundNameView = (TextView) row.findViewById(R.id.soundNameView);
            soundNameView.setText(soundNames[rowPosition]);

            row.setBackgroundColor(Color.WHITE);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 0; i < parentView.getChildCount(); i++) {
                        parentView.getChildAt(i).setBackgroundColor(Color.WHITE);
                    }
                    v.setBackgroundColor(0xFFC4CCFB);
                    selectedSound = soundNames[rowPosition];

                    playSound();
                    Log.d("AlertSoundDialog", selectedSound);
                }
            });

            return row;
        }
    }


}