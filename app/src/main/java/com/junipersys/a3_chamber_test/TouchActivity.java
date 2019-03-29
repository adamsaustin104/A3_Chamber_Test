package com.junipersys.a3_chamber_test;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TouchActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String FILE_NAME = "Touch_Log.txt";
    private float[] lastTouchDownXY = new float[2];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.touch_layout);

        Button touch_detect = findViewById(R.id.touch_detect_button);

        Intent intent = getIntent();

        touch_detect.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // save the X,Y coordinates
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    lastTouchDownXY[0] = event.getX();
                    lastTouchDownXY[1] = event.getY();
                }
                // let the touch event pass on to whoever needs it
                return false;
            }
        });
        touch_detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // retrieve the stored coordinates
                float x = lastTouchDownXY[0];
                float y = lastTouchDownXY[1];
                SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");

                formatter.setTimeZone(TimeZone.getTimeZone("America/Denver"));
                Calendar cal = Calendar.getInstance();
                Date dateTime = cal.getTime();

                String text = "Touch Detected: (X:" + x + ", Y:" + y + ") at " + formatter.format(dateTime);
                FileOutputStream fos = null;

                try {
                    fos = openFileOutput(FILE_NAME, MODE_APPEND);

                    fos.write(text.getBytes());

                    View contextView = findViewById(R.id.touch_detect_button);

                    Snackbar sb = Snackbar.make(contextView, R.string.touch_logged, Snackbar.LENGTH_INDEFINITE);
                    sb.setAction(R.string.goToLogFile, TouchActivity.this::onClick);
                    sb.show();
                    //Toast.makeText(TouchActivity.this, "Saved to " + getFilesDir() + "/" + FILE_NAME, Toast.LENGTH_SHORT).show();

                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public void onClick(View v) {
        File file = new File(v.getContext().getFilesDir(), TouchActivity.FILE_NAME);
        Uri uri = FileProvider.getUriForFile(TouchActivity.this,
                BuildConfig.APPLICATION_ID + ".provider",
                file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        v.getContext().startActivity(intent);
    }
}
