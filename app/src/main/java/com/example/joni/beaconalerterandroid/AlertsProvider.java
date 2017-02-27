package com.example.joni.beaconalerterandroid;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.example.joni.beaconalerterandroid.jsonentities.Alert;

/**
 * Created by Joni on 26.2.2017.
 */
public class AlertsProvider extends ContentProvider {
    private SQLiteDatabase thisDB;
    private DBHelper helper;

    public static final String AUTHORITY = "com.example.joni.beaconalerterandroid.AlertsProvider";
    public static final String PROVIDER_NAME = "com.example.joni.beaconalerterandroid.AlertsProvider";
    public static final Uri ALERTS_CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/alerts");

    private static final int ALERTS = 1;
    private static final int ALERT_ID = 2;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "alerts", ALERTS);
        uriMatcher.addURI(PROVIDER_NAME, "alerts/#", ALERT_ID);
    }

    public boolean onCreate() {
        Log.d("provider", " in provider onCreate");
        Context c = getContext();
        helper = new DBHelper(c);
        thisDB = helper.getReadableDatabase();
        if (thisDB == null) {
            Log.d("provider", "null");
            return false;
        } else {
            Log.d("provider", "full");
            return true;
        }
    }

    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ALERTS:
                return "vnd.android.cursor.dir/vnd.example.joni.beaconalerterandroid ";
            case ALERT_ID:
                return "vnd.android.cursor.item/vnd.example.joni.beaconalerterandroid ";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        int rowsDeleted = 0;
        String id;
        switch (uriType) {
            case ALERTS:
                rowsDeleted = thisDB.delete(DBHelper.TABLE_ALERTS, selection, selectionArgs);
                break;
            case ALERT_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = thisDB.delete(DBHelper.TABLE_ALERTS, AlertsTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = thisDB.delete(DBHelper.TABLE_ALERTS, AlertsTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;

    }

    public Uri insert(Uri uri, ContentValues values) {
        int uriType = uriMatcher.match(uri);
        Uri _uri = null;

        long id = 0;
        switch (uriType) {
            case ALERTS:
                id = thisDB.insertWithOnConflict(DBHelper.TABLE_ALERTS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                _uri = ContentUris.withAppendedId(ALERTS_CONTENT_URI, id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return _uri;
    }


    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();

        int uriType = uriMatcher.match(uri);

        switch (uriType) {
            case ALERTS:
                sqlBuilder.setTables(DBHelper.TABLE_ALERTS);
                break;
            case ALERT_ID:
                sqlBuilder.setTables(DBHelper.TABLE_ALERTS);
                sqlBuilder.appendWhere(AlertsTable.COLUMN_ID + " = " + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Cursor cur = sqlBuilder.query(thisDB, projection, selection, selectionArgs, null, null, sortOrder);
        cur.setNotificationUri(getContext().getContentResolver(), uri);

        return cur;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        int rowsUpdated = 0;
        String id;
        switch (uriType) {
            case ALERTS:
                rowsUpdated = thisDB.update(DBHelper.TABLE_ALERTS, values, selection, selectionArgs);
                break;
            case ALERT_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = thisDB.update(DBHelper.TABLE_ALERTS, values, AlertsTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = thisDB.update(DBHelper.TABLE_ALERTS, values, AlertsTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}