package com.toxic.ble;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * 接管ble读写监听bytes数据
 * 编码和解码bytes到msg Object
 * Created by hua on 2018/1/19.
 */

public class BytesPresenter {
    private static final String TAG = "BytesPresenter";

    public void onRead(byte[] datas){
        Log.i(TAG, "onRead: "+hex2String(datas));
        //decode
    }
    public void onWrite(byte[] datas){
        Log.i(TAG, "onWrite: "+hex2String(datas));
    }
    public void onChanged(byte[] datas){
        Log.i(TAG, "onChanged: "+hex2String(datas));
        //encode
    }

    @NonNull
    public static String hex2String(byte[] bs){
        StringBuilder sb=new StringBuilder();
        for(byte b:bs){
            String s=Integer.toHexString(b & 0xff);
            sb.append(s.length()==1?"0"+s:s);
            sb.append(" ");
        }
        return sb.toString();
    }


}
