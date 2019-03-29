package com.junipersys.a3_chamber_test;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.TimerTask;

public class StartChamberTimer extends TimerTask {

    private Context mContext;

    StartChamberTimer(Context context){
        mContext = context;
    }

    @Override
    public void run() {
        startTests();
    }

    public void startTests(){
        //begin CPU testing
        CpuTest cpuTest = new CpuTest();
        cpuTest.runCpuTest();

        //begin disk test
        DiskTest diskTest = new DiskTest(mContext);
        diskTest.runDiskTest();

        //begin RAM test
        RamTest ramTest = new RamTest();
        ramTest.runRamTest();

        SharedPreferences sp = mContext.getSharedPreferences(MainActivity.PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("chamber_passed", 1);
        editor.commit();

        Intent intent = new Intent(mContext, TouchActivity.class);
        mContext.startActivity(intent);
    }
}
