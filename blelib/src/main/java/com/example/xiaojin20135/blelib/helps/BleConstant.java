package com.example.xiaojin20135.blelib.helps;
/**
 * Created by xiaojin20135 on 2018-03-01.
 *              * @param newState 连接状态：
 *                 0：in disconnected state
 *                 1：in connecting state
 *                 2：in connected state
 *                 3：in disconnecting state
 */
public enum BleConstant {
    BLE_CONSTANT;
    public static final int DISCONNECTED = 0; //连接断开
    public static final int CONNECTIONING = 1; //正在连接
    public static final int CONNECTED= 2; //连接成功
    public static final int DISCONNECTING = 3; //正在断开
    public static final int CONNECTDONE = 4;//连接后加密认证完成，可跳转
    public static final int LOWVERSION = 5;//系统版本过低，无法设置MTU

    //发现新设备what值
    public static final int SCANNEWDEVICE = 10;
    //开始连接
    public static final int STARTCONNECT = 11;
    //发送失败，重新发送
    public static final int SENDFAILED_TRY = 12;
    //发送成功
    public static final int SEND_SUCCESS = 13;
}
