package com.example.joni.beaconalerterandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
            return super.newView(context, cursor, parent);
        }

        //Creates a custom view based on cursor data
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
            Log.d("AdapterLog", "view bound");

            Alert alert = new Alert(cursor);
            Log.d("AdapterLog", alert.generateJson().toString());

            TextView titleView = (TextView) view.findViewById(R.id.cell_title_view);
            TextView timeView = (TextView) view.findViewById(R.id.cell_time_view);

            LinearLayout daysContainer = (LinearLayout) view.findViewById(R.id.cell_days_container);
            TextView longDaysView = (TextView) view.findViewById(R.id.cell_long_info_view);

            Switch enableSwitch = (Switch) view.findViewById(R.id.cell_enable_switch);

            //Setting data to views that are used by both types of alerts
            titleView.setText(alert.getTitle());
            //TODO: Get dateformat from settings
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            timeView.setText(timeFormat.format(alert.getTime()));

            enableSwitch.setChecked(alert.isEnabled());

            //Repeating alert
            if(alert.isRepeating()){
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
                        }else{
                            dayView.setTextColor(Color.GRAY);
                        }
                    }

                }
            }else{
                longDaysView.setVisibility(View.VISIBLE);
                daysContainer.setVisibility(View.GONE);
                longDaysView.setTextColor(Color.GRAY);

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
                longDaysView.setText(dateFormat.format(alert.getTime()));
            }

        }
}


