package com.junipersys.a3_chamber_test;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

public class Wifi extends Service {

    public static final String TAG = Wifi.class.getCanonicalName();

    public static final int TIMEOUT_DURATION = 15 * 1000;

    private WifiConfiguration mWifiConfiguration;
    private WifiManager mWifiManager;
    private List<ScanResult> mScanResults;
    private Handler mTimeoutHandler = new Handler();


    private String mWifiAp;
    private String mWifiPass;

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {

        mWifiAp = "JSIMfg";
        mWifiPass = "K4pdun!t5";

        mWifiConfiguration = new WifiConfiguration();
        mWifiConfiguration.SSID = "\"" + mWifiAp + "\"";
        mWifiConfiguration.preSharedKey = "\"" + mWifiPass + "\"";

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if(mWifiManager == null){
            return START_NOT_STICKY;
        }

        if(!mWifiManager.isWifiEnabled()){
            mWifiManager.setWifiEnabled(true);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);

        registerReceiver(mWifiReceiver, intentFilter);

        mWifiManager.startScan();

        mTimeoutHandler.postDelayed(mTimeoutRunnable, TIMEOUT_DURATION);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        mTimeoutHandler.removeCallbacksAndMessages(null);
        unregisterReceiver(mWifiReceiver);
    }

    private Runnable mTimeoutRunnable = new Runnable(){

        @Override
        public void run() {
        }
    };

    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null) {
                return;
            }

            switch (intent.getAction()) {
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    networkStateShanged(networkInfo);
                    Log.d(TAG, "Network state changed");
                    break;
                case WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION:
                    Log.d(TAG, "Supplicant connection change");
                    break;
                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                    Log.d(TAG, "Supplicant state changed");
                    break;
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    scanResultsAvailable();
                    Log.d(TAG, "Scan reuslts available");
                    break;
            }
        }
    };

    private void networkStateShanged(NetworkInfo networkInfo) {

        if (networkInfo == null) {
            return;
        }

        boolean isConnected = networkInfo.isConnected();
        boolean isConnecting = networkInfo.isConnectedOrConnecting();

        String ssidExtraInfo = networkInfo.getExtraInfo().replace("\"", "");

        if(isConnected && isConnecting && mWifiAp.equals(ssidExtraInfo)){
        }
    }

    private void scanResultsAvailable() {
        mScanResults = mWifiManager.getScanResults();
        if (mScanResults != null) {
            for (ScanResult scanResult : mScanResults) {
                if (scanResult.SSID.equals(mWifiAp)) {
                    attemptConnection();
                }
            }
        }
    }

    private void attemptConnection() {
        mWifiManager.addNetwork(mWifiConfiguration);
        mWifiManager.disconnect();
        mWifiManager.enableNetwork(mWifiConfiguration.networkId, true);
        mWifiManager.reconnect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
