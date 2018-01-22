package com.toxic.ble;

import android.bluetooth.BluetoothDevice;

/**
 * Created by hua on 2018/1/19.
 */

public interface ScanCallBack {
    /**
     * 扫描到新设备的回调
     * @param device
     * @return 用于自动连接，该设备是否符合连接
     */
    boolean onScan(BluetoothDevice device);
}
