package com.example.joni.beaconalerterandroid.Services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.example.joni.beaconalerterandroid.AlertScheduler;
import com.example.joni.beaconalerterandroid.AlertsProvider;
import com.example.joni.beaconalerterandroid.AlertsTable;
import com.example.joni.beaconalerterandroid.jsonentities.Alert;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Joni on 7.3.2017.
 */
public class RescheduleAlertsService extends IntentService {

    public RescheduleAlertsService() {
        super("RescheduleAlertsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d("RescheduleAlertsService", "Scheduling alerts");
            ArrayList<Alert> allAlerts = new ArrayList<>();

            Cursor alertCursor = getContentResolver().query(AlertsProvider.ALERTS_CONTENT_URI, null,null, null, null);

            try {
                while (alertCursor.moveToNext()) {
                    allAlerts.add(new Alert(alertCursor));
                }
                AlertScheduler scheduler = AlertScheduler.getInstance();
                //Ensuring the scheduler has a reference to the context and the alarm manager
                scheduler.setContext(getApplicationContext());
                scheduler.setManager((AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE));

                for(Alert alert : allAlerts){
                    if(alert.isEnabled()){
                        scheduler.scheduleAlert(alert);
                    }
                }

            } finally {
                alertCursor.close();
            }
        }
    }
}