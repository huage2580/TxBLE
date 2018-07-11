# TxBLE
ble test
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
