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


    //启动测量命令 CMD = 01
    byte[] startMeasureCmd = new byte[]{(byte) 0xFA, (byte) 0xFB, 0x01, 0x01, 0x01, 0x01, (byte) 0xF9};

    //停止测量命令  CMD = 05;
    byte[] stopMeasureCmd = new byte[]{(byte) 0xFA, (byte) 0xFB, 0x05, 0x01, 0x01, 0x01, (byte) 0xFD};

    //发送错误结果主机应答帧 命令码 CMD = 04;
    byte[] onErrorResponseCmd = new byte[]{(byte) 0xFA, (byte) 0xFB, 0x04, 0x01, 0x01, 0x01, (byte) 0xFC};
    //接受到正确结果应答帧 CMD = 03;
    byte[] mOnCorrectResponseCmd = new byte[]{(byte) 0xFA, (byte) 0xFB, 0x03, 0x01, 0x01, 0x01, (byte) 0xFB};
    //发送测量过程中压力值&脉搏强度(心率)应答帧 CMD = 02 (可以不发给从站)
    byte[] mOnMeasuringResponseCmd = new byte[]{(byte) 0xFA, (byte) 0xFB, 0x02, 0x01, 0x01, 0x01, (byte) 0xFA};
    StringBuilder pulseSb = new StringBuilder();
    StringBuilder pressSb = new StringBuilder();
    int pre = 0;
    int cur = 0;
    private String TAG = "MainActivity";
    private TextView mMeasureResultTv;
    // 最终高压
    private TextView tvHighPress;
    //最终低压
    private TextView tvLowPress;
    //最终脉率
    private TextView tvPulseRate;
    // 传输过程中压力
    private TextView tvTranslatingPress;
    // 传输过程中脉率
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
        mMeasureResultTv = (TextView) findViewById(R.id.measure_result);
        // 最终高压
        tvHighPress = (TextView) findViewById(R.id.highPress);
        //最终低压
        tvLowPress = (TextView) findViewById(R.id.lowPress);
        //最终脉率
        tvPulseRate = (TextView) findViewById(R.id.pulse_rate);
        tvPulseRate = (TextView) findViewById(R.id.pulse_rate);
        // 传输过程中压力
        tvTranslatingPress = (TextView) findViewById(R.id.translating_press);

        // 传输过程中脉率
        tvTranslatingPulse = (TextView) findViewById(R.id.translating_pulse_rate);
        second_edit = (EditText) findViewById(R.id.second_edit);
        first_edit = (EditText) findViewById(R.id.first_edit);
        measure_count = (TextView) findViewById(R.id.measure_count);
    }

    @Override
    protected void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {


//                mMeasureResultTv.setText(v+"");
                Log.d(TAG, "==========buffer=====" + SystemsConvertUtils.bytesToHexString(buffer));
                int responseCmd = buffer[2] & 0xFF;
                //转化为16进制的String字符串,接受到的全部是16进制的
                String typeCmd = Integer.toHexString(responseCmd);
                //接收过程中的从站发送帧的命令码CMD = 82"82".equals(typeCmd)

                //血压计初始化完成，可以发送命令了 CMD = "87"
                if ("87".equals(typeCmd)) {
                    Log.d(TAG, "=========血压计初始化完成======");
                    EventBus.getDefault().post("turgoscope_init_success");
                }
                if ("82".equals(typeCmd)) {
                    int translatingPress = ((buffer[6] & 0xFF) << 8) + (buffer[7] & 0xFF);

                    tvTranslatingPress.setText("传输过程中的压力值" + translatingPress);

                    int translatingPulse = ((buffer[8] & 0xFF) << 8) + (buffer[9] & 0xFF);

                    tvTranslatingPulse.setText("传输过程中的脉率" + translatingPulse);
                    pre = cur;
                    cur = translatingPress;
                    Log.d(TAG, "pre ===========" + pre);
                    Log.d(TAG, "cur============" + cur);
                    if (cur < pre) {
                        pulseSb.append("" + translatingPulse).append(",");
                    }
                    pressSb.append("" + translatingPress).append(",");

                }

                //接收完毕发送正确结果命令码 CMD = 83
                if ("83".equals(typeCmd)) {
                    int last = ((buffer[6] & 0xFF) << 8) + (buffer[7] & 0xFF);
                    // 最终高压
                    tvHighPress.setText("高压" + last);
                    //最终低压
                    int low = buffer[8] & 0xFF;
                    tvLowPress.setText("低压" + low);
                    int lastPulseRate = buffer[9] & 0xFF;
                    //最终脉率
                    tvPulseRate.setText("脉率" + lastPulseRate);
                    Log.d(TAG, "===压力值======" + pressSb);
                    Log.d(TAG, "=====脉率变化=====" + pulseSb);

                    //接收完毕后发送响应帧
                    if (isSendCheckResult(buffer)) {
                        send(mOnCorrectResponseCmd);
                        Log.d(TAG, "============正确响应码发送成功了========");
                    }

                    repeatMeasure();

                }

                //发送错误测量结果 命令码CMD = 84
                //4 无法检测出高低压
                //5 加压错误
                //6 手臂或身体运动引起加压不当
                //7 袖带太松或松脱
                //8 压力超过最大测量值
                if ("84".equals(typeCmd)) {
                    int errorCode = (buffer[7] & 0xFF) + (buffer[8] & 0xFF);
                    String erCode = Integer.toHexString(errorCode);
                    if ("4".equals(erCode)) {
                        Toast.makeText(MainActivity.this, "无法检测出高低压", Toast.LENGTH_SHORT).show();
                    } else if ("5".equals(erCode)) {
                        Toast.makeText(MainActivity.this, "加压错误", Toast.LENGTH_SHORT).show();
                    } else if ("6".equals(erCode)) {
                        Toast.makeText(MainActivity.this, "手臂或身体运动引起加压不当", Toast.LENGTH_SHORT).show();
                    } else if ("7".equals(erCode)) {
                        Toast.makeText(MainActivity.this, "袖带太松或松脱", Toast.LENGTH_SHORT).show();
                    } else if ("8".equals(erCode)) {
                        Toast.makeText(MainActivity.this, "压力超过最大测量值", Toast.LENGTH_SHORT).show();
                    }

                    //接收完毕后发送响应帧
                    if (isSendCheckResult(buffer)) {
                        send(onErrorResponseCmd);
                        Log.d(TAG, "============错误响应码发送成功了========");
                    }
                    //接收到错误的测量之后间隔几秒进行开始重新测量
                    repeatMeasure();


                }

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

    /**
     * 循环测试
     */
    private void repeatMeasure() {
        if (!TextUtils.isEmpty(first_edit.getText().toString())) {
            Log.d(TAG, "===========进入错误了==============");
            Log.d(TAG, "=========现在已经是第==============" + (Integer.valueOf(first_edit.getText().toString()))*4);
            while (mCount <= (Integer.valueOf(first_edit.getText().toString()))*4) {
                send(stopMeasureCmd);
                Log.d(TAG, "===========停止测量命令发送了==============");
                sendD1SMsg(MainActivity.this, 0);
                Log.d(TAG, "===========断电了=====state=========" + getDeviceState(MainActivity.this));
                Log.d(TAG, "===========断电了之后此时的时间是==============" + System.currentTimeMillis());

                if (mCount < (Integer.valueOf(first_edit.getText().toString()))*4) {

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    sendD1SMsg(MainActivity.this, 1);
                    Log.d(TAG, "===========开始供电命令发送了========state======" + getDeviceState(MainActivity.this));
                    Log.d(TAG, "===========供电了此时的时间是=================" + System.currentTimeMillis());
                    Log.d(TAG, "=========现在已经是第==============" + mCount + "次了");
                    mCount++;
                }

                measure_count.setText("目前已经测量的次数:" + (mCount-1)/4);


                break;
            }
        } else {
            if(mCount == 4) {
                send(stopMeasureCmd);
                Log.d(TAG, "===========停止测量命令发送了==============");
                sendD1SMsg(MainActivity.this, 0);
            }
        }
    }

    // 控制电源 0 表示关闭供电 1表示开启供电
    public static int getDeviceState(Context context) {
        File file = new File("/sys/medical_drv/medical_states");
        FileReader mReader = null;
        int value = 0;
        BufferedReader br = null;
        String s = null;
        try {
            mReader = new FileReader(file);
            br = new BufferedReader(mReader);
            while ((s = br.readLine()) != null) {
                value = Integer.parseInt(s);
            }
            mReader.close();
            mReader = null;
        } catch (Exception e) {
            Toast.makeText(context, "血压计无法正常供电，请先结束测量再重新开始测量", Toast.LENGTH_SHORT).show();
        }
        return value;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 开始测量
     *
     * @param view
     */
    public void startMeasure(View view) {
        send(startMeasureCmd);
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
     * 停止测量并断电
     *
     * @param view
     */
    public void stopMeasure(View view) {
        send(stopMeasureCmd);
        sendD1SMsg(this, 0);
        mCount = 1;
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
//        if(!TextUtils.isEmpty(second_edit.getText().toString().trim())) {
//            try {
//                Thread.sleep((long) (Double.parseDouble(second_edit.getText().toString().trim())*1000));
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        send(startMeasureCmd);
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
            send(startMeasureCmd);
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
