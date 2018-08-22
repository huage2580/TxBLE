package com.toxic.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * 蓝牙HID的操作工具类
 */
public class HIDUtils {

    Context mContext;
    ScanCallBack callBack;

    public HIDUtils(Context mContext) {
        this.mContext = mContext;
    }

    //通过反射获得HID设备的Profile
    @SuppressLint("NewApi")
    public static int getInputDeviceHiddenConstant() {
        Class<BluetoothProfile> clazz = BluetoothProfile.class;
        for (Field f : clazz.getFields()) {
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isPublic(mod)
                    && Modifier.isFinal(mod)) {
                try {
                    if ("HID_HOST".equals(f.getName())) {//android P
                        return f.getInt(null);
                    }
                    if ("INPUT_DEVICE".equals(f.getName())) {
                        return f.getInt(null);
                    }
                } catch (Exception e) {
                }
            }
        }
        return -1;
    }

    /**
     * 获取系统内连接上的HID设备
     * @param callBack
     */
    public void getHidConncetList(final ScanCallBack callBack) {
        try {
            BluetoothAdapter.getDefaultAdapter().getProfileProxy(mContext,
                    new BluetoothProfile.ServiceListener() {
                        @Override
                        public void onServiceConnected(int profile, BluetoothProfile proxy) {
                            try{
                                if (profile == getInputDeviceHiddenConstant()){
                                    List<BluetoothDevice> connectedDevices = proxy
                                            .getConnectedDevices();
                                    for (BluetoothDevice connectedDevice : connectedDevices) {
                                       callBack.onScan(connectedDevice);
                                    }
                                }
                            }catch (Exception e){
                                // TODO: 2018/7/12
                            }
                        }

                        @Override
                        public void onServiceDisconnected(int profile) {

                        }
                    }, getInputDeviceHiddenConstant());
        } catch (Exception e) {

        }

    }
}
