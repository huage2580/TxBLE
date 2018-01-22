package com.example.hua.bledemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by hua on 2017/6/12.
 */

public class MyBtReceiver extends BroadcastReceiver {
    private static final String TAG = "MyBtReceiver";
    MainActivity a;

    public MyBtReceiver(MainActivity a) {
        this.a = a;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        Log.e(TAG, "action==>"+intent.getAction() );
        BluetoothDevice btDevice=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(intent.getAction())){
            Log.i(TAG, "onReceive: ===开始搜索===");
        }
        else if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())){
            Log.i(TAG, "onReceive: "+btDevice.getAddress()+"=>"+btDevice.getName());
            if (btDevice.getName()!=null && btDevice.getName().startsWith("Gamesir")){
                a.pair(btDevice);
//                a.connect(btDevice);
            }

        }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())){
            Log.i(TAG, "onReceive: ===结束搜索===");

        }
        else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())){
            int cur_bond_state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
            if (cur_bond_state==BluetoothDevice.BOND_BONDED){
                Log.i(TAG, "加入已配对: "+btDevice.getAddress()+"=>"+btDevice.getName());
                a.connect(btDevice);
            }
        }
        else if(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(intent.getAction())){
            int state=intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE,BluetoothAdapter.STATE_DISCONNECTED);
            if (state==BluetoothAdapter.STATE_CONNECTED){
                Log.i(TAG, "onReceive:设备连接成功"+btDevice.getName());
            }
        }
        int blueToothstate = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction()) && (blueToothstate == BluetoothAdapter.STATE_ON)) {
            a.autoConnect();
            a.searchDevices();
        }
    }
}
