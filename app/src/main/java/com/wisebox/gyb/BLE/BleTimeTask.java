package com.wisebox.gyb.BLE;

import java.util.Timer;
import java.util.TimerTask;

public class BleTimeTask {
    private Timer timer;
    private TimerTask task;
    private long interval;

    public BleTimeTask(long time, TimerTask task) {
        this.task = task;
        this.interval = time;
        timer = new Timer();
    }

    public void start() {
        timer.schedule(task, 0, interval);
    }

    public void stop() {
        if(timer != null) {
            timer.cancel();
            if(task != null) {
                task.cancel();
            }
        }
    }
}
