package com.junipersys.a3_chamber_test;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static com.junipersys.a3_chamber_test.MainActivity.PREF_FILE;

public class SerialActivity extends AppCompatActivity {

    private Button saveSerial;
    private EditText serialText;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serial_layout);
        int savedSerialLength = GetKeyLength('S');

        saveSerial = findViewById(R.id.save_serial);
        serialText = findViewById(R.id.serial_input);
        saveSerial.setOnClickListener(v -> {
            boolean bResult = true;

            //Save Serial Key S
            String serial = serialText.getText().toString().trim();

            byte[] b2;
            b2 = serial.getBytes();
            if (!WriteKey('S', b2)) {
                AlertDialog.Builder dlg2 = new AlertDialog.Builder(SerialActivity.this);
                dlg2.setMessage("There was an error writing the 'S' key.");//may require a reboot
                dlg2.setNegativeButton("OK", null);
                dlg2.create();
                dlg2.show();
                bResult = false;
            }

            SharedPreferences sp = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("serial", serial);
            editor.commit();

            Intent intent = new Intent(SerialActivity.this, MainActivity.class);
            startActivity(intent);
        });

        if(savedSerialLength == 6){
            Intent intent = new Intent(SerialActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }
    //--------------------------------------------------------------------------------------------
    //Functions for EEPROM Keys
    //--------------------------------------------------------------------------------------------
    static {
        System.loadLibrary("a3-eeprom-keys-native-lib");
    }
    public native int GetKeyLength(char key);
    public native boolean ReadKey(char key, byte[] data);
    public native boolean WriteKey(char key, byte[] data);
    public native boolean EraseKey(char key);
    //--------------------------------------------------------------------------------------------


}
