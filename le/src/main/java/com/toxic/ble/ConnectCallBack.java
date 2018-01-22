package com.toxic.ble;

import android.bluetooth.BluetoothDevice;

/**
 * Created by hua on 2018/1/19.
 */

public interface ConnectCallBack {
    void connectDevice(BluetoothDevice device);
}
