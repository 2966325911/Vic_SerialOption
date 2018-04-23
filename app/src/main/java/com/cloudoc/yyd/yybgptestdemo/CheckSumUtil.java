package com.cloudoc.yyd.yybgptestdemo;

/**
 * @author : Vic
 * time   : 2018/04/23
 * desc   :  文档中用的是校验和算法,8位校验和用于计算校验码
 */
public class CheckSumUtil {
    /**
     * 8 位校验和
     *
     * @param msg 需要计算校验和的byte数组
     * @param length 校验和位数
     * @return 计算出的校验和数组
     */
    private static byte[] sumCheck(byte[] msg, int length) {
        long mSum = 0;
        byte[] mByte = new byte[length];

        /** 逐Byte添加位数和 */
        for (byte byteMsg : msg) {
            long mNum = ((long)byteMsg >= 0) ? (long)byteMsg : ((long)byteMsg + 256);
            mSum += mNum;
        }

        /** 位数和转化为Byte数组 */
        for (int liv_Count = 0; liv_Count < length; liv_Count++) {
            mByte[length - liv_Count - 1] = (byte)(mSum >> (liv_Count * 8) & 0xff);
        }

        return mByte;
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

            String hv = Integer.toHexString(v);

            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
    }


    /**
     这也是计算方式
     */
    public static byte[] checkSum(byte[] data) {

        int i,sum=0;
        for (i=0;i<data.length;i++) {
            sum+=data[i];//将每个数相加
            if(sum>0xff){
                sum=~sum;
                sum+=1;
            }
        }
        sum = sum & 0xFF;
        return intToBytes(sum);
    }

    public static byte[] intToBytes( int value )
    {
        byte[] src = new byte[4];
        src[3] =  (byte) ((value>>24) & 0xFF);
        src[2] =  (byte) ((value>>16) & 0xFF);
        src[1] =  (byte) ((value>>8) & 0xFF);
        src[0] =  (byte) (value & 0xFF);
        return src;
    }
}
