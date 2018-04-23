package com.cloudoc.yyd.yybgptestdemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

/**
 * @author : Vic
 *         time   : 2018/04/19
 *         desc   :
 */

public class BaseSerialPortGlucoseActivity extends AppCompatActivity {
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    /**
     * 串口路径 固定 格式为 血糖/dev/ttyMT1
     */
    private static final String PATHNAME = "/dev/ttyMT1";

    /**
     *  波特率 9600
     */
    private static final int BAUD_RATE = 9600;
    private static  BaseSerialPortGlucoseActivity baseSerialPortActivity = null;

    public static BaseSerialPortGlucoseActivity getInstance(){
        if(baseSerialPortActivity == null) {
            synchronized (BaseSerialPortGlucoseActivity.class) {
                if(baseSerialPortActivity == null) {
                    baseSerialPortActivity = new BaseSerialPortGlucoseActivity();
                }
            }
        }
        return baseSerialPortActivity;
    }



    HandlerThread sendingHandlerThread = new HandlerThread("sendingHandlerThread");

    {
        sendingHandlerThread.start();
    }

    private Handler sendingHandler = new Handler(sendingHandlerThread.getLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (mOutputStream != null) {
                try {
                    mOutputStream.write((byte[]) msg.obj);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };




    /**
     * 读取流中的数据
     */
    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    // 7 根据血糖发送文档中提供的发送的帧格式而定
                    byte[] buffer = new byte[8];
                    if (mInputStream == null) {
                        return;
                    }
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        int i = 0;
                        i++;
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    /**
     * 简单的错误弹出框
     * @param resourceId
     */
    private void DisplayError(String resourceId) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Error");
        b.setMessage(resourceId);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        b.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mSerialPort = openSerialPort();
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();

            //创建一个接收线程
            mReadThread = new ReadThread();
            mReadThread.start();
        } catch (SecurityException e) {
            // You do not have read/write permission to the serial port.
            DisplayError("You do not have read/write permission to the serial port.");
        } catch (IOException e) {
            // The serial port can not be opened for an unknown reason.
            DisplayError("The serial port can not be opened for an unknown reason.");
        } catch (InvalidParameterException e) {
            // Please configure your serial port first.
            DisplayError("Please configure your serial port first.");
        }
    }

    protected void onDataReceived(final byte[] buffer, final int size){};

    @Override
    protected void onDestroy() {
        if (mReadThread != null) {
            mReadThread.interrupt();
        }
        closeSerialPort();
        mSerialPort = null;
        if (null != sendingHandlerThread) {
            sendingHandlerThread.quit();
        }
        super.onDestroy();
    }

    /**
     * 发送测量命令
     * @param sendCmd
     */
    protected void sendCmd(byte[] sendCmd) {
        if (sendCmd == null && sendCmd.length == 0) {
            return;
        }
        Message message = Message.obtain();
        message.obj = sendCmd;
        sendingHandler.sendMessage(message);
    }

    /**
     * 打开串口
     * @return
     * @throws SecurityException
     * @throws IOException
     * @throws InvalidParameterException
     */
    private SerialPort openSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            mSerialPort = new SerialPort(new File(PATHNAME), BAUD_RATE, 0);
        }
        return mSerialPort;
    }

    /**
     * 关闭串口
     */
    private void closeSerialPort(){
        if(mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }
}
