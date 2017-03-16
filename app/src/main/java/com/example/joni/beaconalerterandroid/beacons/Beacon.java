package com.example.joni.beaconalerterandroid.beacons;
import android.util.Log;

import java.util.Arrays;

public class Beacon  {
    private static final String TAG = Beacon.class.getSimpleName();

    public  String name;
    public String deviceAddress;
    public String id;
    public int latestRssi;
    public long lastDetectedTimestamp;
    public int txPower;

    // Eddystone frame types
    public static final byte TYPE_UID = (byte) 0x00;

    public Beacon(String name, String address, int rssi, int txPower, String uid, long timestamp) {
        this.name = name;
        this.deviceAddress = address;
        this.latestRssi = rssi;
        this.txPower = txPower;
        this.id = uid.toLowerCase(); //Forces the ID to lowercase
        this.lastDetectedTimestamp = timestamp;
    }

    public void update(String address, int rssi, int txPower) {
        this.deviceAddress = address;
        this.latestRssi = rssi;
        this.txPower = txPower;

    }

    @Override
    public String toString() {
        return String.format("Name: "+name+"\nUID: "+id+"\nRSSI: "+latestRssi+"\nMAC: "+deviceAddress+"\nTXPOWER: "+txPower);
    }

    @Override
    public boolean equals(Object obj){
        //If obj is not a beacon
        if (!(obj instanceof Beacon)) {
            return false;
        }
        //Should be safe to the object to a beacon
        Beacon objBeacon = (Beacon) obj;
        //If obj is this
        if (objBeacon == this){
            return true;
        }
        if(objBeacon.id.equals(this.id)){
            return true;
        }

        return false;
    }
}