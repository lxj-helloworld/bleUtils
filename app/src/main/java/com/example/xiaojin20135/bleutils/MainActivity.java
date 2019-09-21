package com.example.xiaojin20135.bleutils;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.xiaojin20135.blelib.BleManager;
import com.example.xiaojin20135.blelib.activity.ScanListActivity;
import com.example.xiaojin20135.blelib.bean.MyBluetoothDevice;
import com.example.xiaojin20135.blelib.helps.BleConstant;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void sendEncrypt(View view){
        Intent intent = new Intent(MainActivity.this, ScanListActivity.class);
        intent.putExtra("className","com.example.xiaojin20135.bleutils.SendTestActivity");
        startActivity(intent);
    }
}
