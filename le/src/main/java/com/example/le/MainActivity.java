package com.example.le;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.bluetooth.le.Gamesir;
import com.toxic.ble.BytesPresenter;
import com.toxic.ble.ConnectCallBack;
import com.toxic.ble.ScanCallBack;
import com.toxic.ble.TxBLE;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static String sss = "00008650-0000-1000-8000-00805f9b34fb";
    public static String ccc= "00008655-0000-1000-8000-00805f9b34fb";//数据通道
    public static final byte[] VIBRATION_DATA = { (byte) 0X04, (byte) 0XFF, (byte) 0X04, (byte) 0XFF, (byte) 0X04 };
    Button btnScan;
    Button btnOpen;
    Button btnStop;
    String temps="";
    private BluetoothGatt mGatt;
    private BluetoothGattCharacteristic mc;
    static BluetoothAdapter mbtAdapter;
    private static final String TAG = "ble";
    private List<BluetoothDevice> finds=new ArrayList<>();
    long back=System.currentTimeMillis();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnOpen= (Button) findViewById(R.id.btn_le_open);
        btnScan= (Button) findViewById(R.id.btn_le_scan);
        btnStop= (Button) findViewById(R.id.btn_le_stop);
        btnOpen.setOnClickListener(this);
        btnScan.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_le_open:
//                openBt();
                testBLE();
                break;
            case  R.id.btn_le_scan:
                startScan();
                break;
            case R.id.btn_le_stop:
                vvvvv();
                break;
            default:
                //nothing
        }
    }

    private void testBLE() {
        final TxBLE ble = TxBLE.getInstance(this, true);
                ble.serviceUUID(sss)
                .characteristicUUID(ccc)
                .scanCallBack(new ScanCallBack() {
                    @Override
                    public boolean onScan(BluetoothDevice device) {
                        Log.i(TAG, "onScan: "+device);
                        return device.getName() != null && device.getName().startsWith("Gamesir");
                    }
                })
                .connectCallBack(new ConnectCallBack() {
                    @Override
                    public void connectDevice(BluetoothDevice device) {
                        Main.btaddr=BtaddrToHex(device.getAddress());
                        ble.read();
                    }
                }).addBytesPresenter(ccc,new BytesPresenter(){
                    private String temp="";
                    @Override
                    public void onRead(byte[] datas) {
                        byte[] jdecode = Main.decodeData(datas);
                        String msg =hex2String(jdecode);
                        if (temp.equals(msg)){return;}
                        temp = msg;
                        Log.i(TAG, "onRead: "+msg);
                    }
                })
                .finds(true);
    }

    private void openBt(){
        mbtAdapter=BluetoothAdapter.getDefaultAdapter();
        if(!mbtAdapter.isEnabled()){
            mbtAdapter.enable();
        }
    }
    private void startScan(){
        finds.clear();
        mbtAdapter.startLeScan(callBack);
//        mbtAdapter.startLeScan(new UUID[]{gameUUID},callBack);
    }
    private void stopScan(){
        mbtAdapter.stopLeScan(callBack);
    }
    private void vvvvv(){
        mc.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        mc.setValue(VIBRATION_DATA);
        mGatt.writeCharacteristic(mc);
//        byte[] test={ 0x5c, (byte) 0xf6,0x18, (byte) 0xc3,0x62,0x42, (byte) 0xee, (byte) 0xb0, (byte) 0x9e, (byte) 0x95, (byte) 0x98, (byte) 0xa8,0x30,0x61, (byte) 0xc3,0x0c};
//        int[] temp=new int[16];
//        for(int i=0;i<16;i++){
//            temp[i]=test[i];
//        }
//        Log.e(TAG, "转换后="+hex2String(temp) );
//        temp= Gamesir.decryJoyDataYuneec(temp);
//        Log.e(TAG, "解密后="+hex2String(temp) );
//        for (int i = 0; i < temp.length; i++) {
//            temp[i] = temp[i] & 0xff;
//        }
//        Log.i(TAG, "vvvvv: "+hex2String(temp)+"===="+hex2String(test));

    }
    private BluetoothAdapter.LeScanCallback callBack=new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (!finds.contains(device)){
                finds.add(device);
                Log.i(TAG, "onLeScan: "+device.getName()+"=>"+device.getAddress()+" rssi->"+rssi);
                connect(device);
            }
        }
    };
    private void connect(BluetoothDevice d){
        stopScan();
        if (d.getName()!=null && d.getName().startsWith("Gamesir") && d.getName().endsWith("GCM")){
            Log.i(TAG, "connect: "+d.getName()+"-->"+d.getAddress());
            Gamesir.setBTMac(BtaddrToHex(d.getAddress()));
            Main.btaddr=BtaddrToHex(d.getAddress());
            d.connectGatt(this,false,gattCallBack);
        }
    }
    BluetoothGattCallback gattCallBack=new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState== BluetoothProfile.STATE_CONNECTED){//连接成功后搜索gatt
                Log.i(TAG, "连接设备成功");
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> ss = gatt.getServices();
                for (BluetoothGattService s:ss){
                    //Log.i(TAG, "onServicesDiscovered: "+s.getUuid());
                    if (s.getUuid().toString().equals(sss)){
                        for (BluetoothGattCharacteristic c :s.getCharacteristics()){
                            //Log.i(TAG, "cc==>>>>>"+c.getUuid());
                            if (c.getUuid().toString().equals(ccc)){
                                Log.i(TAG, "找到Characteristic");
                                c.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                                c.setValue(VIBRATION_DATA);
                                gatt.writeCharacteristic(c);
                                mc=c;
                                mGatt=gatt;
                                return;
                            }
                        }
                    }

                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (!characteristic.getUuid().toString().equals(ccc)){
                return;
            }
            byte[] btemp=characteristic.getValue();
            byte[] jdecode = Main.decodeData(btemp);
            if(btemp.length==16){
                int[] temp=new int[16];
                for(int i=0;i<16;i++){
                    temp[i]=btemp[i];
                }
                temp= Gamesir.decryJoyData(temp);
                for (int i = 0; i < temp.length; i++) {
                    temp[i] = temp[i] & 0xff;
                }
                if (!temps.equals(hex2String(temp))){
                    temps=hex2String(temp);
                    long now = System.currentTimeMillis();
                    Log.i(TAG, hex2String(jdecode)+"<<===>>"+hex2String(temp)+"["+(now-back));
                    back=now;
                }

            }
            gatt.readCharacteristic(characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i(TAG, "onCharacteristicWrite: ");
            gatt.readCharacteristic(characteristic);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i(TAG, "onCharacteristicChanged: ");
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.i(TAG, "onDescriptorRead: ");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.i(TAG, "onDescriptorWrite: ");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.i(TAG, "onReliableWriteCompleted: ");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.i(TAG, "onReadRemoteRssi: ");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.i(TAG, "onMtuChanged: ");
        }
    };
    private String hex2String(byte[] bs){
        StringBuilder sb=new StringBuilder();
        for(byte b:bs){
            String s=Integer.toHexString(b & 0xff);
            sb.append(s.length()==1?"0"+s:s);
            sb.append(" ");
        }
        return sb.toString();
    }
    private String hex2String(int[] bs){
        StringBuilder sb=new StringBuilder();
        for(int b:bs){
            sb.append(Integer.toHexString(b & 0xff));
            sb.append(",");
        }
        return sb.toString();
    }
    public byte[] BtaddrToHex(String btaddr) {
        int i, j, len;
        String tmpsr;
        byte[] local_bt_addr = new byte[6];
        len = btaddr.length();
        for (i = 0, j = 0; j < len; i++) {
            if (i >= 6)
                break;
            tmpsr = btaddr.substring(j, j + 1) + btaddr.substring(j + 1, j + 2);
            local_bt_addr[i] = (byte) (Integer.parseInt(tmpsr, 16));
            j += 3;
        }
        return local_bt_addr;
    }
}
