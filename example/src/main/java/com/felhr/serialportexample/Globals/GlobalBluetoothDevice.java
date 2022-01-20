package com.felhr.serialportexample.Globals;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

public class GlobalBluetoothDevice extends Application{
    BluetoothDevice bluetoothDevice = null;
    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }
    public BluetoothDevice getBluetoothDevice() {

        return this.bluetoothDevice;
    }
}