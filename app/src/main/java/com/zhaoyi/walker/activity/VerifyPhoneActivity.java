package com.zhaoyi.walker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

import static cn.smssdk.SMSSDK.getVerificationCode;
import static cn.smssdk.SMSSDK.submitVerificationCode;

/**
 * Created by jianjia Liu on 2017/3/23.
 */

public class VerifyPhoneActivity extends FragmentActivity {
    EditText etPhone;
    Button btnGetAuth;
    Button btnNext;
    TextView tvRegFirst;
    TextView tvRegSecond;
    TextView tvRegThird;
    TextView tvRegTitle;
    ImageView ivRegBack;
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
        setContentView(R.layout.activity_verify_phone);
        etPhone = (EditText) findViewById(R.id.et_userphone);
        btnGetAuth = (Button) findViewById(R.id.btn_getauth);
        btnNext = (Button) findViewById(R.id.btn_next);
        tvRegFirst = (TextView) findViewById(R.id.tv_register_first);
        tvRegSecond = (TextView) findViewById(R.id.tv_register_second);
        tvRegThird = (TextView) findViewById(R.id.tv_register_third);
        ivRegBack = (ImageView) findViewById(R.id.iv_reg_back);
        tvRegTitle = (TextView) findViewById(R.id.tv_reg_title);

        Bundle args = getIntent().getExtras();
        type = args.getInt("type");
        switch(type) {
            case 0:

                break;
            case 1:
                tvRegThird.setText(getString(R.string.reset_passwd));
                tvRegTitle.setText(getString(R.string.reset_pwd_title));
                isPhoneValid = true;
                break;
            case 2:
                tvRegThird.setText(getString(R.string.zy_new_phone));
                tvRegTitle.setText(getString(R.string.zy_change_phone));
                break;
            default:
                break;
        }

        // 为文本框添加事件监听器
        //etPhone.addTextChangedListener(new TextChange());

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
                        VerifyPhoneActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                submitCodeSuccess();
                            }
                        });
                    }else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                        //获取验证码成功
                        VerifyPhoneActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(VerifyPhoneActivity.this, "成功发送验证码", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){
                        //返回支持发送验证码的国家列表
                    }
                } else if(result == SMSSDK.RESULT_ERROR) {
                    VerifyPhoneActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(VerifyPhoneActivity.this, "验证码有误", Toast.LENGTH_SHORT).show();
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
                phone = etPhone.getText().toString().trim();

                String phoneRegEx = "1[0-9]{10}";
                if(!Pattern.compile(phoneRegEx).matcher(phone).matches()) {
                    Toast.makeText(VerifyPhoneActivity.this, "手机号码格式不正确", Toast.LENGTH_SHORT).show();
                    return;
                }
                //getCodeSuccess();
                //请求验证码
                getVerificationCode(country, phone);
                VerifyPhoneActivity.this.runOnUiThread(new Runnable() {
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
                if(!isPhoneValid) {
                    Toast.makeText(VerifyPhoneActivity.this, "该手机已被注册，请更换手机后重试",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                String code = etPhone.getText().toString();
                if(!code.equals("")) {
                    //提交验证码
                    submitVerificationCode(country, phone, code);
                }
                else {
                    Toast.makeText(VerifyPhoneActivity.this, getString(R.string.enter_hint_code),
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

        etPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    etPhone.setBackgroundResource(R.drawable.bg_edit_selected);
                } else {
                    etPhone.setBackgroundResource(R.drawable.bg_edit_unselected);
                    if(type != 1) {
                        checkPhoneIsValid(etPhone.getText().toString().trim());
                    }
                }
            }
        });
        TextChange textChange = new TextChange();
        etPhone.addTextChangedListener(textChange);
        etPhone.setInputType(InputType.TYPE_CLASS_PHONE);
    }

    private void checkPhoneIsValid(String phone) {
        if(TextUtils.isEmpty(phone)) {
            return;
        }
        List<Param> params = new ArrayList<>();
        params.add(new Param("phone", phone));
        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.CHECKPHONEISVALID,
                new OkHttpManager.HttpCallBack() {
                    @Override
                    public void onResponse(JSONObject json) {
                        int code = json.getInteger("code");
                        if (code == 1000) {
                            isPhoneValid = true;
                        } else if(code == 2001) {
                            isPhoneValid = false;
                            Toast.makeText(VerifyPhoneActivity.this, "抱歉，该手机已被注册", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {

                    }
                });
    }

    private void getCodeSuccess() {
        btnGetAuth.setClickable(false);
        btnGetAuth.setBackgroundColor(getResources().getColor(R.color.abs__bright_foreground_disabled_holo_light));

        handler.postDelayed(runnable, TIMEOUT); // 1s timeout

        tvRegFirst.setTextColor(getResources().getColor(R.color.black1));
        tvRegSecond.setTextColor(getResources().getColor(R.color.action_bar_disable));
        etPhone.setText("");
        etPhone.setHint(getString(R.string.enter_hint_code));
        btnNext.setClickable(true);
        btnNext.setBackgroundColor(getResources().getColor(R.color.action_bar_disable));
    }

    private void submitCodeSuccess() {
        Intent intent;
        switch (type) {
            case 0:
                intent = new Intent(VerifyPhoneActivity.this, RegisterActivity.class);
                intent.putExtra("phone", phone);
                startActivity(intent);
                break;
            case 1:
                intent = new Intent(VerifyPhoneActivity.this, ReSetPasswordActivity.class);
                intent.putExtra("phone", phone);
                startActivity(intent);
                break;
            case 2:
                intent = new Intent(VerifyPhoneActivity.this, NewPhoneActivity.class);
                startActivityForResult(intent, TYPE_USER_UPDATE_PHONE);
                break;
            default:
                break;
        }

        VerifyPhoneActivity.this.finish();
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
                    etPhone.setHint(getString(R.string.enter_phone_number));
                    //ColorStateList cs0 = (ColorStateList) resource.getColorStateList(R.color.green) ;
                    btnGetAuth.setBackgroundColor(getResources().getColor(R.color.action_bar_disable));
                    tvRegFirst.setTextColor(getResources().getColor(R.color.action_bar_disable));
                    tvRegSecond.setTextColor(getResources().getColor(R.color.abs__bright_foreground_disabled_holo_light));

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

            boolean Sign1 = etPhone.getText().length() >= 11;

            //System.out.println("btnNext: " + btnNext.isEnabled());
            if (Sign1) {
                //btnGetAuth.setEnabled(true);
                //btnGetAuth.setBackgroundColor(cs2.getDefaultColor());
                etPhone.clearFocus();
            }
            // 在layout文件中，对Button的text属性应预先设置默认值，否则刚打开程序的时候Button是无显示的
            else {
                //btnGetAuth.setEnabled(false);
                //btnGetAuth.setBackgroundColor(cs0.getDefaultColor());
                etPhone.requestFocus();
            }
        }

    }

    protected void onDestroy() {

        // 销毁回调监听接口
        SMSSDK.unregisterAllEventHandler();
        super.onDestroy();
    }
}
