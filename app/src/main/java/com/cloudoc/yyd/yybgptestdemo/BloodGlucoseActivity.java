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

                    Log.d(TAG, "==========buffer=====" + DigitalTransUtil.bytesToHexString(buffer));
                    Log.d(TAG, "==========size=======" + size);

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
     * 开始供电
     * @param view
     */
    public void glucoseStartPower(View view) {
        SystemUtil.glucoseSupplyPower(this,1);
    }

    /**
     * 开始断电
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




    public void exitThisActivity(View view) {
        finish();
    }
}
