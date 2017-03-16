package com.example.joni.beaconalerterandroid.jsonentities;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Joni on 25.2.2017.
 */
public class Settings {
    public static final String HOUR_MODE = "hourMode";
    public static final String DATE_FORMAT = "dateFormat";
    public static final String SNOOZE_ON = "snoozeOn";
    public static final String SNOOZE_LENGTH = "snoozeLength";
    public static final String SNOOZE_AMOUNT= "snoozeAmount";
    public static final String SOUND_VOLUME = "soundVolume";
    public static final String ALERT_SOUND = "alertSound";
    public static final String AUTOMATIC_SYNC = "automaticSync";
    public static final String BEACON_ID = "beaconID";

    public static String generateJson(Context context){
        JSONObject json = new JSONObject();
        SharedPreferences prefs = context.getSharedPreferences("com.example.joni.beaconalerterandroid", Context.MODE_PRIVATE);

        try {
            json.put(HOUR_MODE,prefs.getString(HOUR_MODE,"24"));
            json.put(DATE_FORMAT,prefs.getString(DATE_FORMAT,"dd/MMM/yyyy"));
            json.put(SNOOZE_ON,prefs.getBoolean(SNOOZE_ON,true));
            json.put(SNOOZE_LENGTH,prefs.getInt(SNOOZE_LENGTH,1));
            json.put(SNOOZE_AMOUNT,prefs.getInt(SNOOZE_AMOUNT,5));
            json.put(SOUND_VOLUME,prefs.getFloat(SOUND_VOLUME,(float) 1.00));
            json.put(ALERT_SOUND,prefs.getString(ALERT_SOUND,"clock"));
            json.put(AUTOMATIC_SYNC,prefs.getBoolean(AUTOMATIC_SYNC,true));
            json.put(BEACON_ID,prefs.getString(BEACON_ID,"No beacon set."));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json.toString();

    }

    public static void consumeJson(JSONObject alertJson, Context context){
        try {
            SharedPreferences prefs = context.getSharedPreferences("com.example.joni.beaconalerterandroid", Context.MODE_PRIVATE);

            prefs.edit().putString(HOUR_MODE, alertJson.getString(HOUR_MODE)).apply();
            prefs.edit().putString(DATE_FORMAT, alertJson.getString(DATE_FORMAT)).apply();
            prefs.edit().putBoolean(SNOOZE_ON, alertJson.getBoolean(SNOOZE_ON)).apply();
            prefs.edit().putInt(SNOOZE_LENGTH, alertJson.getInt(SNOOZE_LENGTH)).apply();
            prefs.edit().putInt(SNOOZE_AMOUNT, alertJson.getInt(SNOOZE_AMOUNT)).apply();
            prefs.edit().putFloat(SOUND_VOLUME, (float) alertJson.getDouble(SOUND_VOLUME)).apply();
            prefs.edit().putString(ALERT_SOUND, alertJson.getString(ALERT_SOUND)).apply();
            prefs.edit().putBoolean(AUTOMATIC_SYNC, alertJson.getBoolean(AUTOMATIC_SYNC)).apply();
            prefs.edit().putString(BEACON_ID, alertJson.getString(BEACON_ID)).apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
