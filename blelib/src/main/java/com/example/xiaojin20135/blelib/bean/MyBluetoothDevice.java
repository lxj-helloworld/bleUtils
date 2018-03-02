package com.example.xiaojin20135.blelib.bean;

import android.bluetooth.BluetoothDevice;

/**
 * Created by xiaojin20135 on 2018-03-01.
 * 自定义的蓝牙设备
 */

public class MyBluetoothDevice {
    private BluetoothDevice bluetoothDevice; //本次扫描的蓝牙设备
    private int rssi = 0;//信号强度

    public MyBluetoothDevice(BluetoothDevice bluetoothDevice,int rssi){
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String toString(){
        return this.bluetoothDevice.getName() + ", " + this.bluetoothDevice.getAddress() + ", " + this.rssi;
    }

}
