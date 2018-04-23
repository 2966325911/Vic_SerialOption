package com.cloudoc.yyd.yybgptestdemo;

import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class BloodGlucoseActivity extends BaseSerialPortGlucoseActivity {
    private String TAG = BloodGlucoseActivity.class.getSimpleName();
    private static final double GLUCOSE_DENOMINATOR = 18.00;


    private Toast mToast;
    private TextView tvNormalValue,tvEngValue;
    private double mBloodGlucoseResult,mGlucoseEngValue;

    /**
     *  测量前状态确认  命令码
     */
    private static final String MEASURE_PRE_SATE_CONFIRM_CMD_CODE = "2";
    /**
     * 滴血状态
     */
    private static final String STAXIS_STATE_CMD_CODE = "3";
    /**
     * 测量过程
     */
    private static final String MEASURING_CMD_CODE = "4";
    /**
     * 测量结果
     */
    private static final String MEASURE_RESULT_CMD_CODE = "5";
    /**
     * 试纸状态帧
     *
     */
    private static final String TEST_STRIP_STATE_CMD_CODE = "6";

    /**
     * 机器 自检状态确认
     */
    private static final String MACHINE_SELF_CHECK_STATE_CONFIRM_CMD_CODE = "7";

    private static final String MEASURE_END_CMD_CODE = "9";

    private static final String DEVICE_ERROR = "机器异常，请停止测量后重新开始测量";
    private static final String TEST_STRIP_ERROR = "试纸异常,请用符合血糖仪规格的新的试纸重新插入";
    private static final String TEMPERATURE_ERROR = "温度异常,请停止测量后重新开始测量";
    private static final String BLOOD_INSUFFICIENT = "血量异常，进血不足,请给足够的血以便保证结果准确";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_glucose);
        initView();
    }

    private void initView() {
        tvNormalValue = (TextView) findViewById(R.id.tv_normal_value);
        tvEngValue = (TextView) findViewById(R.id.tv_eng_value);
    }


    @Override
    protected void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{
                    Log.d(TAG,"==========buffer=====" + DigitalTransUtil.bytesToHexString(buffer));
                    Log.d(TAG,"==========size======="  + size );
                    int responseCmd = buffer[1] & 0xFF;
                    //转化为16进制的String字符串,接受到的全部是16进制的
                    String cmd = Integer.toHexString(responseCmd);
                    if(MEASURE_PRE_SATE_CONFIRM_CMD_CODE.equals(cmd)) {
                        Log.d(TAG,"===========startTime==" + System.currentTimeMillis());
                        int preTestConfirmCodeState = buffer[3] & 0xFF;
                        String state = Integer.toHexString(preTestConfirmCodeState);
                        //正常
                        String stateNormal = "0";
                        //试纸异常
                        String testStripException = "1";
                        //温度异常
                        String temperatureException = "2";
                        //系统异常
                        String systemException = "3";
                        if(stateNormal.equals(state)){
                            Log.d(TAG,"==========测量前正常=======");
                        } else  if (testStripException.equals(state)) {
                            Log.d(TAG,TEST_STRIP_ERROR);
                            showToast(TEST_STRIP_ERROR);
                        } else if(temperatureException.equals(state)) {
                            Log.d(TAG,"==========温度异常=======");
                            showToast(TEMPERATURE_ERROR);
                        } else if(systemException.equals(state)) {
                            Log.d(TAG,DEVICE_ERROR);
                            showToast(DEVICE_ERROR);
                        }
                        Log.d(TAG,"===========startTime==" + System.currentTimeMillis());
                        byte[] responseCmdException = {(byte)0xA5,0x02,0x05,0x01,0x08};
                        sendCmd(responseCmdException);
                    } else if(STAXIS_STATE_CMD_CODE.equals(cmd)) {
                        int staxisCodeState = buffer[3] & 0xFF;
                        String staxisState = Integer.toHexString(staxisCodeState);
                        String bloodNormal = "0";
                        String bloodInException = "1";
                        if(bloodInException.equals(staxisState)) {
                            showToast(BLOOD_INSUFFICIENT);
                        } else if(bloodNormal.equals(staxisState)) {
                            Log.d(TAG,"===血液正常=============");
                        }
                        //滴血状态
                        byte[] responseCmdStaxis = {(byte)0xA5,0x03,0x05,0x01,0x09};
                        sendCmd(responseCmdStaxis);
                    } else if(MEASURING_CMD_CODE.equals(cmd)) {

                        byte[] responseCmdMeasuring = {(byte)0xA5,0x04,0x05,0x01,(byte)0x0A};
                        sendCmd(responseCmdMeasuring);
                    } else if(MEASURE_RESULT_CMD_CODE.equals(cmd)){
                        String result;
                        //测量结果
                        String result1 = DigitalTransUtil.convertHexToString(Integer.toHexString(Integer.parseInt(String.valueOf(buffer[3]))));
                        String result2 = DigitalTransUtil.convertHexToString(Integer.toHexString(Integer.parseInt(String.valueOf(buffer[4]))));
                        String result3 = DigitalTransUtil.convertHexToString(Integer.toHexString(Integer.parseInt(String.valueOf(buffer[5]))));
                        Log.d(TAG,"==result1==" + result1 + "====result2====" + result2 + "result3====" + result3 );
                        result = result1+result2+result3;
                        mBloodGlucoseResult = DigitalTransUtil.keepDigitDecimals(Integer.parseInt(result)/GLUCOSE_DENOMINATOR,1);
                        byte[] responseCmdResult = {(byte)0xA5,0x05,0x05,0x01,(byte)0x0B};
                        sendCmd(responseCmdResult);
                        tvNormalValue.setText("正常血糖的值是=======" + mBloodGlucoseResult);
                    }else if(TEST_STRIP_STATE_CMD_CODE.equals(cmd)) {
                        int testStripStateCode = buffer[3] & 0xFF;
                        String state = Integer.toHexString(testStripStateCode);
                        //试纸状态帧
                        // 0x00 试纸被拔出 0x01试纸被插入
                        String pageIn="1";
                        String pageOut = "0";
                        byte[] responseCmdState = {(byte)0xA5,0x06,0x05,0x01,(byte)0x0C};
                        sendCmd(responseCmdState);
                        if(pageIn.equals(state)) {
                            Log.d(TAG,"========试纸插入=====");
                        } else if(pageOut.equals(state)){
                            Log.d(TAG,"========试纸拔出=====");
                        }
                    } else if(MACHINE_SELF_CHECK_STATE_CONFIRM_CMD_CODE.equals(cmd)){
                        byte[] responseCmdConfirm = {(byte)0xA5,0x07,0x05,0x01,(byte)0x0D};
                        Log.d(TAG,"====机器自检状态完成=====");
                        sendCmd(responseCmdConfirm);
                    } else if(MEASURE_END_CMD_CODE.equals(cmd)) {
                        Log.d(TAG,"=============工程模式测试==================");
                        //测量结果
                        double result1 = (Double.parseDouble(String.valueOf(buffer[3]))) * 1000;
                        double result2 = (Double.parseDouble(String.valueOf(buffer[4]))) * 100;
                        double result3 = (Double.parseDouble(String.valueOf(buffer[5]))) * 10;
                        double result4 = (Double.parseDouble(String.valueOf(buffer[6])));
                        mGlucoseEngValue = result1 + result2 + result3 + result4;

                        tvEngValue.setText("工程模式模式下的值(AD值)是========" + mGlucoseEngValue);
                    }
                }catch (Exception e) {
                    showToast(DEVICE_ERROR);
                    e.printStackTrace();
                }
            }
        });
    }



    /**
     * Toast 提示
     * @param msg
     */
    public void showToast(String msg){
        try {
            if(mToast == null) {
                mToast = Toast.makeText(this,msg , Toast.LENGTH_SHORT);
            } else {
                mToast.setText(msg);
                mToast.setDuration(Toast.LENGTH_SHORT);
            }
            mToast.show();
        }catch (Exception e) {
            Toast.makeText(this,msg , Toast.LENGTH_SHORT).show();
        }

    }

    public void cancelToast(){
        try{
            if(mToast != null) {
                mToast.cancel();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 血糖仪开始供电
     * @param view
     */
    public void glucoseStartPower(View view) {
        SystemUtil.glucoseSupplyPower(this,1);
    }

    /**
     * 血糖仪开始断电
     * @param view
     */
    public void glucoseEndPower(View view) {
        SystemUtil.glucoseSupplyPower(this,0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SystemUtil.glucoseSupplyPower(this,0);
        cancelToast();
    }

    /**
     * 发送工程模式命令
     * @param view
     */
    public void sendEngCmd(View view) {
        byte[] engCmd = {(byte)0xC5,0x02,0x05,0x01,0x08};
        sendCmd(engCmd);
    }

    /**
     * 校准命令码
     * @param view
     */
    public void sendCheckCmd(View view) {
        byte[] engCmd = {(byte)0xB5,0x01,0x05,0x01,(byte)0x0C};
        sendCmd(engCmd);
    }

    public void exitThisActivity(View view) {
        finish();
    }
}
