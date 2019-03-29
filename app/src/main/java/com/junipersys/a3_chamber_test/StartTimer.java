package com.junipersys.a3_chamber_test;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StartTimer extends TimerTask {
    @Override
    public void run() {
        //long delay = ChronoUnit.MILLIS.between(LocalTime.now(), LocalTime.of(16, 30, 00));
        //ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        //scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
    }
}
