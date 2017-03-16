package com.example.joni.beaconalerterandroid.Services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
public class UpdateAlertService extends IntentService {
    private boolean willSync;
    public UpdateAlertService() {
        super("UpdateAlertService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("UpdateAlertService", "intent received");
        if (intent != null) {
            String alertString = intent.getStringExtra("alert");
            if(alertString != null) {
                Alert alert = new Alert(alertString);

                if (intent.getBooleanExtra("forceSync", false)) {
                    willSync = true;
                } else {
                    willSync = getSharedPreferences("com.example.joni.beaconalerterandroid", Context.MODE_PRIVATE).getBoolean(Settings.AUTOMATIC_SYNC, true);
                }

                updateAlert(alert);
            }
        }
    }

    private void updateAlert(Alert alert){
        try {
            String alertID = alert.getId();

            ContentValues values = alert.generateContentValue();
            getContentResolver().update(AlertsProvider.ALERTS_CONTENT_URI, values, AlertsTable.COLUMN_ALERTID + "='" + alertID + "'", null);

            if(willSync) {
                URL url = new URL(MainActivity.SERVER_URL + "alerts/" + alertID);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestMethod("PUT");
                conn.connect();

                OutputStream outputStream = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                writer.write(alert.generateJson());
                writer.close();
                outputStream.close();

                InputStream in = conn.getInputStream();
                Log.d("UpdateAlertService", conn.getResponseCode() + " " + conn.getResponseMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}