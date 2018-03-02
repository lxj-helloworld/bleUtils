package com.example.xiaojin20135.blelib.helps;

import java.util.Date;

/**
 * Created by xiaojin20135 on 2018-02-28.
 */

public enum MethodsUtil {
    METHODS_UTIL;
    /**
     * 16进制数组转字符串
     * @param bytes
     * @return
     */
    public String byteToHexString(byte[] bytes){
        Date in = new Date();
        StringBuilder frameStr = new StringBuilder();
        if(bytes == null){
            return "";
        }
        for(int i=0;i<bytes.length;i++){
            frameStr.append(((Integer.toHexString(bytes[i] & 0xFF).length() < 2) ? ("0" + Integer.toHexString(bytes[i] & 0xFF)) : Integer.toHexString(bytes[i] & 0xFF)) + " ");
        }
        Date out = new Date();
        return frameStr.toString();
    }
}
