package com.example.joni.beaconalerterandroid.jsonentities;

/**
 * Created by Joni on 25.2.2017.
 */
public class Settings {
    private String hourMode;
    private String dateFormat;
    private boolean snoozeOn;
    private int snoozeLength;
    private int snoozeAmount;
    private float soundVolume;
    private String alertSound;
    private boolean automaticSync;
    private String beaconID;

    public static final String HOUR_MODE = "hourMode";
    public static final String DATE_FORMAT = "dateFormat";
    public static final String SNOOZE_ON = "snoozeOn";
    public static final String SNOOZE_LENGTH = "snoozeLength";
    public static final String SNOOZE_AMOUNT= "snoozeAmount";
    public static final String SOUND_VOLUME = "soundVolume";
    public static final String ALERT_SOUND = "alertSound";
    public static final String AUTOMATIC_SYNC = "automaticSync";
    public static final String BEACON_ID = "beaconID";

    public static String generateJson(){
        return "";
    }

}
