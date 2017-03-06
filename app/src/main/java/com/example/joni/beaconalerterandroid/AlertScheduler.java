package com.example.joni.beaconalerterandroid;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.example.joni.beaconalerterandroid.jsonentities.Alert;

import java.util.Calendar;

/**
 * Created by Joni on 5.3.2017.
 */
public class AlertScheduler {
    private static AlertScheduler instance = null;

    private AlarmManager manager;
    private Context context;

    private AlertScheduler(){
        //this.context = MainActivity.getBaseContext();
    }

    public static AlertScheduler getInstance(){
        if(instance == null){
            instance = new AlertScheduler();
        }
        return instance;
    }

    public void setManager(AlarmManager manager){
        this.manager = manager;
    }

    public void setContext(Context context){
        this.context = context;
    }

    public void scheduleAlert(Alert alert){
        Intent intent = new Intent(context, AlertSchedulerReceiver.class);
        intent.putExtra("alertID",alert.getId());

        int requestCode = getSqliteID(alert.getId());
        if(requestCode != -1) {
            intent.putExtra("sqlID", requestCode);

            Log.d("AlertScheduler", "" + alert.getTime().toString());
            Log.d("AlertScheduler", ""+requestCode);
            Log.d("AlertScheduler", ""+alert.getId());
            PendingIntent alertItent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Calendar calendar = Calendar.getInstance();
            Calendar timeNow = Calendar.getInstance();

            if(alert.isRepeating()){
                Calendar helperCalendar = Calendar.getInstance();
                helperCalendar.setTime(alert.getTime());

                calendar.set(Calendar.HOUR_OF_DAY, helperCalendar.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, helperCalendar.get(Calendar.MINUTE));
                calendar.set(Calendar.SECOND, 0);

                //Checking how many times a week the alert gets triggered
                boolean[] days = alert.getDays();
                int daysCount = 0;
                for(int i=0; i<days.length;i++){
                    if(days[i]){
                        daysCount++;
                    }
                }
                long interval;
                //Alert repeats only once a week
                if(daysCount == 1){
                    interval = AlarmManager.INTERVAL_DAY * 7;
                    if(calendar.before(timeNow)){
                        Log.d("AlertScheduler", "Time had already passed");
                        calendar.add(Calendar.DATE, 7);
                    }
                //Alert repeats more than once a week. We will handle alerting on correct days in the broadcast receiver
                }else{
                    interval = AlarmManager.INTERVAL_DAY;
                    if(calendar.before(timeNow)){
                        Log.d("AlertScheduler", "Time had already passed");
                        calendar.add(Calendar.DATE, 1);
                    }
                }
                Log.d("AlertScheduler", calendar.toString());
                //manager.setRepeating(AlarmManager.RTC_WAKEUP, helperCalendar.getTimeInMillis(), interval, alertItent);
                manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval, alertItent);
            }else{
                calendar.setTime(alert.getTime());
                Log.d("AlertScheduler", calendar.toString());
                if(!calendar.before(timeNow)){
                    manager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alertItent);
                }else {
                    Log.d("AlertScheduler", "Time had already passed");
                }
            }
            Log.d("AlertScheduler", "Alert scheduled");

        }
    }

    public void cancelAlert(Alert alert){
        Intent intent = new Intent(context, AlertSchedulerReceiver.class);
        intent.putExtra("alertID",alert.getId());
        int requestCode = getSqliteID(alert.getId());
        if(requestCode != -1) {
            Log.d("AlertScheduler", "" + alert.getId());
            Log.d("AlertScheduler", "" + requestCode);
            intent.putExtra("sqlID", requestCode);

            PendingIntent alertItent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            manager.cancel(alertItent);

            Log.d("AlertScheduler", "Alert cancelled");
        }
    }

    public void rescheduleAlert(Alert alert){
        this.cancelAlert(alert);
        this.scheduleAlert(alert);
    }

    private int getSqliteID(String alertID){
        String[] projection = {AlertsTable.COLUMN_ID};
        Cursor idCursor = context.getContentResolver().query(AlertsProvider.ALERTS_CONTENT_URI,projection,AlertsTable.COLUMN_ALERTID+"='"+alertID+"'",null,null);

        int sqliteID;
        if (idCursor != null && idCursor.getCount() > 0) {
            idCursor.moveToNext();
            sqliteID = idCursor.getInt(idCursor.getColumnIndex(AlertsTable.COLUMN_ID));
        }else{
            sqliteID = -1;
        }
        idCursor.close();

        return sqliteID;
    }
}
