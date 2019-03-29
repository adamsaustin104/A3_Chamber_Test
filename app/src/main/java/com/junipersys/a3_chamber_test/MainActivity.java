package com.junipersys.a3_chamber_test;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String PREF_FILE = "a3.chamber";
    private Button startButton;
    private Button startNowButton;
    private Button stopButton;
    private VideoView videoView;
    private TextView ramTextView;
    private TextView ramTextViewStatus;
    GraphicsTest graphicsTest;

    private static final int IS_CHAMBER_RUNNING = 0;
    private static final int IS_TOUCH_CAPTURED = 1;
    private static final int CHAMBER_PASSED = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.startButton);
        startNowButton = findViewById(R.id.startNowButton);
        stopButton = findViewById(R.id.stopButton);
        videoView = findViewById(R.id.videoPlayer);
        ramTextViewStatus = findViewById(R.id.ramStatus);

        initializePlayer();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        //--------------------------------
        //   OnClickButton Methods
        //--------------------------------

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dim backlight
                FileOutputStream fos;
                try {
                    fos = new FileOutputStream("/sys/class/backlight/a3-backlight/brightness");
                    fos.write(50);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //set shared preferences for isChamberRunning and isCapturingTouch
                SharedPreferences prefs = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("is_chamber_running", 1);
                editor.putInt("is_touch_captured", 0);
                editor.putInt("chamber_passed", 0);
                editor.commit();
                //start timer to begin tests at 4:30pm
                startButton.setText("WAITING...");

                DateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");
                Date chamberTime = null;
                try {
                    chamberTime = dateFormatter.parse("16:35:00");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //Now create the time and schedule it
                Timer timer = new Timer();
                //Use this if you want to execute it once
                timer.schedule(new StartChamberTimer(MainActivity.this), chamberTime);


                Date touchTime = null;
                //Starts the touch capture
                try {
                    touchTime = dateFormatter.parse("1:30:00");
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Timer touchtimer = new Timer();
                touchtimer.schedule(new StartTouchTimer(MainActivity.this), touchTime);
            }
        });

        startNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start video playback
                videoView.start();
                //begin CPU testing
                CpuTest cpuTest = new CpuTest();
                cpuTest.runCpuTest();

                //begin disk test
                DiskTest diskTest = new DiskTest(MainActivity.this);
                diskTest.runDiskTest();

                //begin RAM test
                RamTest ramTest = new RamTest();
                ramTest.runRamTest();

                SharedPreferences sp = getSharedPreferences(MainActivity.PREF_FILE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("chamber_passed", 1);
                editor.commit();

                Intent intent = new Intent(MainActivity.this, TouchActivity.class);
                startActivity(intent);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releasePlayer();

                SharedPreferences sp = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("chamber_passed", 1);
                editor.putString("time_passed","032911152019.18");
                editor.commit();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializePlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N) videoView.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //clear shared preferences for chamber test
        switch(item.getItemId()){
            case R.id.eraseMenu:
                SharedPreferences sp = MainActivity.this.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
                reset(sp);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializePlayer(){
        String path = "android.resource://" + getPackageName() + "/" + R.raw.video_test;
        videoView.setVideoURI(Uri.parse(path));
    }

    private void releasePlayer()
    {
        videoView.stopPlayback();
    }

    //--------------------------------------------------------------------------------------------
    //Shared Preferences Functions
    //--------------------------------------------------------------------------------------------
    public static void reset(SharedPreferences sp){
        //Set String INI Defaults
        Map<String, Integer> intSettingsMap = new HashMap<>();
        intSettingsMap.put("is_chamber_running", 0);
        intSettingsMap.put("is_touch_captured", 0);
        intSettingsMap.put("chamber_passed", 0);

        SharedPreferences.Editor editor = sp.edit();

        for(Map.Entry<String, Integer> entry : intSettingsMap.entrySet()){
            editor.putInt(entry.getKey(), entry.getValue());
        }

        editor.apply();
    }

    public static void readInString(String fileString, SharedPreferences sharedPreferences){
        Splitter splitter = Splitter.onPattern("[\n\r]").trimResults();
        List<String> lines = splitter.splitToList(fileString);

        Map<String, Integer> intSettingsMap = new HashMap<>();

        //Read in Strings
        parsePairInt(lines, IS_CHAMBER_RUNNING, intSettingsMap);
        parsePairInt(lines, IS_TOUCH_CAPTURED, intSettingsMap);
        parsePairInt(lines, CHAMBER_PASSED, intSettingsMap);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        for(Map.Entry<String, Integer> entry : intSettingsMap.entrySet()){

            if(entry.getValue() != null){
                editor.putInt(entry.getKey(), entry.getValue());
            }
        }
        editor.apply();
        Log.d(TAG, "import done");
    }
    private static void parsePairInt(List<String> lines, int index, Map<String, Integer> settingsMap){
        if(lines.size() <= index){
            return;
        }
        Splitter splitter = Splitter.on(',').trimResults();
        String line = lines.get(index);
        List<String> strings = splitter.splitToList(line);

        settingsMap.put(strings.get(0), Ints.tryParse(strings.get(1)));
        //mSettingsPairs.add(new SettingsPair(strings.get(0), strings.get(1)));
    }

    //--------------------------------------------------------------------------------------------
}
