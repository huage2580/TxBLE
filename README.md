# TxBLE
ble test
## why 
you know, just for ble.
原生ble，难用的api 伪异步，回调地狱，所以根据项目特点，封装了一份BLE操作库。  
> ps: 非健壮性，请不要直接上手使用，根据项目特点自行改造。避免出现无法解决的bug

## used
```java
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
```
