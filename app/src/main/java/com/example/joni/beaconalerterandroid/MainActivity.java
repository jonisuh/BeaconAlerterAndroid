package com.example.joni.beaconalerterandroid;

import android.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Spinner;


public class MainActivity extends AppCompatActivity {

    private Button moreOptionsButton;
    private Button addAlertButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);

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
}
