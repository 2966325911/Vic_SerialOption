package com.cloudoc.yyd.yybgptestdemo;

import android.content.Context;

import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;



public class SystemUtil {

    /**
     * 控制电源
     * @param context
     * @param value  0 表示关闭供电 1表示开启供电
     */
    public static void sendD1SMsg(Context context,int value){
        File file = new File("/sys/medical_drv/medical_states");
        FileWriter mWriter = null;
        try {
            mWriter = new FileWriter(file);
            mWriter.write(String.valueOf(value));
            mWriter.flush();
            mWriter.close();
            mWriter = null;
        } catch (Exception e) {
            if(value == 1) {
                Toast.makeText(context,"血压计无法正常供电，请先结束测量再重新开始测量",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     *  控制电源 0 表示关闭供电 1表示开启供电
     */
    public static int getDeviceState(Context context){
        File file = new File("/sys/medical_drv/medical_states");
        FileReader mReader = null;
        int value = 0;
        BufferedReader br = null;
        String s = null;
        try {
            mReader = new FileReader(file);
            br = new BufferedReader(mReader);
            while((s = br.readLine()) != null) {
                value = Integer.parseInt(s);
            }
            mReader.close();
            mReader = null;
        } catch (Exception e) {
            Toast.makeText(context,"血压计无法正常供电，请先结束测量再重新开始测量",Toast.LENGTH_SHORT).show();
        }
        return value;
    }

    /**
     * 控制电源 血糖仪
     * @param context
     * @param value  0 表示关闭供电 1表示开启供电
     */
    public static void glucoseSupplyPower(Context context,int value){
        File file = new File("sys/medical_drv/sugar_states");
        FileWriter mWriter = null;
        try {
            mWriter = new FileWriter(file);
            mWriter.write(String.valueOf(value));
            mWriter.flush();
            mWriter.close();
            mWriter = null;
        } catch (Exception e) {
            if(value == 1) {
                Toast.makeText(context,"血糖仪无法正常供电，请先结束测量再重新开始测量",Toast.LENGTH_SHORT).show();
            }
        }
    }
}