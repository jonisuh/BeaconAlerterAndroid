package com.example.joni.beaconalerterandroid;

import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Button moreOptionsButton;
    private Button addAlertButton;

    private ListView alertsListView;
    private AlertsCursorAdapter alertsAdapter;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);

        alertsListView = (ListView) findViewById(R.id.alertsListView);

        String[] messagefromcolumns = {AlertsTable.COLUMN_TITLE, AlertsTable.COLUMN_TIME, AlertsTable.COLUMN_ID};
        int[] messagetoviews = {R.id.cell_title_view, R.id.cell_time_view, R.id.cell_long_info_view};

        alertsAdapter = new AlertsCursorAdapter(this, R.layout.alertlist_cell,
                null, messagefromcolumns, messagetoviews, 0);
        Log.d("MainActivity", SELECTION);

        alertsListView.setAdapter(alertsAdapter);
        getLoaderManager().initLoader(1, null, this);

        alertsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("MainActivity", ""+id);

            }
        });

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
                                return true;
                            case R.id.action_settings:
                                Log.d("MainActivity", "Settings");
                                Intent openSettingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                                /* openchatintent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                openchatintent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); */
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
    }

    //CursorLoader methods
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Log.d("AdapterLog", "Cursor created");
        return new CursorLoader(this, CONTENT_URI,
                PROJECTION, SELECTION, null, null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        alertsAdapter.swapCursor(c);
        Log.d("AdapterLog", "Cursor loaded");
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        alertsAdapter.swapCursor(null);
    }

}
