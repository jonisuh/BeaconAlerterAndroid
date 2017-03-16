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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.joni.beaconalerterandroid.jsonentities.Settings;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Joni on 8.3.2017.
 */
public class DateFormatDialog extends DialogFragment {

    private View dialogview;
    private AlertDialog thisDialog;

    ArrayAdapter<String> adapter;

    private Date timeNow;
    private SharedPreferences prefs;
    private String[] dateFormats;
    private String separator;

    //Views
    private NumberPicker dateFormatPicker;
    private Button dotButton;
    private Button spaceButton;
    private Button slashButton;

    static DateFormatDialog newInstance() {
        DateFormatDialog dialog = new DateFormatDialog();
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater li = LayoutInflater.from(getActivity());
        dialogview = li.inflate(R.layout.dateformat_dialog, null);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Setting up views
        prefs = getActivity().getSharedPreferences("com.example.joni.beaconalerterandroid", Context.MODE_PRIVATE);

        timeNow = new Date();
        this.separator = ".";

        dateFormatPicker = (NumberPicker) dialogview.findViewById(R.id.dateFormatPicker);
        dateFormatPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        generateDateFormats(this.separator);

        dotButton = (Button) dialogview.findViewById(R.id.dotButton);
        dotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                separator = ".";
                generateDateFormats(separator);
            }
        });
        spaceButton = (Button) dialogview.findViewById(R.id.spaceButton);
        spaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                separator = " ";
                generateDateFormats(separator);
            }
        });
        slashButton = (Button) dialogview.findViewById(R.id.slashButton);
        slashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                separator = "/";
                generateDateFormats(separator);
            }
        });

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
                .setPositiveButton("Select",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String selectedFormat = dateFormats[dateFormatPicker.getValue()];
                                prefs.edit().putString(Settings.DATE_FORMAT, selectedFormat).apply();
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

    private void generateDateFormats(String separator){
        ArrayList<String> dateFormatsEU = new ArrayList<>();
        ArrayList<String> dateFormatsUS = new ArrayList<>();

        for(int i=1; i<=4; i++){
            String months = new String(new char[i]).replace("\0", "M");
            dateFormatsEU.add("dd"+separator+months+separator+"yyyy");
            dateFormatsUS.add(months+separator+"dd"+separator+"yyyy");
        }
        dateFormatsEU.addAll(dateFormatsUS);
        this.dateFormats = dateFormatsEU.toArray(new String[0]);
        updateFormatPicker();
    }

    private void updateFormatPicker(){
        dateFormatPicker.setDisplayedValues(null);
        dateFormatPicker.setMinValue(0);
        dateFormatPicker.setMaxValue(dateFormats.length - 1);
        dateFormatPicker.setWrapSelectorWheel(false);

        String[] formattedTimes = new String[dateFormats.length];
        for(int i=0; i<dateFormats.length;i++){
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormats[i]);
            formattedTimes[i] = formatter.format(timeNow);
        }
        dateFormatPicker.setDisplayedValues(formattedTimes);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

}