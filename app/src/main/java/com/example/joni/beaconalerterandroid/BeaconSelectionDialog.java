package com.example.joni.beaconalerterandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.joni.beaconalerterandroid.beacons.Beacon;
import com.example.joni.beaconalerterandroid.beacons.BeaconScanner;
import com.example.joni.beaconalerterandroid.jsonentities.Settings;
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneUID;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Joni on 12.3.2017.
 */
public class BeaconSelectionDialog extends DialogFragment {

    private View dialogview;
    private AlertDialog thisDialog;

    private Button scanBeaconsButton;
    private BeaconScanner scanner;
    private boolean isScanning;
    private BeaconAdapter beaconAdapter;
    private ListView beaconsListView;
    private ArrayList<Beacon> beacons;
    private ProgressBar progressSpinner;

    private String selectedID;

    static BeaconSelectionDialog newInstance() {
        BeaconSelectionDialog dialog = new BeaconSelectionDialog();
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater li = LayoutInflater.from(getActivity());
        dialogview = li.inflate(R.layout.beacon_dialog, null);
        BluetoothAdapter.getDefaultAdapter().disable();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Setting up views
        selectedID = "No beacon selected.";

        scanner = new BeaconScanner();
        scanner.setScanIntervalMs(150);
        scanner.setOnBeaconFoundListener(new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                //Using a 3rd party library to parse the byte data from the advertisement into classes
                List<ADStructure> structures = ADPayloadParser.getInstance().parse(scanRecord);
                for (ADStructure structure : structures) {
                    if (structure instanceof EddystoneUID) {
                        // Eddystone UID
                        EddystoneUID eddyStone = (EddystoneUID) structure;

                        Log.d("BeaconScanner", "Tx Power = " + eddyStone.getTxPower());
                        Log.d("BeaconScanner", "Beacon ID = " + eddyStone.getNamespaceIdAsString() + eddyStone.getInstanceIdAsString());

                        String name = device.getName();
                        String address = device.getAddress();
                        String uid = eddyStone.getBeaconIdAsString();
                        int txPower = eddyStone.getTxPower();
                        Long timeStamp = new Date().getTime();

                        Beacon beacon = new Beacon(name, address, rssi, txPower, uid, timeStamp);
                        if (beacons.contains(beacon)) {
                            Beacon containedBeacon = beacons.get(beacons.indexOf(beacon));
                            containedBeacon.update(address, rssi, txPower);
                            Log.d("BeaconScanner", "old beacon");
                        } else {
                            beacons.add(beacon);
                            Log.d("BeaconScanner", "new beacon " + beacons.size());
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                beaconAdapter.clear();
                                beaconAdapter.addAll(beacons);
                                beaconsListView.invalidateViews();
                            }
                        });

                        //Log.d("BeaconScanner", ""+beaconAdapter.getCount());
                        Log.d("BeaconScanner", beacon.toString());
                    }
                }
            }
        });
        beacons = new ArrayList<>();

        beaconAdapter = new BeaconAdapter(getActivity());
        beaconsListView = (ListView) dialogview.findViewById(R.id.beaconListView);
        beaconsListView.setAdapter(beaconAdapter);

        isScanning = false;
        progressSpinner = (ProgressBar) dialogview.findViewById(R.id.scanProgressSpinner);

        scanBeaconsButton = (Button) dialogview.findViewById(R.id.scanButton);
        scanBeaconsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isScanning){
                    scanner.beginScanning();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //UI
                            progressSpinner.setVisibility(View.VISIBLE);
                            scanBeaconsButton.setText("Stop");

                            selectedID = "No beacon selected.";
                            beacons.clear();
                            Log.d("BeaconScanner", "Beacons length: " + beacons.size());
                            beaconAdapter.clear();
                            beaconAdapter.addAll(beacons);
                            beaconsListView.invalidateViews();
                        }
                    });
                }else{
                    stopScanning();
                }

                isScanning = !isScanning;
            }
        });

        //Builds the alert object
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(dialogview);
        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                stopScanning();
                                dialog.cancel();
                            }
                        })
                .setPositiveButton("Select",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SharedPreferences prefs = getActivity().getSharedPreferences("com.example.joni.beaconalerterandroid", Context.MODE_PRIVATE);
                                stopScanning();
                                prefs.edit().putString(Settings.BEACON_ID, selectedID).apply();
                                ((SettingsActivity) getActivity()).updateViews();
                            }
                        });

        thisDialog = alertDialogBuilder.create();

        //Setting initial state for dialog
        thisDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //Setting up initial views

            }
        });

        //Returning dialog
        return thisDialog;
    }
    private void stopScanning(){
        scanner.stopScanning();

        progressSpinner.setVisibility(View.GONE);
        scanBeaconsButton.setText("Scan");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onDismiss(DialogInterface dialog){
        super.onDismiss(dialog);
        stopScanning();
    }

    @Override
    public void onCancel(DialogInterface dialog){
        super.onCancel(dialog);
        stopScanning();
    }


    class BeaconAdapter extends ArrayAdapter<Beacon> {
        BeaconAdapter(Context context) {
            super(context, R.layout.beacon_list_cell);
            Log.d("BeaconSelectionDialog", "Beacon adapter initialized");
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row;
            Log.d("BeaconSelectionDialog", ""+position);
            final int rowPosition = position;
            final ViewGroup parentView = parent;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.beacon_list_cell, parent, false);
            } else {
                row = convertView;
            }
            TextView deviceNameView = (TextView) row.findViewById(R.id.cellDeviceNameView);
            deviceNameView.setText(beacons.get(rowPosition).name);

            TextView cellIdView = (TextView) row.findViewById(R.id.cellBeaconIDView);
            cellIdView.setText(beacons.get(rowPosition).id);

            TextView rssiView = (TextView) row.findViewById(R.id.cellRssiView);
            rssiView.setText(""+beacons.get(rowPosition).latestRssi);

            row.setBackgroundColor(Color.WHITE);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopScanning();
                    for (int i = 0; i < parentView.getChildCount(); i++) {
                        parentView.getChildAt(i).setBackgroundColor(Color.WHITE);
                    }
                    v.setBackgroundColor(0xFFC4CCFB);
                    selectedID = beacons.get(rowPosition).id;
                    Log.d("BeaconSelectionDialog", selectedID);
                }
            });
            return row;
        }
    }


}