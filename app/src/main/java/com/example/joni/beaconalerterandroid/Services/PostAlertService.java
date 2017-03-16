package com.example.joni.beaconalerterandroid.Services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.example.joni.beaconalerterandroid.AlertScheduler;
import com.example.joni.beaconalerterandroid.AlertsProvider;
import com.example.joni.beaconalerterandroid.MainActivity;
import com.example.joni.beaconalerterandroid.jsonentities.Alert;
import com.example.joni.beaconalerterandroid.jsonentities.Settings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Joni on 11.3.2017.
 */
public class PostAlertService extends IntentService {
    private boolean willSync;
    public PostAlertService() {
        super("PostAlertService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("PostAlertService", "intent received");
        if (intent != null) {

            if(intent.getBooleanExtra("forceSync",false)){
                willSync = true;
            }else{
                willSync = getSharedPreferences("com.example.joni.beaconalerterandroid", Context.MODE_PRIVATE).getBoolean(Settings.AUTOMATIC_SYNC, true);
            }
            String action = intent.getStringExtra("action");
            if(action == null){
                action = "";
            }
            switch(action){
                case "post":
                    Log.d("PostAlertService", "post");
                    String alertJson = intent.getStringExtra("alert");
                    if(alertJson != null) {
                        postAlertToServer(new Alert(alertJson));
                    }
                    break;
                case "post_all":
                    Log.d("PostAlertService","post_all");
                    postAllAlertsToServer();
                    break;
                default:
                    Log.d("PostAlertService","default" );
                    break;
            }


        }
    }

    private void postAlertToServer(Alert alert){
        try {
            //Saving alert in content provider
            ContentValues values = alert.generateContentValue();
            getContentResolver().insert(AlertsProvider.ALERTS_CONTENT_URI, values);

            /*
             * Schedules the new alert.
             * Scheduling has to be done here because the contentprovider will not contain the
             * SQLite ID for this alert
             */
            AlertScheduler scheduler = AlertScheduler.getInstance();
            scheduler.setContext(getApplicationContext());
            scheduler.setManager((AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE));
            scheduler.scheduleAlert(alert);

            //Sending alert to server if user wants to send it to the server or if automatic sync is on
            if(willSync) {
                postJSONToServer(alert.generateJson(), "alerts/");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void postAllAlertsToServer(){
        //We are not concerned with storing values to the content provider as this method should only be used for synchronizing the servers state with the device state
        Cursor alertCursor = getContentResolver().query(AlertsProvider.ALERTS_CONTENT_URI, null, null, null, null);
        JSONArray alertJsonArray = new JSONArray();

        try {
            while (alertCursor.moveToNext()) {
                JSONObject alertJson = new JSONObject(new Alert(alertCursor).generateJson());
                alertJsonArray.put(alertJson);
            }

            JSONObject alertsJsonObject = new JSONObject();
            alertsJsonObject.put("alerts", alertJsonArray);

            String jsonArrayString = alertsJsonObject.toString();
            Log.d("PostAlertService", jsonArrayString);
            postJSONToServer(jsonArrayString, "alerts/all");

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            alertCursor.close();
        }
    }



    private void postJSONToServer(String json, String urlSuffix)  throws IOException {
        URL url = new URL(MainActivity.SERVER_URL + urlSuffix);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestMethod("POST");
        conn.connect();

        OutputStream outputStream = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        writer.write(json);
        writer.close();
        outputStream.close();

        InputStream in = conn.getInputStream();
        Log.d("PostAlertService", conn.getResponseCode() + " " + conn.getResponseMessage());
    }
}
