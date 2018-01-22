package com.example.hua.bledemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    String handStr="Gamesir";
    static BluetoothAdapter mbtAdapter;
    Button btnOpen;
    Button btnSearch;
    Button btnCon;
    MyBtReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        regRecevicer();
        btnOpen= (Button) findViewById(R.id.openbt);
        btnOpen.setOnClickListener(this);
        btnSearch= (Button) findViewById(R.id.searchBt);
        btnSearch.setOnClickListener(this);
        btnCon= (Button) findViewById(R.id.conBt);
        btnCon.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch( v.getId()){
            case R.id.openbt:
                openBlueTooth();
                break;
            case R.id.searchBt:
                searchDevices();
                break;
            case R.id.conBt:
                getCon();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    /**
     * 开启蓝牙
     */
    private void openBlueTooth(){
        mbtAdapter =BluetoothAdapter.getDefaultAdapter();
        if(!mbtAdapter.isEnabled()){
            mbtAdapter.enable();
        }else{
            autoConnect();
            searchDevices();
            getConnectDevice(new findCallBack() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "onSuccess: 早就有设备链接了");
                    mbtAdapter.cancelDiscovery();
                }

                @Override
                public void onFaied() {
                    Log.i(TAG, "onFaied: 当前没连接手柄");
                }
            });
        }
    }

    /**
     * 搜索蓝牙设备
     */
    public void searchDevices(){
        if(!mbtAdapter.isDiscovering()){
            mbtAdapter.startDiscovery();
        }
    }

    /**
     * 自动连接已经配对的设备
     */
    public void autoConnect(){
        Set<BluetoothDevice> devices = mbtAdapter.getBondedDevices();
        for (BluetoothDevice d: devices) {
            Log.i(TAG, "autoConnect: "+d.getName()+"=>"+d.getAddress());
            connect(d);
        }
    }
    public void connect( BluetoothDevice d){
        final BluetoothDevice device = d;
        try {
            BluetoothAdapter.getDefaultAdapter().getProfileProxy(this,
                    new BluetoothProfile.ServiceListener() {
                        @Override
                        public void onServiceConnected(int profile, BluetoothProfile proxy) {
                            Log.i(TAG, "onServiceConnected: "+profile);
                            Method method = null;
                            try {
                                method = proxy.getClass().getMethod("connect",
                                        new Class[] { BluetoothDevice.class });
                                method.invoke(proxy, device);
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onServiceDisconnected(int profile) {

                        }
                    }, getInputDeviceHiddenConstant());
        } catch (Exception e) {

        }
    }

    /**
     * 加入配对列表
     * @param bluetoothDevice
     */
    public void pair(BluetoothDevice bluetoothDevice) {
        BluetoothDevice device = bluetoothDevice;
        Method createBondMethod;
        try {
            createBondMethod = BluetoothDevice.class.getMethod("createBond");
            createBondMethod.invoke(device);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 注册寻找设备广播
     */
    private void regRecevicer(){
        receiver=new MyBtReceiver(this);
        IntentFilter itf=new IntentFilter(BluetoothDevice.ACTION_FOUND);
        itf.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        itf.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        itf.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        itf.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        itf.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        itf.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver,itf);
    }
    public static int getInputDeviceHiddenConstant() {
        Class<BluetoothProfile> clazz = BluetoothProfile.class;
        for (Field f : clazz.getFields()) {
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isPublic(mod)
                    && Modifier.isFinal(mod)) {
                try {
                    if (f.getName().equals("INPUT_DEVICE")) {
                        return f.getInt(null);
                    }
                } catch (Exception e) {
                }
            }
        }
        return -1;
    }

    /**
     * 获取连接设备
     */
    public void getConnectDevice(final findCallBack c){
        BluetoothAdapter.getDefaultAdapter().getProfileProxy(this, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                List<BluetoothDevice> devices = proxy.getConnectedDevices();
                for (BluetoothDevice device:devices){
                    if (device.getName().startsWith(handStr)){
                        Log.i(TAG, "onServiceConnected: 手柄已连接");
                        c.onSuccess();
                        return;
                    }
                }
                c.onFaied();
            }

            @Override
            public void onServiceDisconnected(int profile) {

            }
        }, getInputDeviceHiddenConstant());
    }
    public void getCon(){
        BluetoothManager m= (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        List<BluetoothDevice> devices=m.getConnectedDevices(BluetoothProfile.GATT);
        for (BluetoothDevice device:devices){
            Log.e(TAG, "getCon: "+device.getName());
        }

    }
    public interface findCallBack{
        void onSuccess();
        void onFaied();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.i(TAG, "dispatchKeyEvent: "+event.getKeyCode());
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        Log.i(TAG, "dispatchGenericMotionEvent: "+ev);
        float lx = ev.getAxisValue(MotionEvent.AXIS_HAT_X);
        float ly = ev.getAxisValue(MotionEvent.AXIS_HAT_Y);
        float xaxis2 = ev.getAxisValue(MotionEvent.AXIS_X);// 摇杆
        float yaxis2 = ev.getAxisValue(MotionEvent.AXIS_Y);
        float xaxis3 = ev.getAxisValue(MotionEvent.AXIS_Z);
        float yaxis3 = ev.getAxisValue(MotionEvent.AXIS_RZ);
        Log.e(TAG, "lx="+lx+" ly="+ly+" xaxis2="+xaxis2+" ys2="+yaxis2+" xax3="+xaxis3+" yax3="+yaxis3);
        return super.dispatchGenericMotionEvent(ev);
    }
}
