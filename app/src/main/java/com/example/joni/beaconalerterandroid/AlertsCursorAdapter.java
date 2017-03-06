package com.example.joni.beaconalerterandroid;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.example.joni.beaconalerterandroid.jsonentities.Alert;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;

/**
 * Created by Joni on 27.2.2017.
 */
public class AlertsCursorAdapter extends SimpleCursorAdapter {

        private Context mContext;
        private Context appContext;
        private int layout;
        private Cursor cr;
        private final LayoutInflater inflater;

        private int repeatingItemCount;
        private int oneTimeItemCount;

        public AlertsCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            this.layout = layout;
            this.mContext = context;
            this.inflater = LayoutInflater.from(context);
            this.cr = c;
            Log.d("AdapterLog", "Adapter created");
        }

        @Override
        public View getView(int position, View convertview, ViewGroup arg2) {
            return super.getView(position, convertview, arg2);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            //Log.d("AdapterLog", "new view");
            return super.newView(context, cursor, parent);
        }

        //Creates a custom view based on cursor data
        @Override
        public void bindView(final View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
            //Log.d("AdapterLog", "view bound");
            Log.d("AdapterLog", "" + cursor.getPosition());
            final Alert alert = new Alert(cursor);

            //Section header
            TextView sectionHeader = (TextView) view.findViewById(R.id.sectionHeaderTitle);

            TextView idView = (TextView) view.findViewById(R.id.cell_id_view);
            TextView titleView = (TextView) view.findViewById(R.id.cell_title_view);
            TextView timeView = (TextView) view.findViewById(R.id.cell_time_view);

            LinearLayout daysContainer = (LinearLayout) view.findViewById(R.id.cell_days_container);
            TextView longDaysView = (TextView) view.findViewById(R.id.cell_long_info_view);

            //Enable / disable
            Switch enableSwitch = (Switch) view.findViewById(R.id.cell_enable_switch);
            enableSwitch.setChecked(alert.isEnabled());
            enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    alert.setIsEnabled(isChecked);
                    mContext.getContentResolver().update(AlertsProvider.ALERTS_CONTENT_URI, alert.generateContentValue(), AlertsTable.COLUMN_ALERTID + "='" + alert.getId() + "'", null);
                    Log.d("AdapterLog", "alert state: " + alert.isEnabled());

                    AlertScheduler scheduler = AlertScheduler.getInstance();
                    if(alert.isEnabled()){
                        scheduler.scheduleAlert(alert);
                    }else{
                        scheduler.cancelAlert(alert);
                    }
                }
            });

            //Setting data to views that are used by both types of alerts
            idView.setText(alert.getId());
            titleView.setText(alert.getTitle());
            //TODO: Get dateformat from settings
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            timeView.setText(timeFormat.format(alert.getTime()));

            //Repeating alert
            if(alert.isRepeating()){
                if(cursor.getPosition() == 0){
                    sectionHeader.setVisibility(View.VISIBLE);
                    sectionHeader.setText("REPEATING");
                }else{
                    sectionHeader.setVisibility(View.GONE);
                }
                //Checking if every day is selected
                boolean everyDaySelected = true;
                for(int i=0; i<alert.getDays().length; i++){
                    if(!alert.getDays()[i]){
                        everyDaySelected = false;
                    }
                }

                if(everyDaySelected){
                    daysContainer.setVisibility(View.GONE);
                    longDaysView.setVisibility(View.VISIBLE);
                    longDaysView.setText("Every day");
                    longDaysView.setTextColor(Color.BLUE);
                }else{
                    daysContainer.setVisibility(View.VISIBLE);
                    longDaysView.setVisibility(View.GONE);

                    for(int i=0; i<alert.getDays().length; i++){
                        String viewId = "cell_day_" + i;
                        int id = view.getResources().getIdentifier(viewId,"id",view.getContext().getPackageName());
                        TextView dayView = (TextView) view.findViewById(id);

                        if(alert.getDays()[i]){
                            dayView.setTextColor(Color.BLUE);
                            dayView.setTypeface(null, Typeface.NORMAL);
                        }else{
                            dayView.setTextColor(Color.GRAY);
                            dayView.setTypeface(null, Typeface.NORMAL);
                        }
                    }

                }
            }else{
                if(cursor.getPosition() == this.repeatingItemCount){
                    sectionHeader.setVisibility(View.VISIBLE);
                    sectionHeader.setText("ONE TIME");
                    float scale = view.getResources().getDisplayMetrics().density;
                    int leftAsPX = (int) (10*scale +0.5f);
                    int topAsPX = (int) (5*scale +0.5f);
                    sectionHeader.setPadding(leftAsPX, topAsPX, 0, topAsPX);
                }else if(cursor.getPosition() == 0){
                    sectionHeader.setVisibility(View.VISIBLE);
                    sectionHeader.setText("ONE TIME");
                }else{
                    sectionHeader.setVisibility(View.GONE);
                }

                longDaysView.setVisibility(View.VISIBLE);
                daysContainer.setVisibility(View.GONE);
                longDaysView.setTextColor(Color.GRAY);

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
                longDaysView.setTextColor(Color.BLACK);
                longDaysView.setText(dateFormat.format(alert.getTime()));
            }

        }

    public void setRepeatingItemCount(int count){
        this.repeatingItemCount = count;
    }

    public int getRepeatingItemCount(){
        return this.repeatingItemCount;
    }

    public void setOneTimeItemCount(int count){
        this.oneTimeItemCount = count;
    }

    public int getOneTimeItemCount(){
        return this.oneTimeItemCount;
    }
}


