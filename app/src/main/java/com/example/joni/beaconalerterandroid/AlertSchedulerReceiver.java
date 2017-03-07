package com.example.joni.beaconalerterandroid;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.example.joni.beaconalerterandroid.jsonentities.Alert;

import java.util.Calendar;

/**
 * Created by Joni on 5.3.2017.
 */
public class AlertSchedulerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "AlertWakeLock");
        //Acquire the lock for 10 seconds so that activity has enough time to open
        wl.acquire(10000);

        String alertID = intent.getStringExtra("alertID");
        int sqlID = intent.getIntExtra("sqlID", -1);

        String toastText = alertID+"\n"+sqlID;
        Log.d("AlertSchedulerReceiver", toastText);
        Cursor alertCursor = context.getContentResolver().query(AlertsProvider.ALERTS_CONTENT_URI, null, AlertsTable.COLUMN_ALERTID + "='" + alertID + "'", null, null);

        alertCursor.moveToNext();
        Alert alert = new Alert(alertCursor);
        alertCursor.close();

        boolean displayAlert = true;
        if(alert.isRepeating()){
            Calendar calendar = Calendar.getInstance();
            int currentWeekday = calendar.get(Calendar.DAY_OF_WEEK)-2;
            if(alert.getDays()[currentWeekday]){
                displayAlert = true;
                Log.d("AlertSchedulerReceiver", "enabled today");
            }else{
                displayAlert = false;
                Log.d("AlertSchedulerReceiver", "disabled today");
            }
            //TODO: Reschedule for next day
            //Rescheduling a repeating alert for the next available date.
            AlertScheduler scheduler = AlertScheduler.getInstance();
            scheduler.setContext(context);
            scheduler.setManager((AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
            scheduler.scheduleAlert(alert);
            Log.d("AlertSchedulerReceiver", ""+currentWeekday);
        }else{
            displayAlert = true;
        }

        if(displayAlert) {
            Intent openMainActivityIntent = new Intent(context, MainActivity.class);
            openMainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            //openMainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            openMainActivityIntent.putExtra("alert", alert.generateJson());
            context.startActivity(openMainActivityIntent);
            Log.d("AlertSchedulerReceiver", "Attached " + alert.generateJson() +" to intent.");
        }

        Toast t = Toast.makeText(context, toastText, Toast.LENGTH_SHORT);
        t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        t.show();

        wl.release();
    }
}
