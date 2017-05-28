package com.zhaoyi.walker.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaoyi.walker.R;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

import static cn.smssdk.SMSSDK.getVerificationCode;
import static cn.smssdk.SMSSDK.submitVerificationCode;

/**
 * Created by jianjia Liu on 2017/3/23.
 */

public class VerifyOldPhoneActivity extends FragmentActivity {
    private Context context;
    EditText etCode;
    Button btnGetAuth;
    Button btnNext;
    ImageView ivRegBack;
    private TextView tvOldPhone;
    private boolean isPhoneValid = false;
    private static String APPKEY = "1c6076b221916";  // mob 短信验证key
    private static String APPSECRET = "7a1bf8951ce12bf1666aed1a587e73a1";  // mob 短信验证secret
    private final int TIMEOUT  = 1000;
    private int times = 60;
    private String  phone;
    private String country = "86";

    private int type; // 验证类型
    private static final int TYPE_USER_FORGOT_PASSWORD = 103;
    private static final int  TYPE_USER_UPDATE_PHONE = 104;
    Handler handler = new Handler();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_old_phone);
        
        context = VerifyOldPhoneActivity.this;
        etCode = (EditText) findViewById(R.id.et_userphone);
        btnGetAuth = (Button) findViewById(R.id.btn_getauth);
        btnNext = (Button) findViewById(R.id.btn_next);
        ivRegBack = (ImageView) findViewById(R.id.iv_reg_back);
        tvOldPhone = (TextView) findViewById(R.id.tv_old_phone);

        Bundle args = getIntent().getExtras();
        phone = args.getString("phone");
        tvOldPhone.setText(phone);

        btnGetAuth.setClickable(true);

        //短信验证码SDK
        SMSSDK.initSDK(this,APPKEY,APPSECRET);
        EventHandler eh=new EventHandler(){

            @Override
            public void afterEvent(int event, int result, Object data) {

                if (result == SMSSDK.RESULT_COMPLETE) {
                    //回调完成
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        //提交验证码成功
                        VerifyOldPhoneActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                submitCodeSuccess();
                            }
                        });
                    }else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                        //获取验证码成功
                        VerifyOldPhoneActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(VerifyOldPhoneActivity.this, "成功发送验证码", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){
                        //返回支持发送验证码的国家列表
                    }
                } else if(result == SMSSDK.RESULT_ERROR) {
                    VerifyOldPhoneActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(VerifyOldPhoneActivity.this, "验证码有误", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    ((Throwable)data).printStackTrace();
                }
            }
        };
        SMSSDK.registerEventHandler(eh); //注册短信回调

        btnGetAuth.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(phone)) {
                    Log.e("Error: ", "phone can't be empty.");
                    return;
                }
                
                //getCodeSuccess();
                //请求验证码
                getVerificationCode(country, phone);
                VerifyOldPhoneActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getCodeSuccess();
                    }
                });
            }

        });
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String code = etCode.getText().toString();
                if(!code.equals("")) {
                    //提交验证码
                    submitVerificationCode(country, phone, code);
                }
                else {
                    Toast.makeText(VerifyOldPhoneActivity.this, getString(R.string.enter_hint_code),
                            Toast.LENGTH_SHORT).show();
                }
            }

        });
        ivRegBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        
        TextChange textChange = new TextChange();
        etCode.addTextChangedListener(textChange);
    }

    private void getCodeSuccess() {
        btnGetAuth.setClickable(false);
        btnGetAuth.setBackgroundColor(getResources().getColor(R.color.abs__bright_foreground_disabled_holo_light));

        handler.postDelayed(runnable, TIMEOUT); // 1s timeout

        btnNext.setClickable(true);
        btnNext.setBackgroundColor(getResources().getColor(R.color.action_bar_disable));
    }

    private void submitCodeSuccess() {
        Intent intent = new Intent(VerifyOldPhoneActivity.this, NewPhoneActivity.class);
        startActivityForResult(intent, TYPE_USER_UPDATE_PHONE);
        finish();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TYPE_USER_UPDATE_PHONE) {
            if(resultCode == RESULT_OK) {
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // handler自带方法实现定时器
            try {
                times--;
                if(times == 0) {
                    btnGetAuth.setClickable(true);
                    times = 60;
                    handler.removeCallbacks(runnable);
                    btnGetAuth.setText("获取验证码");
                    etCode.setHint(getString(R.string.enter_phone_number));
                    //ColorStateList cs0 = (ColorStateList) resource.getColorStateList(R.color.green) ;
                    btnGetAuth.setBackgroundColor(getResources().getColor(R.color.action_bar_disable));

                    return;
                }
                btnGetAuth.setText(Long.toString(times, 10) + "s后重新发送");
                handler.postDelayed(this, TIMEOUT);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    // EditText监听器
    class TextChange implements TextWatcher {

        @Override
        public void afterTextChanged(Editable arg0) {

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {

        }

        @Override
        public void onTextChanged(CharSequence cs, int start, int before,
                                  int count) {

            boolean Sign1 = etCode.getText().length() >= 11;

            //System.out.println("btnNext: " + btnNext.isEnabled());
            if (Sign1) {
                //btnGetAuth.setEnabled(true);
                //btnGetAuth.setBackgroundColor(cs2.getDefaultColor());
                etCode.clearFocus();
            }
            // 在layout文件中，对Button的text属性应预先设置默认值，否则刚打开程序的时候Button是无显示的
            else {
                //btnGetAuth.setEnabled(false);
                //btnGetAuth.setBackgroundColor(cs0.getDefaultColor());
                etCode.requestFocus();
            }
        }

    }

    protected void onDestroy() {

        // 销毁回调监听接口
        SMSSDK.unregisterAllEventHandler();
        super.onDestroy();
    }
}
