package com.example.mybt;
import android.app.Application;

public class MyApplication extends Application {
    private BluetoothThread bluetoothThread;

    public void setBluetoothThread(BluetoothThread bluetoothThread) {
        this.bluetoothThread = bluetoothThread;
    }

    public BluetoothThread getBluetoothThread() {
        return bluetoothThread;
    }
}

