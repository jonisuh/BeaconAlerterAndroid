package com.example.joni.beaconalerterandroid;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "alertsLocalDataBase";
    static final int DATABASE_VERSION = 1;
    static final String TABLE_ALERTS = AlertsTable.TABLE_ALERTS;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("DBHelper", "DBHelper()");
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AlertsTable.DATABASE_CREATE);
        Log.d("DBHelper", "onCreate");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DBHelper", "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALERTS);
        onCreate(db);
    }

}