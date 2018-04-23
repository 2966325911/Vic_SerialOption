package com.cloudoc.yyd.yybgptestdemo;

/**
 * author : yuyandong
 * time   : 2017/06/21
 * desc   :
 * version: 1.0
 */

/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {

    private static final String TAG = "SerialPort";
    private FileDescriptor mFd; //文件描述
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    /**
     *  获得一个窗口
     * @param device 设备
     * @param baudrate 波特率
     * @param flags 标志
     * @throws SecurityException
     * @throws IOException
     */
    public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {

		//检查权限
        if (!device.canRead() || !device.canWrite()) {
            try {
                //如果丢失权限读写权限，就再获取权限
                Process su;
                su = Runtime.getRuntime().exec("/system/xbin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
                        + "exit\n";
                // 写命令
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead()
                        || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }

        //打开设备，这里调用jni的open方法
        mFd = open(device.getAbsolutePath(), baudrate, flags);
        Log.d(TAG,"=======串口打开了========");
        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    // JNI

    /**
     * 打开串口设备的方法
     * @param path 设备的绝对路径
     * @param baudrate 波特率
     * @param flags 标志
     * @return
     */
    private native static FileDescriptor open(String path, int baudrate, int flags);
    //关闭设备
    public native void close();

    //加载库文件
    static {
        System.loadLibrary("SerialPort");
//        System.loadLibrary("serial_port");
    }
}
