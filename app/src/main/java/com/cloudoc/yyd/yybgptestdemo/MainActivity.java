package com.cloudoc.yyd.yybgptestdemo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;

public class MainActivity extends SerialPortActivity {


    StringBuilder pulseSb = new StringBuilder();
    StringBuilder pressSb = new StringBuilder();
    int pre = 0;
    int cur = 0;
    private String TAG = "MainActivity";

    private TextView tvTranslatingPulse;
    private TextView measure_count;
    private EditText second_edit, first_edit;
    private int mCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        EventBus.getDefault().register(this);
    }

    private void initView() {

        second_edit = (EditText) findViewById(R.id.second_edit);
        first_edit = (EditText) findViewById(R.id.first_edit);
        measure_count = (TextView) findViewById(R.id.measure_count);
    }

    @Override
    protected void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //处理串口传输过来的数据，进行相应的逻辑处理

            }
        });
    }

    /**
     * 判断是否发硬应答结果
     * 算法为计算校验和是否和接收到的校验code一致，如果一致则发送否则不发送
     * 计算校验码规则 ：最后一位为校验码，检验码的计算方法为:FA到DATA(n)的和然后模256，得到的结果即为检验码
     *
     * @param buffer
     * @return
     */
    private boolean isSendCheckResult(byte[] buffer) {
        int bufferLength = buffer.length;
        int sum = 0;
        for (int i = 0; i < bufferLength - 1; i++) {
            sum += (buffer[i] & 0xFF);
        }
        String receiveCheckCode = Integer.toHexString(buffer[10] & 0xFF);
        String calculateCheckCode = Integer.toHexString(sum % 256);

        if (receiveCheckCode.equals(calculateCheckCode)) {
            return true;
        }
        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }



    //发送数据
    public void send(byte[] sendCmd) {
        Log.d(TAG, "====sendCmd======" + sendCmd);
        if (sendCmd == null && sendCmd.length == 0) {
            return;
        }
        final Message message = Message.obtain();
        message.obj = sendCmd;

        sendingHandler.sendMessage(message);

    }



    /**
     * 控制电源
     *
     * @param context
     * @param value   0 表示关闭供电 1表示开启供电
     */
    public static void sendD1SMsg(Context context, int value) {
        File file = new File("/sys/medical_drv/medical_states");
        FileWriter mWriter = null;
        try {
            mWriter = new FileWriter(file);
            mWriter.write(String.valueOf(value));
            mWriter.flush();
            mWriter.close();
            mWriter = null;
        } catch (Exception e) {
            if (value == 1) {
                Toast.makeText(context, "血压计无法正常供电，请先结束测量再重新开始测量", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 加电
     *
     * @param view
     */
    public void startPowerOn(View view) {
        sendD1SMsg(this, 1);
    }

    /**
     * 断电
     *
     * @param view
     */
    public void endPowerOff(View view) {
        mCount = 1;
        sendD1SMsg(this, 0);
    }

    @Subscribe
    public void sendMeasureCMD(String key) {
        if (!TextUtils.isEmpty(key) && "turgoscope_init_success".equals(key)) {
        }
    }

    public void closeApp(View view) {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sendD1SMsg(this, 0);
    }
}
