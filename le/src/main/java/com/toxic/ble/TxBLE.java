package com.toxic.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * BLE
 * Created by hua on 2018/1/19.
 */

public class TxBLE {

    private static final String TAG = "TxBLE";
    private Context mContext;
    private BluetoothAdapter mAdapter;
    private HIDUtils hidUtils;
    /**
     * 搜索扫描过滤用的UUID
     */
    private UUID[] uuidFilter=null;

    /**
     * 保存搜索的设备
     */
    private List<BluetoothDevice> findDevices=new ArrayList<>();

    /**
     * 扫描回调
     */
    private ScanCallBack mScanCallBack=null;
    private BluetoothAdapter.LeScanCallback leScanCallBack=null;
    /**
     * 连接回调
     */
    private ConnectCallBack connectCallBack=null;

    private int findTimeOut = 10*1000;
    /**
     * 当前连接设备
     */
    private BluetoothDevice mConnectDevice=null;
    /**
     * 只连接一个服务和自动连接时可用
     */
    private String serviceUUID = "";

    private String characteristicUUID="";

    /**
     * 停止扫描的handler
     */
    private Handler mStopScanHandler =new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (mAdapter!=null){
                mAdapter.stopLeScan(leScanCallBack);
                Log.i(TAG, "扫描超时自动关闭");
            }
            return true;
        }
    });
    /**
     * 当前的控制器//默认控制器
     */
    private BytesPresenter presenter;

    private HashMap<String,BytesPresenter> bytesPresenterMap = new HashMap<>();

    private SpinLock ioLock=new SpinLock();

    /**
     *
     */
    private Executor ioExecutor = Executors.newSingleThreadExecutor();
    /**
     * 通过单线程线程池，实现异步 不阻塞binder线程，单线程执行，队列等待 保证请求的阻塞式时序性。
     */
    private Executor readPresenterExecutor = Executors.newSingleThreadExecutor();
    private Executor writePresenterExecutor = Executors.newSingleThreadExecutor();
    private Executor notifyPresenterExecutor = Executors.newSingleThreadExecutor();

    private List<BluetoothGattCharacteristic> characteristicList = new ArrayList<>();
    /**
     * 获取线程池来自行控制读写
     * @return
     */
    public Executor getIoExecutor() {
        return ioExecutor;
    }

    /**
     * 获取实例，获取blueAdapter
     * @param autoEnabled 自动开启系统蓝牙
     * @return
     */
    public static TxBLE getInstance(Context context, boolean autoEnabled){
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (autoEnabled && !defaultAdapter.isEnabled()){
            defaultAdapter.enable();
        }
        return new TxBLE(context,defaultAdapter);
    }

    /**
     * 使用自定义的adapter
     * @param adapter
     */
    public TxBLE(Context context,BluetoothAdapter adapter) {
        mContext = context;
        mAdapter = adapter;
        hidUtils = new HIDUtils(context);
        presenter = new BytesPresenter();
    }

    /**
     * 设置扫描过滤
     * @param uuids
     * @return
     */
    public TxBLE scanFilter(UUID[] uuids){
        uuidFilter=uuids;
        return this;
    }

    /**
     * 扫描新设备回调.也用于自动连接过滤设备
     * @param callBack
     * @return
     */
    public TxBLE scanCallBack(ScanCallBack callBack){
        mScanCallBack = callBack;
        return this;
    }

    /**
     * 连接到设备的回调
     * @param callBack
     * @return
     */
    public TxBLE connectCallBack(ConnectCallBack callBack){
        connectCallBack = callBack;
        return this;
    }

    /**
     * 扫描超时
     * @param scanTimeOut 单位ms
     * @return
     */
    public TxBLE scanTimeOut(int scanTimeOut){
        this.findTimeOut=scanTimeOut;
        return this;
    }

    public TxBLE serviceUUID(String uuidString){
        this.serviceUUID = uuidString;
        return this;
    }

    public TxBLE characteristicUUID(String uuid){
        this.characteristicUUID=uuid;
        return this;
    }
    public TxBLE addBytesPresenter(String characteristicUUID,BytesPresenter presenter){
        bytesPresenterMap.put(characteristicUUID,presenter);
        return this;
    }

    /**
     * 扫描设备
     * @param autoConnect 自动连接
     */
    public void finds(final boolean autoConnect){
        findDevices.clear();
        mAdapter.stopLeScan(leScanCallBack);
        mStopScanHandler.sendEmptyMessageDelayed(666,findTimeOut);
        leScanCallBack = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if (!findDevices.contains(device)){
                    findDevices.add(device);
                    Log.i(TAG, "onLeScan: find devices"+device.getName()+"-->"+device.getAddress());
                    if (null == TxBLE.this.mScanCallBack){return;}
                    boolean canConnect = TxBLE.this.mScanCallBack.onScan(device);
                    if (autoConnect && canConnect){
                        //自动连接
                        connectGatt(device);
                    }
                }
            }
        };
        //系统连接的HID设备
        hidUtils.getHidConncetList(new ScanCallBack() {
            @Override
            public boolean onScan(BluetoothDevice device) {
                boolean canConnect = TxBLE.this.mScanCallBack.onScan(device);
                if (autoConnect && canConnect){
                    //自动连接
                    connectGatt(device);
                }
                return false;
            }
        });
        //BLE设备扫描
        if (uuidFilter!=null && uuidFilter.length>0){
              mAdapter.startLeScan(leScanCallBack);
        }else {
            mAdapter.startLeScan(uuidFilter,leScanCallBack);
        }
    }

    /**
     * 连接指定蓝牙设备
     * @param device
     */

    public void connectGatt(BluetoothDevice device){
        Log.i(TAG, "connectGatt: "+device.getAddress());
        mAdapter.stopLeScan(leScanCallBack);
        this.mConnectDevice = device;
        mConnectDevice.connectGatt(mContext,false,gattCallBack);
    }

    /**
     * 读和写都应该在可控新线程，不要阻塞蓝牙的binder线程
     * @param datas
     */
    public void write(byte[] datas){
        if (mc==null){return;}
        ioLock.lock();
        mc.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        mc.setValue(datas);
        mGatt.writeCharacteristic(mc);
    }

    /**
     * 读和写都应该在可控新线程，不要阻塞蓝牙的binder线程
     *
     */
    public void read(){
        if (mc==null){return;}
        ioLock.lock();
        mGatt.readCharacteristic(mc);
    }

    /**
     * 订阅广播
     * @return true 订阅成功
     */
    public boolean enableNotification(){
        if (mc==null){return false;}
        boolean tmp;
        tmp = mGatt.setCharacteristicNotification(mc,true);
        if (tmp){
            for(BluetoothGattDescriptor dp: mc.getDescriptors()){
                if (dp != null) {
                    if ((mc.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                        dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    } else if ((mc.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                        dp.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                    }
                    mGatt.writeDescriptor(dp);
                }
            }
        }
        Log.i(TAG, "enableNotification() called"+tmp);
        return tmp;
    }


    private BluetoothGattCharacteristic mc;
    private BluetoothGatt mGatt;


    private BluetoothGattCallback gattCallBack=new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState== BluetoothProfile.STATE_CONNECTED){
                //连接成功后搜索gatt
                Log.i(TAG, "连接设备成功");
                mGatt = gatt;
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                characteristicList.clear();
                List<BluetoothGattService> ss = gatt.getServices();
                for (BluetoothGattService s : ss) {
                    characteristicList.addAll(s.getCharacteristics());
                }
            }
            if (connectCallBack!=null){
                ioExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        switch2Characteristic(characteristicUUID);
                        connectCallBack.connectDevice(mConnectDevice);
                    }
                });
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (!characteristic.equals(mc)){
                return;
            }
            final byte[] tmp = characteristic.getValue();
            readPresenterExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    presenter.onRead(tmp);
                }
            });
            ioLock.unlock();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (!characteristic.equals(mc)){
                return;
            }
            final byte[] tmp = characteristic.getValue();
            writePresenterExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    presenter.onWrite(tmp);
                }
            });
            ioLock.unlock();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (!characteristic.equals(mc)){
                return;
            }
            final byte[] tmp = characteristic.getValue();
            notifyPresenterExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    presenter.onChanged(tmp);
                }
            });
        }
    };

    /**
     * 切换特征值读写
     * @param newCharacteristic
     */
    public void switch2Characteristic(BluetoothGattCharacteristic newCharacteristic){
        ioLock.lock();
        mc=newCharacteristic;
        characteristicUUID = mc.getUuid().toString();
        if (bytesPresenterMap.containsKey(characteristicUUID)){
            presenter = bytesPresenterMap.get(characteristicUUID);
        }else {
            presenter = new BytesPresenter();
        }
        ioLock.unlock();
    }

    public void switch2Characteristic(String newCharacteristic){
        for (BluetoothGattCharacteristic bluetoothGattCharacteristic : characteristicList) {
            if (newCharacteristic.equals(bluetoothGattCharacteristic.getUuid().toString())){
                switch2Characteristic(bluetoothGattCharacteristic);
                return;
            }
        }
    }
}
