package com.wisebox.gyb;

import android.app.Application;

import com.clj.fastble.data.BleDevice;
import com.wisebox.gyb.utils.gsonObj.DeptMacJson;

import java.util.ArrayList;

public class MyApp extends Application {

    private static Application myapp = null;
    private static ArrayList<DeptMacJson> myHospitalMacs = new ArrayList<DeptMacJson>();

    @Override
    public void onCreate() {
        super.onCreate();
        if(myapp == null) {
            myapp = this;
        }
    }

    public static Application getInstance() {
        return myapp;
    }

    public static void addMacItem(DeptMacJson mac) {
        myHospitalMacs.add(mac);
    }

    public static ArrayList<DeptMacJson> getHospitalMacList() {
        return myHospitalMacs;
    }
}
