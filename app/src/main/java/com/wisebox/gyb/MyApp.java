package com.wisebox.gyb;

import android.app.Application;

import com.clj.fastble.data.BleDevice;

import java.util.ArrayList;

public class MyApp extends Application {

    private static Application myapp = null;
    private static ArrayList<String> myHospitalMacs = new ArrayList<String>();

    @Override
    public void onCreate() {
        super.onCreate();
        if(myapp == null) {
            myapp = this;
        }
        myHospitalMacs.add("D8:96:E0:8B:81:59");
        myHospitalMacs.add("D8:96:E0:8B:81:9B");
        myHospitalMacs.add("D8:96:E0:8B:81:5D");
    }

    public static Application getInstance() {
        return myapp;
    }

    public static ArrayList<String> getHospitalBlies() {
        return myHospitalMacs;
    }
}
