package com.example.joni.beaconalerterandroid.Services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joni.beaconalerterandroid.AlertScheduler;
import com.example.joni.beaconalerterandroid.AlertsProvider;
import com.example.joni.beaconalerterandroid.jsonentities.Alert;
import com.example.joni.beaconalerterandroid.jsonentities.Settings;

import java.util.ArrayList;

/**
 * Created by Joni on 12.3.2017.
 */
public class SynchronizationService extends IntentService {
    private boolean willSync;
    public SynchronizationService() {
        super("SynchronizationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            Log.d("SynchronizationService", "Synchronization started");
            boolean forceSync = intent.getBooleanExtra("forceSync",false);
            if(forceSync){
                willSync = true;
            }else{
                willSync = getSharedPreferences("com.example.joni.beaconalerterandroid", Context.MODE_PRIVATE).getBoolean(Settings.AUTOMATIC_SYNC, true);
            }

            if(willSync) {
                //Synchronizing alerts
                Intent alertsService = new Intent(getApplicationContext(), GetAlertsService.class);
                //alertsService.putExtra("action", "full_sync");
                alertsService.putExtra("action","partial_sync");
                startService(alertsService);

                //Synchronizing alerts
                Intent settingsService = new Intent(getApplicationContext(), GetSettingsService.class);
                startService(settingsService);

                if(forceSync){
                    //Toast needs to be ran on main thread.
                    Handler mHandler = new Handler(getMainLooper());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            String toastMessage = "Synchronization started. \nSettings and alerts may get modified on your device during the synchronization";
                            Toast t = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG);
                            TextView v = (TextView) t.getView().findViewById(android.R.id.message);
                            if( v != null) v.setGravity(Gravity.CENTER);
                            t.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                            t.show();
                        }
                    });
                    }
                }
        }
    }
}