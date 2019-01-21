package com.wisebox.gyb.BLE;

import android.app.Application;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleScanCallback;
import com.wisebox.gyb.MyApp;

/**
 * 蓝牙工具类
 */
public class BLEUtils {

    public static void init() {
        BleManager.getInstance().init(MyApp.getInstance());
        BleManager.getInstance().enableLog(true)
                .setReConnectCount(1, 5000)
                .setSplitWriteNum(20)
                .setConnectOverTime(10000)
                .setOperateTimeout(5000);
    }

    public static BleManager getInstance() {
        return BleManager.getInstance();
    }

    public static void scan(BleScanCallback callback) {
        BleManager.getInstance().scan(callback);
    }

}
