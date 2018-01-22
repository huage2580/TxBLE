package com.toxic.ble;

/**
 * 特征值改变的监听器
 * Created by hua on 2018/1/19.
 */

public interface ValueWatcher {
    /**
     * 特征值改变
     * @param values
     */
    void onCharacteristicsChanged(byte[] values);
}
