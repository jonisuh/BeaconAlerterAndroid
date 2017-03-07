package com.example.joni.beaconalerterandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.joni.beaconalerterandroid.Services.RescheduleAlertsService;

/**
 * Created by Joni on 6.3.2017.
 */
public class RebootAlertSchedulerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("RescheduleAlertsService", "Broadcast received.");
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent rescheduleIntent = new Intent(context, RescheduleAlertsService.class);
            context.startService(rescheduleIntent);
        }
    }
}
