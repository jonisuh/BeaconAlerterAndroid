package com.example.joni.beaconalerterandroid;

/**
 * Created by Joni on 26.2.2017.
 */

public class AlertsTable {
    public static final String TABLE_ALERTS = "alerts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_REPEATING= "repeating";
    public static final String COLUMN_ISENABLED = "isenabled";
    public static final String COLUMN_DAYS = "days";
    public static final String COLUMN_ALERTID = "alertid";

    public static final String DATABASE_CREATE = "create table if not exists "
            + TABLE_ALERTS
            + " ("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TIME + " text,"
            + COLUMN_REPEATING + " integer,"
            + COLUMN_ISENABLED + " integer,"
            + COLUMN_DAYS + " text,"
            + COLUMN_TITLE + " text,"
            + COLUMN_ALERTID + " text"
            + ");";

}


