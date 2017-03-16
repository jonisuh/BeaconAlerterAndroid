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
import com.example.joni.beaconalerterandroid.AlertsTable;
import com.example.joni.beaconalerterandroid.MainActivity;
import com.example.joni.beaconalerterandroid.jsonentities.Alert;
import com.example.joni.beaconalerterandroid.jsonentities.Settings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Joni on 11.3.2017.
 */
public class GetAlertsService extends IntentService {

    public GetAlertsService() {
        super("GetAlertsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("GetAlertsService", "intent received");
        if (intent != null) {
            String action = intent.getStringExtra("action");
            if(action == null){
                action = "";
            }
            switch(action){
                case "partial_sync":
                    Log.d("GetAlertsService", "partial_sync");
                    partialSynchronization();
                    break;
                case "full_sync":
                    Log.d("GetAlertsService", "full_sync");
                    fullSynchronization();
                    break;
                default:
                    Log.d("GetAlertsService","default" );
                    break;
            }
        }
    }

    private void partialSynchronization(){
        ArrayList<Alert> alertsFromServer = getAlertsFromServer();
        if(alertsFromServer !=null){
            //Getting all current alerts
            ArrayList<Alert> allAlerts = new ArrayList<>();
            Cursor alertCursor = getContentResolver().query(AlertsProvider.ALERTS_CONTENT_URI, null,null, null, null);


            //Parsing cursors into alerts
            while (alertCursor.moveToNext()) {
                allAlerts.add(new Alert(alertCursor));
            }

            AlertScheduler scheduler = AlertScheduler.getInstance();
            //Ensuring the scheduler has a reference to the context and the alarm manager
            scheduler.setContext(getApplicationContext());
            scheduler.setManager((AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE));

            //Canceling all current alerts
            for (Alert alert : allAlerts) {
                scheduler.cancelAlert(alert);
                scheduler.cancelSnooze(alert);
            }
            alertCursor.close();
            getContentResolver().delete(AlertsProvider.ALERTS_CONTENT_URI,null,null);

            for(Alert newAlert : alertsFromServer){
                ContentValues values = newAlert.generateContentValue();
                getContentResolver().insert(AlertsProvider.ALERTS_CONTENT_URI, values);
                if(newAlert.isEnabled()) {
                    scheduler.scheduleAlert(newAlert);
                }
            }
        }
    }

    private void fullSynchronization(){
        ArrayList<Alert> alertsFromServer = getAlertsFromServer();
        if(alertsFromServer !=null){
            //Initializing scheduler
            AlertScheduler scheduler = AlertScheduler.getInstance();
            scheduler.setContext(getApplicationContext());
            scheduler.setManager((AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE));

            for(Alert alert : alertsFromServer){
                Cursor alertCursor = getContentResolver().query(AlertsProvider.ALERTS_CONTENT_URI, null, AlertsTable.COLUMN_ALERTID + "='" + alert.getId() + "'", null, null);

                ContentValues values = alert.generateContentValue();

                if(alertCursor.getCount() == 1){
                    getContentResolver().update(AlertsProvider.ALERTS_CONTENT_URI, values, AlertsTable.COLUMN_ALERTID + "='" + alert.getId() + "'", null);
                    if(alert.isEnabled()) {
                        scheduler.rescheduleAlert(alert);
                    }else{
                        scheduler.cancelAlert(alert);
                        scheduler.cancelSnooze(alert);
                    }
                }else{
                    getContentResolver().insert(AlertsProvider.ALERTS_CONTENT_URI, values);
                    if(alert.isEnabled()) {
                        scheduler.scheduleAlert(alert);
                    }
                }
                alertCursor.close();
            }

            //Posting all alerts to server to update server state
            Intent postService = new Intent(getApplicationContext(), PostAlertService.class);
            postService.putExtra("action", "post_all");
            startService(postService);
        }
    }

    private ArrayList<Alert> getAlertsFromServer(){
        try {
            URL url = new URL(MainActivity.SERVER_URL + "alerts/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            conn.setRequestProperty("Content-Type", "application/json");

            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            Log.d("GetAlertsService", conn.getResponseCode() + " " + conn.getResponseMessage());

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
            String json = br.readLine();
            br.close();

            if(json != null) {
                JSONArray alertJsonArray = new JSONArray(json);
                ArrayList<Alert> alertArraylist = new ArrayList<>();

                // Log.d("GetAlertsService", json);
                for (int i = 0; i < alertJsonArray.length(); i++) {
                    alertArraylist.add(new Alert(alertJsonArray.get(i).toString()));
                }

                return alertArraylist;
            }else{
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}