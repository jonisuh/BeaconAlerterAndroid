package com.example.joni.beaconalerterandroid.beacons;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.Date;


/**
 * Created by Joni on 12.3.2017.
 */
public class BeaconScanner{
    private  int scanInterval;

    private Handler scanHandler;
    private boolean isScanning;
    private boolean toggleScan ;
    private BluetoothAdapter adapter;

    private BluetoothAdapter.LeScanCallback callback;

    public BeaconScanner(){
        this.scanInterval = 2000;
        this.adapter = BluetoothAdapter.getDefaultAdapter();
        this.scanHandler = new Handler();
        this.isScanning = false;
        this.toggleScan = false;
    }

    public void beginScanning() {
        if(callback != null) {
            //Forcing bluetooth on
            if (!adapter.isEnabled()){
                adapter.enable();
            }

            toggleScan = true;
            scanHandler.post(scanRunnable);
        }
    }

    public void stopScanning() {
        toggleScan = false;
    }

    public void setScanIntervalMs(int interval){
        this.scanInterval = interval;
    }

    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            if(toggleScan) {

                if (isScanning) {
                    adapter.stopLeScan(callback);
                } else{
                    adapter.startLeScan(callback);
                }
                isScanning = !isScanning;

                scanHandler.postDelayed(this, scanInterval);
            }
        }
    };

    public void setOnBeaconFoundListener(BluetoothAdapter.LeScanCallback callback){
        this.callback = callback;
    }
}