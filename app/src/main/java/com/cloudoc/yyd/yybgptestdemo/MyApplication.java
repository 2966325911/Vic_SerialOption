package com.cloudoc.yyd.yybgptestdemo;

import android.app.Application;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;


/**
 * Created by kqw on 2016/10/26.
 * MyApplication
 */

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";
//    public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
    private SerialPort mSerialPort = null;
    private String path = "/dev/ttyMT2";

    //打开串口
    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            mSerialPort = new SerialPort(new File(path), 9600, 0);
            Log.d(TAG,"getSerialPort执行了");
        }
        return mSerialPort;
    }

    //关闭串口
    public void  closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }
}
