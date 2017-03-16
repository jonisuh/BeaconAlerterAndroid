package com.example.joni.beaconalerterandroid.Services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.example.joni.beaconalerterandroid.MainActivity;
import com.example.joni.beaconalerterandroid.jsonentities.Alert;
import com.example.joni.beaconalerterandroid.jsonentities.Settings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Joni on 11.3.2017.
 */
public class GetSettingsService extends IntentService {

    public GetSettingsService() {
        super("GetSettingsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("GetSettingsService", "intent received");
        if (intent != null) {
            try {
                URL url = new URL(MainActivity.SERVER_URL + "settings/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                conn.setRequestProperty("Content-Type", "application/json");

                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                Log.d("GetSettingsService", conn.getResponseCode() + " " + conn.getResponseMessage());

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
                String json = br.readLine();
                br.close();

                JSONObject jsonObject = (JSONObject) new JSONArray(json).get(0);
                Settings.consumeJson(jsonObject, getApplicationContext());
                Log.d("GetSettingsService", jsonObject.toString());

            } catch (IOException e) {
                e.printStackTrace();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
