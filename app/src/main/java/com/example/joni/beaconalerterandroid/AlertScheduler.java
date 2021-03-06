package com.example.joni.beaconalerterandroid;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import com.example.joni.beaconalerterandroid.jsonentities.Alert;

import java.util.Calendar;

/**
 * Created by Joni on 5.3.2017.
 */
public class AlertScheduler {
    private static AlertScheduler instance = null;
    private SharedPreferences prefs;
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
        this.prefs = context.getSharedPreferences("com.example.joni.beaconalerterandroid", Context.MODE_PRIVATE);
    }

    public void scheduleAlert(Alert alert){
        Intent intent = new Intent(context, AlertSchedulerReceiver.class);
        intent.putExtra("alertID",alert.getId());

        int requestCode = getSqliteID(alert.getId());
        if(requestCode != -1) {
            intent.putExtra("sqlID", requestCode);
            PendingIntent alertItent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Calendar calendar = Calendar.getInstance();
            Calendar timeNow = Calendar.getInstance();

            if(alert.isRepeating()) {
                Calendar helperCalendar = Calendar.getInstance();
                helperCalendar.setTime(alert.getTime());

                calendar.set(Calendar.HOUR_OF_DAY, helperCalendar.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, helperCalendar.get(Calendar.MINUTE));
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                int today = timeNow.get(Calendar.DAY_OF_WEEK) - 2;
                if(today == -1){
                    today = 6;
                }
                boolean[] days = alert.getDays();

                //Checking that atleast one day is selected
                boolean hasDaySelected = false;
                for (int j = 0; j < days.length; j++) {
                    if(days[j]){
                        hasDaySelected = true;
                    }
                }

                if (hasDaySelected){
                    if (!days[today] || (days[today] && calendar.before(timeNow))) {
                        int i = today + 1;
                        int nextScheduledDay = -1;

                        while (nextScheduledDay == -1) {
                            if (i > 6) {
                                i = 0;
                            }
                            if (days[i]) {
                                if(i <= today){
                                    i += 7;
                                }
                                nextScheduledDay = i;
                                break;
                            }
                            i++;
                        }

                        nextScheduledDay -= today;
                        Log.d("AlertScheduler", "" + nextScheduledDay);
                        calendar.add(Calendar.DATE, nextScheduledDay);
                    }

                    manager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alertItent);

                    Log.d("AlertScheduler", calendar.toString());
                }
            }else{
                calendar.setTime(alert.getTime());
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
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
        intent.putExtra("alertID", alert.getId());
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

    public void scheduleSnooze(Alert alert, int snoozeLength, int snoozeCount){
        Log.d("Snooze ", snoozeLength+" "+snoozeCount);
        Intent intent = new Intent(context, AlertSchedulerReceiver.class);
        intent.putExtra("alertID",alert.getId());
        intent.putExtra("snooze", snoozeCount);
        int requestCode = getSqliteID(alert.getId());
        if(requestCode != -1) {
            intent.putExtra("sqlID", requestCode);
            PendingIntent alertItent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Calendar calendar = Calendar.getInstance();
            Calendar timeNow = Calendar.getInstance();

            Calendar helperCalendar = Calendar.getInstance();
            helperCalendar.setTime(alert.getTime());

            calendar.add(Calendar.MINUTE, snoozeLength);
            calendar.set(Calendar.MILLISECOND, 0);

            Log.d("AlertScheduler", calendar.toString());
            if(!calendar.before(timeNow)){
                manager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alertItent);
            }else {
                Log.d("AlertScheduler", "Time had already passed");
            }

            Log.d("AlertScheduler", "Snooze scheduled");

        }
    }

    public void cancelSnooze(Alert alert){
        //Cancelling all possible snoozes for this alert.
        for(int i=0; i<=3; i++) {
            Intent intent = new Intent(context, AlertSchedulerReceiver.class);
            intent.putExtra("alertID", alert.getId());
            intent.putExtra("snooze",i);
            int requestCode = getSqliteID(alert.getId());
            if (requestCode != -1) {
                PendingIntent alertItent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                manager.cancel(alertItent);

            }
        }
        Log.d("AlertScheduler", "Snoozes cancelled");

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
