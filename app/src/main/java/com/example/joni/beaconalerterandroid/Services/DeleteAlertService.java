package com.example.joni.beaconalerterandroid.Services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.joni.beaconalerterandroid.AlertScheduler;
import com.example.joni.beaconalerterandroid.AlertsProvider;
import com.example.joni.beaconalerterandroid.AlertsTable;
import com.example.joni.beaconalerterandroid.MainActivity;
import com.example.joni.beaconalerterandroid.jsonentities.Alert;
import com.example.joni.beaconalerterandroid.jsonentities.Settings;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Joni on 11.3.2017.
 */
public class DeleteAlertService extends IntentService {

    public DeleteAlertService() {
        super("DeleteAlertService");
    }
    private boolean willSync;

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("DeleteAlertService", "intent received");
        if (intent != null) {
            String alertString = intent.getStringExtra("alert");
            if(alertString != null) {
                Alert alert = new Alert(alertString);

                if (intent.getBooleanExtra("forceSync", false)) {
                    willSync = true;
                } else {
                    willSync = getSharedPreferences("com.example.joni.beaconalerterandroid", Context.MODE_PRIVATE).getBoolean(Settings.AUTOMATIC_SYNC, true);
                }

                deleteAlert(alert);
            }
        }
    }

    private void deleteAlert(Alert alert){
        try {
            //Cancelling alerts / snoozes
            AlertScheduler scheduler = AlertScheduler.getInstance();
            scheduler.setContext(getApplicationContext());
            scheduler.setManager((AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE));
            scheduler.cancelAlert(alert);
            scheduler.cancelSnooze(alert);

            String alertID = alert.getId();
            getContentResolver().delete(AlertsProvider.ALERTS_CONTENT_URI, AlertsTable.COLUMN_ALERTID + "='" + alertID + "'", null);



            if(willSync) {
                URL url = new URL(MainActivity.SERVER_URL + "alerts/" + alertID);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setDoInput(true);
                conn.setRequestMethod("DELETE");
                conn.connect();

                InputStream in = conn.getInputStream();
                Log.d("DeleteAlertService", conn.getResponseCode() + " " + conn.getResponseMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}