package com.example.xiaojin20135.blelib.helps;

import android.util.Log;

/**
 * Created by xiaojin20135 on 2018-02-28.
 */

public enum Encrypt {
    ENCRYPT;
    private static final String TAG = "Encrypt";
    public byte[] encryptMake(byte[] encryptIn){
       /* for(int t=0;t<encryptIn.length;t++){
            Log.d(TAG, "encrytIn[" + t + "] = " + Integer.toHexString(encryptIn[t] & 0xFF));
        }*/
        byte[] encrypt = new byte[16];
        int C1 = 52845;
        int C2 = 22719;
        int Key = 0x35ac;
        //依次对字符串中各字符进行操作
        for(int i=0;i<8;i++){
//            Log.d(TAG,"前 encryptIn["+i+"] = " + Integer.toHexString(encryptIn[i] & 0xFF));
            encryptIn[i] = (byte)((encryptIn[i] & 0xFF) ^ (Key >> 8));
            Key = ((((encryptIn[i] & 0xFF) + Key)*C1 + C2) & 0xFFFF);
//            Log.d(TAG,"后 encryptIn["+i+"] = " + Integer.toHexString(encryptIn[i] & 0xFF));
        }

        Log.d(TAG, "encryptIn = " + MethodsUtil.METHODS_UTIL.byteToHexString(encryptIn));
        //对加密结果进行转换
        for(int i=0;i<8;i++){
            encrypt[i*2] = (byte)((encryptIn[i] & 0xFF)/26 + 65);
            encrypt[i*2 + 1] = (byte)((encryptIn[i] & 0xFF)%26 + 65);
        }
//        for(int t=0;t<encrypt.length;t++){
//            Log.d(TAG,"encrypt["+t+"] = " + Integer.toHexString(encrypt[t]&0xFF));
//        }
        return encrypt;
    }
}
