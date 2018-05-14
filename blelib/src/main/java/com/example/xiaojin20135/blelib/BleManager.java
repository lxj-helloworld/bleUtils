package com.example.xiaojin20135.blelib;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.xiaojin20135.blelib.bean.MyBluetoothDevice;
import com.example.xiaojin20135.blelib.helps.BleConstant;
import com.example.xiaojin20135.blelib.helps.DatasBuffer;
import com.example.xiaojin20135.blelib.helps.Encrypt;
import com.example.xiaojin20135.blelib.helps.MethodsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.xiaojin20135.blelib.helps.BleConstant.SCANNEWDEVICE;
import static com.example.xiaojin20135.blelib.helps.BleConstant.SENDFAILED_TRY;
import static com.example.xiaojin20135.blelib.helps.BleConstant.STARTCONNECT;

/**
 * Created by xiaojin20135 on 2018-02-28.
 */
public enum BleManager {
    BLE_MANAGER;
    private static final String TAG = "BleManager";
    /**
     * 本地蓝牙适配器，是所有蓝牙交互的入口点，利用其可以发现其他蓝牙设备，查询绑定设备的列表，
     * 使用已知的MAC地址实例化BluetoothDevice，以及创建BluetoothServerSocket来侦听来自其他设备的通信。
     */
    private BluetoothAdapter mBluetoothAdapter;
    //是否真在扫描
    private boolean mScanning;
    //UUID特征字，需要配置硬件修改
    private UUID UUID_SERVICE; //服务
    private UUID UUID_WRITE; //用于发送数据到设备
    private UUID UUID_NOTIFICATION; // 用于接收蓝牙设备发送的数据
    private UUID UUID_CONFIRM; //发送命令，触发蓝牙设备发送加密串，开启蓝牙认证
    private UUID UUID_NOTIFICATION_DES2; //

    //GATT是用于发送和接收的通用规范，BLE之间的文件传输数据传输都是基于GATT，因此在进行连接之前，需要进行GATT接口回调。
    private BluetoothGatt mBluetoothGatt;
    private boolean isServiceConnected;
    //写特征字
    BluetoothGattCharacteristic mWriteCharacteristic;
    //通知特征字
    BluetoothGattCharacteristic mNotificationCharacteristic;
    //确认特征字
    BluetoothGattCharacteristic mConfirmCharacteristic;
    /**
     * 表示远程蓝牙设备，利用他可以通过Bluetoothsocket请求与某个远程设备建立连接
     */
    private BluetoothDevice mDevice;
    private Activity activity;
    //当前扫描到的设备列表 ,设备加信号强度
    private List<MyBluetoothDevice> deviceList = new ArrayList<>();
    //新发现设备通知
    private Handler bleHandler;
    //发送标示
    private Handler sendHandler;
    //当前发送的分帧
    private byte[] currentFrame = {};
    //当前发送的完整帧
    private byte[] currentCompleteFrame = {};


    private DatasBuffer datasBuffer;

    //标示是否发送成功，如果发送成功，收到成功回调之后，继续发送下一帧，如果处于发送失败状态，即使收到成功回调也不发送下一帧
    public static boolean sendSuccess = false;
    //是否自动配对
    private boolean ble_auto_connected_key;
    //自动配对的蓝牙设备Address
    private String bleAddress = "";




    /**
     * 初始化特征字
     * @param uuid_service
     * @param uuid_write
     * @param uuid_notification
     * @param uuid_confirm
     * @param uuid_notification_des2
     */
    public void init(String uuid_service,String uuid_write,String uuid_notification,String uuid_confirm,String uuid_notification_des2,Activity activity){
        UUID_SERVICE = UUID.fromString(uuid_service);
        UUID_WRITE = UUID.fromString(uuid_write);
        UUID_NOTIFICATION = UUID.fromString(uuid_notification);
        UUID_CONFIRM = UUID.fromString(uuid_confirm);
        UUID_NOTIFICATION_DES2 = UUID.fromString(uuid_notification_des2);
        this.activity = activity;
        ble_auto_connected_key = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("ble_auto_connected_key",true);
        bleAddress = PreferenceManager.getDefaultSharedPreferences(activity).getString ("bleAddress","C8:FD:19:86:DC:0C");
        datasBuffer = DatasBuffer.DATAS_BUFFER;
        initBle();

    }

    /**
     * 前期准备
     */
    public void initBle(){
        //初始化BLE manager和适配器
        final BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        //如果蓝牙未打开，提示用户打开蓝牙
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, 1);
        }
    }

    /**
     * 开始扫描
     */
    public void startScan(){
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, 1);
        }
        Log.d(TAG,"mScanning = " +mScanning);
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        //扫描前清理当前已经扫描到的设备列表
        deviceList.clear();
        scanLeDevice(true);
    }


    /**
     * 停止扫描
     */
    public void stopScan(){
        Log.d (TAG,"尝试停止扫描！");
        if (mBluetoothAdapter != null) {
            scanLeDevice(false);
            Log.d (TAG,"停止扫描成功");
        }else{
            Log.d (TAG,"mBluetoothAdapter is null or mBluetoothAdapter is disenable");
        }

    }

    /**
     * 扫描周围的蓝牙设备
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mScanning = true;
            // 定义一个回调接口供扫描结束处理
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            Log.d(TAG,"停止扫描");
        }
    }
    /**
     * 蓝牙扫描回调
     */
    final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            String name = device.getName();
            Log.d(TAG, "in onLeScan. name =" + name + " : " + rssi);
            if (name != null && !name.equals("")) {
                //判断新扫描的设备是否已经存在
                for(int i=0;i<deviceList.size();i++){
                    Log.d(TAG,"deviceList.get(i).getBluetoothDevice().getAddress()");
                    Log.d(TAG,"device.getAddress() = " + device.getAddress());
                    if(deviceList.get(i).getBluetoothDevice().getAddress().equals(device.getAddress())){
                        return;
                    }
                }
                Log.d(TAG,"设备不存在，添加");
                //添加到设备列表
                MyBluetoothDevice myBluetoothDevice = new MyBluetoothDevice(device,rssi);
                deviceList.add(myBluetoothDevice);
                sendStateChange(SCANNEWDEVICE,"");
                //判断是是否需要自动连接 如果设置为自动连接，并且蓝牙地址不为空，并且地址匹配
                if(ble_auto_connected_key && !bleAddress.equals ("") && device.getAddress ().equals (bleAddress)){
                    setmDevice(device);
                    startConnect();
                }
            }else{
                Log.d(TAG,"扫描设备名称获取失败，未识别的设备。");
            }
        }
    };
    /**
     * 开始连接
     */
    public void startConnect() {
        //已选中的扫描设备不为空，尝试连接。
        if (mDevice != null) {
            //停止扫描
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            Log.d(TAG,"stop scan.");
            if (mBluetoothGatt != null) {
                //断开当前已经建立的一个连接，或者去掉当前正在尝试建立的连接
                mBluetoothGatt.disconnect();
                //关闭 GATT client
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            }else{
            }
            //发送开始连接通知
            sendStateChange(STARTCONNECT,"");
            //连接到GATT server
            mBluetoothGatt = mDevice.connectGatt(activity, false, mGattCallback);
        }else{
            Log.d(TAG,"mDevice is null");
        }
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        /**
         * Callback indicating when GATT client has connected/disconnected to/from a remote GATT server.
         * @param gatt
         * @param status
         * @param newState 连接状态：
         *                 0：in disconnected state
         *                 1：in connecting state
         *                 2：in connected state
         *                 3：in disconnecting state
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG," in onConnectionStateChange. status = " + status);
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(TAG, "连接状态发生变化: " + newState);
            sendStateChange(newState,"");
            if (status != BluetoothGatt.GATT_SUCCESS) {
                String err = "连接失败：" + status;
                gatt.close();
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                }
                if (mDevice != null) {
                    mBluetoothGatt = mDevice.connectGatt(activity, false, mGattCallback);
                }
                Log.d(TAG, err);
                sendStateChange(newState,"err");
                return;
            }
            if(newState == 0){
                sendStateChange(newState,"err");
                Log.d(TAG,"蓝牙断开");
            }
            //已连接 当蓝牙设备已经连接 获取ble设备上面的服务
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "连接成功，读取Services。");
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {//当设备无法连接
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                }
                gatt.close();
                if (mDevice != null) {
//                    mBluetoothGatt = mDevice.connectGatt(activity, true, mGattCallback);
                }
            }
        }
        //Callback invoked when the list of remote services, characteristics and descriptors for the remote device have been updated, ie new services have been discovered.
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG," in onServicesDiscovered. status = " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                isServiceConnected = true;
                boolean serviceFound;
                if (mBluetoothGatt != null && isServiceConnected) {
                    //得到想要的Servce
                    BluetoothGattService gattService = mBluetoothGatt.getService(UUID_SERVICE);
                    //得到写特征字
                    mWriteCharacteristic = gattService.getCharacteristic(UUID_WRITE);
                    mWriteCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                    Log.d(TAG,"mWriteCharacteristic  = " + mWriteCharacteristic);
                    Log.d(TAG,"mWriteCharacteristic.getWriteType = " + mWriteCharacteristic.getWriteType());
                    //确认特征字
                    mConfirmCharacteristic = gattService.getCharacteristic(UUID_CONFIRM);
                    mConfirmCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                    Log.d(TAG,"mConfirmCharacteristic  = " + mConfirmCharacteristic);
                    Log.d(TAG,"mConfirmCharacteristic.getWriteType = " + mConfirmCharacteristic.getWriteType());
                    //得到 通知 Characteristic
                    mNotificationCharacteristic = gattService.getCharacteristic(UUID_NOTIFICATION);
                    Log.d(TAG,"mNotificationCharacteristic  = " + mNotificationCharacteristic);
                    boolean b = mBluetoothGatt.setCharacteristicNotification(mNotificationCharacteristic, true);
                    if(b){
                        List<BluetoothGattDescriptor> list = mNotificationCharacteristic.getDescriptors();
                        BluetoothGattDescriptor descriptor1 = mNotificationCharacteristic.getDescriptor(UUID_NOTIFICATION_DES2);
                        Log.d(TAG,"descriptor1.getPermissions = " + descriptor1.getPermissions());
                        boolean b1 = descriptor1.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        if (b1) {
                            boolean setResult = mBluetoothGatt.writeDescriptor(descriptor1);
                            Log.d(TAG,"setResult = " + setResult);
                            if(setResult){
                                Log.d(TAG, "Enable 通知成功！");

                            }else{
                                Log.d(TAG, "Enable 通知失败");
                            }
                        }else{
                            Log.d(TAG, "Enable 设置值失败！");
                        }
                    }else{
                        Log.d(TAG,"设置CharacteristicNotification失败，不处理");
                    }
                }else {
                    Log.d(TAG,"mBluetoothGatt = " + mBluetoothGatt + " isServiceConnected = " + isServiceConnected);

                }
            }else{
                Log.d(TAG,"status != BluetoothGatt.GATT_SUCCESS");
            }
        }
        //Callback reporting the result of a characteristic read operation.
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG," in onCharacteristicRead.");
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "read value: " + characteristic.getValue());
            }
        }
        //Callback indicating the result of a characteristic write operation.
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if(status == BluetoothGatt.GATT_SUCCESS){
                sendConfirm();
                Log.d(TAG, "onDescriptorWrite: " + "设置成功");
            }else{
                Log.d(TAG, "onDescriptorWrite: " + "设置失败 status = " + status);
            }
        }
        //Callback reporting the result of a descriptor read operation.
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG," in onDescriptorRead.");
            super.onDescriptorRead(gatt, descriptor, status);
        }
        //Callback indicating the result of a descriptor write operation.
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            Log.d(TAG," in onCharacteristicWrite.发送数据到蓝牙设备。");
            super.onCharacteristicWrite(gatt, characteristic, status);
            if(status == BluetoothGatt.GATT_SUCCESS && sendSuccess){
                Log.d(TAG, "onCharacteristicWrite: " + "发送成功: " + " " + MethodsUtil.METHODS_UTIL.byteToHexString(characteristic.getValue()));
                //如果发送成功，移除当前发送成功的帧，并且发送下一帧
                datasBuffer.clearFirstSended();
                sendData(datasBuffer.getFirstToSend());
            }else{
                //如果发送失败，
                Log.d(TAG, "onCharacteristicWrite: " + "发送失败 status = " + status);
            }
        }
        //Callback triggered as a result of a remote characteristic notification.
        @Override
        public final void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
//            Log.d(TAG,"on onCharacteristicChanged. 收到蓝牙设备发来的数据。");
            byte[] value = characteristic.getValue();
            if(value != null){
                receiveManage(value);
            }else{
                Log.d(TAG,"value is null ");
            }
        }
        //Callback invoked when a reliable write transaction has been completed.
        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            Log.d(TAG," in onReliableWriteCompleted.");
            super.onReliableWriteCompleted(gatt, status);
        }
        //Callback reporting the RSSI for a remote device connection.
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(TAG," in onReadRemoteRssi.");
            super.onReadRemoteRssi(gatt, rssi, status);
        }
        //Callback indicating the MTU for a given device connection has changed.
        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Log.d(TAG," in onMtuChanged. mtu = " + mtu + " ; status = " +status);
            super.onMtuChanged(gatt, mtu, status);
        }
    };
    /**
     * 发送数据到蓝牙设备,如果发送成功，返回true，否则，返回false
     * @param datas
     */
    private boolean sendData(byte[] datas){
        currentFrame = datas;
        boolean sendResult = false;
        if(mWriteCharacteristic != null && datas != null){
            mWriteCharacteristic.setValue(datas);
            mWriteCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            sendResult = mBluetoothGatt.writeCharacteristic(mWriteCharacteristic);
            //设置当前分帧发送成功以及失败标识，
            // 如果发送成功，在onCharacteristicWrite回调成功后，开始发送下一帧，
            //如果发送失败，即使收到onCharacteristicWrite回到成功，也不继续发送下一帧，等待发送失败的再次发送
            sendSuccess  = sendResult;
            if(sendResult){
                Log.d(TAG,"in sendData . 发送成功:" + MethodsUtil.METHODS_UTIL.byteToHexString(datas));
            }else{
                Log.d(TAG,"in sendData . 发送失败:" + MethodsUtil.METHODS_UTIL.byteToHexString(datas));
                //发送失败，重新发送
                sendHandlerStatus(SENDFAILED_TRY,"");
            }
        }else{
           Log.d(TAG,"mWriteCharacteristic is null or datas is null.");
        }
        return sendResult;
    }

    /**
     * 分帧发送失败， 重试
     * @return
     */
    public boolean sendTry(){
        Log.d(TAG,"sendTry 发送失败，重新发送:" + MethodsUtil.METHODS_UTIL.byteToHexString(currentFrame));
        return sendData(currentFrame);
    }

    /**
     * 完整帧发送失败，重试
     * @return
     */
    public boolean sendComplete(){
        Log.d(TAG,"sendTry 发送失败，重新发送:" + MethodsUtil.METHODS_UTIL.byteToHexString(currentCompleteFrame));
        return sendSliceData(currentCompleteFrame);
    }

    /**
     * 分帧发送
     * @param datas
     * @return
     */
    public boolean sendSliceData(byte[] datas){
        //记录当前发送的完整报文
        currentCompleteFrame = datas;
        Log.d(TAG, "in sendSliceData. datas = " + MethodsUtil.METHODS_UTIL.byteToHexString(datas));
        //清理当前需要发送的数据
        datasBuffer.clearToSend();
        boolean sendResult = false;
        if(mWriteCharacteristic != null){
            //本次报文长度
            int len = datas.length;
            Log.d(TAG,"len = " + len);
            //分帧个数
            int count =  (len / 18);
            if(len%18 != 0){
                count = count + 1;
            }
//            Log.d(TAG,"count = " + count + "  Math.ceil(len / 18) = " + Math.ceil(len / 18));
            int sylen = len;
            int from = 0;
            for(int i=0;i<count;i++){
                //帧
                byte[] frame = new byte[19];
                int length=0;
                length=sylen;
                if((sylen/(18+1))<1){
                    frame[0]=(byte)(0x80|i);
                    sylen=sylen%19;
                }else{
                    frame[0]=(byte)i;
                    sylen=sylen-18;
                }
                for(int k=0;k<18;k++){
                    if(k<length){
//                        Log.d(TAG,"k+1 = " + (k+1) + " ; (from + k) = " + (from + k));
                        frame[k+1]=datas[from+k];
                    }else{
                        frame[k+1]=0;
                    }
                }
                from=from+18;
//                Log.d(TAG,"frame = " + MethodsUtil.METHODS_UTIL.byteToHexString(frame));
                datasBuffer.addToSend(frame);
            }
        }else{
            Log.d(TAG,"mWriteCharacteristic is null");
        }
        //如果待发送报文数量大于0，开始发送
        if(datasBuffer.getFrameToSend().size() > 0){
            //开始发送分帧
            sendResult = sendData(datasBuffer.getFirstToSend());
            Log.d(TAG,"发送结果。 in sendSliceData, sendResult = " + sendResult);
        }
        return sendResult;
    }
    /**
     * 断开连接
     */
    public void stopConnect(){
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
    }
    /**
     * 接受报文处理
     */
    private void receiveManage(byte[] receiveArr){
        Log.d(TAG,"receiveArr = " + MethodsUtil.METHODS_UTIL.byteToHexString(receiveArr));
        byte firstData = (byte)(receiveArr[0]);
//        Log.d(TAG,"firstData = " + Integer.toHexString(firstData & 0xFF));
        if((firstData & 0xFF) == 0xFF){ //蓝牙设备发来的加密串，
            Log.d(TAG,"加密认证中");
            sendEncrypt(receiveArr);
        }else if((firstData & 0xFF)== 0xFE){ //加密认证过程完成
            Log.d(TAG,"加密认证过程完成");
            sendStateChange(BleConstant.CONNECTDONE,"");
        }else{
            DatasBuffer.DATAS_BUFFER.addFrame(receiveArr);
        }
    }
    /**
     * 通知蓝牙设备开始加密认证
     * 连接成功后发送，通知蓝牙设备发送加密串，进行认证；
     */
    private boolean sendConfirm(){
        boolean sendResult = false;
        byte[] datas = {(byte)0xAA};
        if(mConfirmCharacteristic != null){
            Log.d(TAG,"in sendData. datas.length = " + datas.length);
            mConfirmCharacteristic.setValue(datas);
            mConfirmCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            sendResult = mBluetoothGatt.writeCharacteristic(mConfirmCharacteristic);

            Log.d(TAG,"sendConfirm sendResult = " + sendResult);
        }else{
            Log.d(TAG,"mConfirmCharacteristic is null");
        }
        return sendResult;
    }
    /**
     * 处理解密串，并返回
     * @param encryptIn
     * @return
     */
    public boolean sendEncrypt(byte[] encryptIn){
        Log.d(TAG, "in sendEncrypt");
        //从第二个数据开始，取九个数据
        byte[] temp = new byte[9];
        for(int i=1;i<9;i++){
            temp[i-1] = encryptIn[i];
        }
        byte[] encrypt = Encrypt.ENCRYPT.encryptMake(temp);
        byte[] encryptResult = new byte[19];
        encryptResult[0] = (byte)0xFF;
        for(int i=0;i<16;i++){
            encryptResult[i+1] = encrypt[i];
        }
        encryptResult[17] = 0x00;
        encryptResult[18] = 0x00;
        return sendData(encryptResult);
    }
    /**
     * 返回当前已经扫描到的设备的列表
     * @return
     */
    public List<MyBluetoothDevice> getDevicesList(){
        return deviceList;
    }

    public BluetoothDevice getmDevice() {
        return mDevice;
    }

    public void setmDevice(BluetoothDevice mDevice) {
        this.mDevice = mDevice;
    }

    public Handler getBleHandler() {
        return bleHandler;
    }

    public void setBleHandler(Handler bleHandler) {
        this.bleHandler = bleHandler;
    }

    /**
     * 蓝牙动作，扫描新设备、开始连接、连接状态变化
     * @param state
     */
    public void sendStateChange(int state,String obj){
        Log.d(TAG,"sendStateChange. state = " + state);
        if(bleHandler != null){
            Message message = new Message();
            message.what = state;
            message.obj = obj;
            bleHandler.sendMessage(message);
        }else{
            Log.d(TAG,"handler is null.");
        }
    }

    /**
     * 标示发送失败
     * @param state
     * @param obj
     */
    public void sendHandlerStatus(int state,String obj){
        Log.d(TAG,"sendHandlerStatus. state = " + state);
        if(sendHandler != null){
            Message message = new Message();
            message.what = state;
            message.obj = obj;
            sendHandler.sendMessage(message);
        }else{
            Log.d(TAG,"handler is null.");
        }
    }

    public Handler getSendHandler() {
        return sendHandler;
    }

    public void setSendHandler(Handler sendHandler) {
        this.sendHandler = sendHandler;
    }
}
