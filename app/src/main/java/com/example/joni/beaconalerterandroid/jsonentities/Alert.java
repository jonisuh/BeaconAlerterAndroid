package com.example.joni.beaconalerterandroid.jsonentities;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.example.joni.beaconalerterandroid.AlertsTable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Joni on 25.2.2017.
 */
public class Alert {
    private String title;
    private Date time;
    private boolean repeating;
    private boolean isEnabled;
    private String id;
    private boolean[] days;
    private int sqliteId;

    public Alert() {

    }

    public Alert(String title, Date time, boolean repeating, boolean isEnabled, String id, boolean[] days) {
        this.title = title;
        this.time = time;
        this.repeating = repeating;
        this.isEnabled = isEnabled;
        this.id = id;
        this.days = days;
    }

    public Alert(String json){
        try {
            JSONObject alertJson = new JSONObject(json);
            this.title = alertJson.getString("title");
            this.time = stringToDate(alertJson.getString("time"));
            this.repeating = alertJson.getBoolean("repeating");
            this.isEnabled = alertJson.getBoolean("isEnabled");
            this.id = alertJson.getString("id");

            JSONObject days = alertJson.getJSONObject("days");
            this.days = new boolean[7];
            this.days[0] = days.getBoolean("mon");
            this.days[1] = days.getBoolean("tue");
            this.days[2] = days.getBoolean("wed");
            this.days[3] = days.getBoolean("thu");
            this.days[4] = days.getBoolean("fri");
            this.days[5] = days.getBoolean("sat");
            this.days[6] = days.getBoolean("sun");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Alert(Cursor cursor){
        this.title = cursor.getString(cursor.getColumnIndex(AlertsTable.COLUMN_TITLE));
        this.time = new Date(cursor.getLong(cursor.getColumnIndex(AlertsTable.COLUMN_TIME)));
        this.repeating = (cursor.getInt(cursor.getColumnIndex(AlertsTable.COLUMN_REPEATING)) != 0);
        this.isEnabled = (cursor.getInt(cursor.getColumnIndex(AlertsTable.COLUMN_ISENABLED)) != 0);
        this.id = cursor.getString(cursor.getColumnIndex(AlertsTable.COLUMN_ALERTID));
        this.days = convertStringToDays(cursor.getString(cursor.getColumnIndex(AlertsTable.COLUMN_DAYS)));
        this.sqliteId = cursor.getInt(cursor.getColumnIndex(AlertsTable.COLUMN_ID));
    }

    public ContentValues generateContentValue(){
        ContentValues values = new ContentValues();

        values.put(AlertsTable.COLUMN_TITLE, this.title);
        values.put(AlertsTable.COLUMN_TIME, this.time.getTime());
        values.put(AlertsTable.COLUMN_REPEATING, this.repeating);
        values.put(AlertsTable.COLUMN_ISENABLED, this.isEnabled);
        values.put(AlertsTable.COLUMN_ALERTID, this.id);
        values.put(AlertsTable.COLUMN_DAYS, convertDaysToString(this.days));

        return values;
    }

    public String generateJson(){
        JSONObject json = new JSONObject();
        try {
            json.put("title",this.title);
            json.put("time",dateToString(this.time));
            json.put("repeating",this.repeating);
            json.put("isEnabled",this.isEnabled);
            json.put("id",this.id);

            JSONObject daysJson = new JSONObject();
            daysJson.put("mon",days[0]);
            daysJson.put("tue",days[1]);
            daysJson.put("wed",days[2]);
            daysJson.put("thu",days[3]);
            daysJson.put("fri",days[4]);
            daysJson.put("sat",days[5]);
            daysJson.put("sun",days[6]);

            json.put("days", daysJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json.toString();
    }

    public Date stringToDate(String date){
        //yyyy-MM-dd'T'HH:mm:ss.SSSZ
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.ENGLISH);
            Date dateFromString = format.parse(date);

            return dateFromString;
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d("CreateAlertDialog", e.toString());
            return null;
        }
    }

    public String dateToString(Date date){
        //yyyy-MM-dd'T'HH:mm:ssZZZZZ
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.ENGLISH);
        String stringFromDate = format.format(date);
        return stringFromDate;
    }


    public boolean[] getDays() {return days;}

    public void setDays(boolean[] days) {this.days = days;}

    public String getId() {return id;}

    public void setId(String id) {this.id = id;}

    public boolean isEnabled() {return isEnabled;}

    public void setIsEnabled(boolean isEnabled) {this.isEnabled = isEnabled;}

    public boolean isRepeating() {return repeating;}

    public void setRepeating(boolean repeating) {this.repeating = repeating;}

    public Date getTime() {return time;}

    public void setTime(Date time) {this.time = time;}

    public String getTitle() {return title;}

    public void setTitle(String title) {this.title = title;}

    public int getSqliteId() {return sqliteId;}

    public void setSqliteIdt(int sqliteId) {this.sqliteId = sqliteId;}

    public String convertDaysToString(boolean[] days){
        String daysAsString = "";

        for(int i = 0; i<days.length; i++) {
            if (days[i]) {
                daysAsString = daysAsString+"1";
            }else{
                daysAsString = daysAsString+"0";
            }
        }
        return daysAsString;
    }

    public boolean[] convertStringToDays(String days){
        char[] daysAsCharArray = days.toCharArray();
        boolean[] daysAsArray = new boolean[daysAsCharArray.length];

        for(int i=0; i<daysAsCharArray.length;i++){
            if(daysAsCharArray[i] == '1'){
                daysAsArray[i] = true;
            }else{
                daysAsArray[i] = false;
            }
        }
        return daysAsArray;
    }

}
