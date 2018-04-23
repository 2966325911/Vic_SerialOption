package com.cloudoc.yyd.yybgptestdemo;

import android.nfc.Tag;
import android.util.Log;

/**
 * author : yuyandong
 * time   : 2017/06/28
 * desc   :
 * version: 1.0
 */

public class SystemsConvertUtils {
    private static String TAG = "SystemsConvertUtils";
//    /**
//     * 16进制转化为字符串
//     * @param bytes
//     * @return
//     */
//    public static String bytesToHexString(byte[] bytes) {
//        String result = "";
//        for (int i = 0; i < bytes.length; i++) {
//            String hexString = Integer.toHexString(bytes[i] & 0xFF);
//            if (hexString.length() == 1) {
//                hexString = '0' + hexString;
//            }
//            result += hexString.toUpperCase();
//        }
//        return result;
//    }

    public static byte[] hex2byte(String hex) {
        String digital = "0123456789ABCDEF";
        String hex1 = hex.replace(" ", "");
        char[] hex2char = hex1.toCharArray();
        byte[] bytes = new byte[hex1.length() / 2];
        byte temp;
        for (int p = 0; p < bytes.length; p++) {
            temp = (byte) (digital.indexOf(hex2char[2 * p]) * 16);
            temp += digital.indexOf(hex2char[2 * p + 1]);
            bytes[p] = (byte) (temp & 0xff);
        }
        return bytes;
    }

    /**
     * 16进制字符串转换为byte[]
     *
     * @param hexString
     * @return
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase().replace(" ", "");
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * byte[]转换成16进制字符串
     *
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            Log.d(TAG,"=========v===========" + v + "===i====" + i);
            String hv = Integer.toHexString(v);
            Log.d(TAG,"=========hv===========" + hv);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
}
