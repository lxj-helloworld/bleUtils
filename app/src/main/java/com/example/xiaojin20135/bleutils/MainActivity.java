package com.example.xiaojin20135.bleutils;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.xiaojin20135.blelib.BleManager;
import com.example.xiaojin20135.blelib.bean.MyBluetoothDevice;
import com.example.xiaojin20135.blelib.helps.BleConstant;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";
    BleManager bleManager = BleManager.BLE_MANAGER;
    private String uuid_service = "0000fff0-0000-1000-8000-00805f9b34fb";
    private String uuid_write = "0000fff6-0000-1000-8000-00805f9b34fb";
    private String uuid_notification = "0000fff4-0000-1000-8000-00805f9b34fb";
    private String uuid_confirm = "0000fff3-0000-1000-8000-00805f9b34fb";
    private String uuid_notification_des2 = "00002902-0000-1000-8000-00805f9b34fb";
    private Handler handler;
    //当前扫描到的设备列表 ,设备加信号强度
    private List<MyBluetoothDevice> deviceList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        bleManager.init(uuid_service,uuid_write,uuid_notification,uuid_confirm,uuid_notification_des2,this);
//        initHandler();
    }

    private void initHandler(){
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG,"msg.what = " + msg.what);
                super.handleMessage(msg);
                if(msg.what == BleConstant.SCANNEWDEVICE){//发现新设备
                    deviceList = bleManager.getDevicesList();
                    Log.d(TAG,"deviceList = " + deviceList.toString());
                    MyBluetoothDevice myBluetoothDevice = deviceList.get(deviceList.size()-1);
                    Log.d(TAG,"bluetoothDevice.getAddress() = " + myBluetoothDevice.toString());
                }
            }
        };
        bleManager.setBleHandler(handler);
    }

    public void startScan(View view) {

        bleManager.startScan();
    }

    public void startConnect(View view){
        bleManager.startConnect();
    }

    public void sendEncrypt(View view){
      /*  byte[] temp = {0x3b,0x59,0x74,(byte)0x38,0x1d,(byte)0x85,(byte)0x4f,(byte)0x5c,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
        byte[] result = Encrypt.ENCRYPT.encryptMake(temp);
        Log.d(TAG, "result = " + MethodsUtil.METHODS_UTIL.byteToHexString(result));*/
        Intent intent = new Intent(MainActivity.this,ScanListActivity.class);
        startActivity(intent);
    }
}
