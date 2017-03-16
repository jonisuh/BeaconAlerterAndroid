package com.example.joni.beaconalerterandroid.Services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
import java.util.Set;

/**
 * Created by Joni on 11.3.2017.
 */
public class PostSettingsService extends IntentService {
    private boolean willSync;

    public PostSettingsService() {
        super("PostSettingsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("PostSettingsService", "intent received");
        if (intent != null) {
            try {
                if (intent.getBooleanExtra("forceSync", false)) {
                    willSync = true;
                } else {
                    willSync = getSharedPreferences("com.example.joni.beaconalerterandroid", Context.MODE_PRIVATE).getBoolean(Settings.AUTOMATIC_SYNC, true);
                }

                if (willSync) {
                    String json = Settings.generateJson(getApplicationContext());
                    Log.d("PostSettingsService", json);

                    URL url = new URL(MainActivity.SERVER_URL + "settings");
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
                    Log.d("PostSettingsService", conn.getResponseCode() + " " + conn.getResponseMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}