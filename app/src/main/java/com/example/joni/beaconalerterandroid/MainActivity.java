package com.example.joni.beaconalerterandroid;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.joni.beaconalerterandroid.Services.SynchronizationService;
import com.example.joni.beaconalerterandroid.jsonentities.Settings;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    //Server URL
    public static final String SERVER_URL = "https://beaconalerter.herokuapp.com/api/";

    private Button moreOptionsButton;
    private Button addAlertButton;

    private ListView alertsListView;
    private AlertsCursorAdapter alertsAdapter;

    //Alertscheduler
    private AlertScheduler scheduler;

    //CursorLoader settings
    private String[] PROJECTION = new String[]{
            AlertsTable.COLUMN_ID,
            AlertsTable.COLUMN_ALERTID,
            AlertsTable.COLUMN_TITLE,
            AlertsTable.COLUMN_TIME,
            AlertsTable.COLUMN_ISENABLED,
            AlertsTable.COLUMN_REPEATING,
            AlertsTable.COLUMN_DAYS
    };

    private String SELECTION = "";
    private Uri CONTENT_URI = AlertsProvider.ALERTS_CONTENT_URI;

    private boolean returningFromSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);
        returningFromSettings = false;

        //Ensures that app wakes up correctly and displays the applcation
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        //Initializing scheduler
        scheduler = AlertScheduler.getInstance();
        scheduler.setContext(this);
        scheduler.setManager((AlarmManager) getSystemService(Context.ALARM_SERVICE));


        alertsListView = (ListView) findViewById(R.id.alertsListView);

        String[] messagefromcolumns = {AlertsTable.COLUMN_TITLE, AlertsTable.COLUMN_TIME, AlertsTable.COLUMN_ID};
        int[] messagetoviews = {R.id.cell_title_view, R.id.cell_time_view, R.id.cell_long_info_view};

        alertsAdapter = new AlertsCursorAdapter(this, R.layout.alertlist_cell,
                null, messagefromcolumns, messagetoviews, 0);
        Log.d("MainActivity", SELECTION);

        alertsListView.setAdapter(alertsAdapter);
        getLoaderManager().initLoader(1, null, this);

        TextView emptyText = (TextView)findViewById(R.id.noAlertsFoundView);
        alertsListView.setEmptyView(emptyText);

        moreOptionsButton = (Button) findViewById(R.id.moreActionsButton);
        moreOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.this, moreOptionsButton);

                popup.getMenuInflater().inflate(R.menu.more_options_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_sync:
                                Log.d("MainActivity", "Sync");
                                Intent syncIntent = new Intent(MainActivity.this, SynchronizationService.class);
                                syncIntent.putExtra("forceSync",true);
                                startService(syncIntent);
                                return true;
                            case R.id.action_settings:
                                Log.d("MainActivity", "Settings");
                                returningFromSettings = true;
                                Intent openSettingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(openSettingsIntent);
                                return true;
                            default:
                                Log.d("MainActivity", "Should not happen");
                                return false;

                        }
                    }
                });
                popup.show();
            }
        });

        addAlertButton = (Button) findViewById(R.id.addAlertButton);
        addAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = CreateAlertDialog.newInstance();
                dialog.show(getFragmentManager(), "CreateAlertDialog");
            }
        });

        //Try displaying popover if alert was triggered
        showAlertPopover();
    }

    //CursorLoader methods
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Log.d("AdapterLog", "Cursor created");
        return new CursorLoader(this, CONTENT_URI,
                PROJECTION, SELECTION, null, AlertsTable.COLUMN_TIME+" ASC");
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {

        int repeatingCount = 0;
        while(c.moveToNext()){
            if(c.getInt(c.getColumnIndex(AlertsTable.COLUMN_REPEATING)) == 1){
                repeatingCount++;
            }
        }
        c.moveToFirst();
        Log.d("AdapterLog", "repeating " + repeatingCount);
        Log.d("AdapterLog", "one time "+(c.getCount() - repeatingCount));

        alertsAdapter.setRepeatingItemCount(repeatingCount);
        alertsAdapter.setOneTimeItemCount(c.getCount() - repeatingCount);
        alertsAdapter.swapCursor(c);
        Log.d("AdapterLog", "Cursor loaded");
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        alertsAdapter.swapCursor(null);
        Log.d("AdapterLog", "Cursor reset");
    }

    public Context getContext(){
        return getBaseContext();
    }

    private void showAlertPopover(){
        //Handling intent from alert scheduler receiver. If intent contains an alertID we open the alert popup dialog
        Intent intent = getIntent();
        String alert = intent.getStringExtra("alert");
        if(alert != null){
            Log.d("MainActivity", alert + "\nReceived in MainActivity");
            DialogFragment dialog;
            int snooze = intent.getIntExtra("snooze", -1);
            if(snooze != -1){
                dialog = AlertPopupDialog.newInstance(alert,snooze);
            }else {
                dialog = AlertPopupDialog.newInstance(alert);
            }
            dialog.setCancelable(false);
            dialog.show(getFragmentManager(), "AlertPopupDialog");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("MainActivity", "New intent");
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one
        showAlertPopover();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "Refreshing adapter.");
        alertsAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(!returningFromSettings){
            SharedPreferences prefs = getSharedPreferences("com.example.joni.beaconalerterandroid", Context.MODE_PRIVATE);
            if(prefs.getBoolean(Settings.AUTOMATIC_SYNC,true)){
                Intent syncIntent = new Intent(MainActivity.this, SynchronizationService.class);
                startService(syncIntent);
            }
        }else{
            returningFromSettings = false;
        }
    }
}
