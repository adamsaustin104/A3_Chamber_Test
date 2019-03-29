package com.junipersys.a3_chamber_test;

import android.content.Context;
import android.content.Intent;

import java.util.TimerTask;

public class StartTouchTimer extends TimerTask {

    private Context mContext;

    StartTouchTimer(Context context){
        mContext = context;
    }

    @Override
    public void run() {
        Intent intent = new Intent(mContext, TouchActivity.class);
        mContext.startActivity(intent);
    }


}
