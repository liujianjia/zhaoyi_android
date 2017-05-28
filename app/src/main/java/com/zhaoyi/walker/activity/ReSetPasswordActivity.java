package com.zhaoyi.walker.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.ZyApplication;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jianjia Liu on 2017/3/24.
 */

public class ReSetPasswordActivity extends FragmentActivity {
    private EditText etPasswd;
    private EditText etConfirmPasswd;
    private Button btnReSet;
    private ImageView ivReSetBack;
    private String phone;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_passwd);
        etPasswd = (EditText) findViewById(R.id.et_userpasswd);
        etConfirmPasswd = (EditText) findViewById(R.id.et_userconfirmpasswd);
        btnReSet = (Button) findViewById(R.id.btn_reset);
        ivReSetBack = (ImageView) findViewById(R.id.iv_reset_back);

        TextChange textChange = new TextChange();
        etPasswd.addTextChangedListener(textChange);
        etConfirmPasswd.addTextChangedListener(textChange);
        btnReSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String passwd = etPasswd.getText().toString().trim();
                String confirmPasswd = etConfirmPasswd.getText().toString().trim();
                if(!passwd.equals(confirmPasswd)) {
                    Toast.makeText(ReSetPasswordActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }
                reSetPasswd(passwd);
            }
        });
        ivReSetBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Bundle args = getIntent().getExtras();
        phone = args.getString("phone");
    }

    private void reSetPasswd(final String passwd) {
        List<Param> params = new ArrayList<Param>();

        params.add(new Param("pwd", passwd));
        params.add(new Param("phone", phone));

        final ProgressDialog pd = new ProgressDialog(ReSetPasswordActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage(getString(R.string.reseting));
        pd.show();

        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.FINDPASSWROD,
                new OkHttpManager.HttpCallBack() {
            @Override
            public void onResponse(JSONObject json) {
                int code = json.getInteger("code");
                if (code == 1000) {
                    Toast.makeText(ReSetPasswordActivity.this,
                            "重置成功！", Toast.LENGTH_SHORT)
                            .show();

                    JSONObject userJson = ZyApplication.getInstance().getUserJson();
                    userJson.put(MyContants.JSON_KEY_PASSWORD, passwd);
                    ZyApplication.getInstance().setUserJson(userJson);

                    Log.i(ZyApplication.getInstance()
                            .getUserJson()
                            .getString(MyContants.JSON_KEY_NAME) +
                            "(" +
                            ZyApplication.getInstance()
                            .getUserJson()
                            .getString(MyContants.JSON_KEY_ZYID)
                            + "): ", "password has changed.");

                    startActivity(new Intent(ReSetPasswordActivity.this, LoginActivity.class));
                    finish();
                }
                else if (code == 2000){
                    pd.dismiss();
                    Toast.makeText(ReSetPasswordActivity.this,
                            "该手机未被注册", Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(String errorMsg) {

            }
        });
    }

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
            boolean Sign1 = etPasswd.getText().length() > 0;
            boolean Sign2 = etConfirmPasswd.getText().length() > 0;

            if (Sign1 && Sign2) {
                btnReSet.setEnabled(true);
                btnReSet.setBackgroundColor(getResources().getColor(R.color.theme_default_color));
            }
            // 在layout文件中，对Button的text属性应预先设置默认值，否则刚打开程序的时候Button是无显示的
            else {
                btnReSet.setEnabled(false);
                btnReSet.setBackgroundColor(getResources().getColor(R.color.abs__bright_foreground_disabled_holo_light));
            }
        }

    }
}
