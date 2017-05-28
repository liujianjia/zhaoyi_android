package com.zhaoyi.walker.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
 * Created by jianjia Liu on 2017/5/7.
 */

public class RecoverPayPasswordActivity extends FragmentActivity {
    private Context context;
    private EditText etOldPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private Button btnReset;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);

        context = RecoverPayPasswordActivity.this;
        etOldPassword = (EditText) findViewById(R.id.et_old_password);
        etNewPassword = (EditText) findViewById(R.id.et_new_password);
        etConfirmPassword = (EditText) findViewById(R.id.et_confirm_password);
        btnReset = (Button) findViewById(R.id.btn_reset);

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextChange textChange = new TextChange();
        etOldPassword.addTextChangedListener(textChange);
        etNewPassword.addTextChangedListener(textChange);
        etConfirmPassword.addTextChangedListener(textChange);

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = etOldPassword.getText().toString().trim();
                String newPassword = etNewPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();
                if(!confirmPassword.equals(newPassword)) {
                    Toast.makeText(context, "新密码不一致， 请重新输入", Toast.LENGTH_SHORT).show();
                    return;
                }
                verifyAndUpdateOnSever(oldPassword, newPassword);
            }
        });

        //((TextView) findViewById(R.id.tv_title)).setText("修改支付密码");

        userId = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.JSON_KEY_ZYID);
    }

    private void verifyAndUpdateOnSever(String oldPassword, String newPassword) {
        if (TextUtils.isEmpty(userId)) {
            Log.e("Error: ", "user id can't be empty.");
            return;
        }

        final ProgressDialog pd = new ProgressDialog(context);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("正在验证");
        pd.show();
        List<Param> params = new ArrayList<Param>();
        params.add(new Param("oldPwd", oldPassword));
        params.add(new Param("pwd", newPassword));
        params.add(new Param("userId", userId));
        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.CHANGEPAYPASSWORD,
                new OkHttpManager.HttpCallBack() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                int code = jsonObject.getInteger("code");
                if (code == 1000) {
                    Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                    finish();
                } else if (code == 2000) {
                    Toast.makeText(context,
                            "旧密码错误", Toast.LENGTH_SHORT)
                            .show();
                    pd.dismiss();
                }else {
                    pd.dismiss();
                }

            }
            @Override
            public void onFailure(String errorMsg) {
                pd.dismiss();
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
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

            boolean Sign1 = etOldPassword.getText().length() > 0;
            boolean Sign2 = etNewPassword.getText().length() > 0;
            boolean Sign3 = etConfirmPassword.getText().length() > 0;

            if (Sign1 && Sign2 && Sign3) {
                btnReset.setEnabled(true);
                btnReset.setBackgroundColor(getResources().getColor(R.color.theme_default_color));
            }
            // 在layout文件中，对Button的text属性应预先设置默认值，否则刚打开程序的时候Button是无显示的
            else {
                btnReset.setEnabled(false);
                btnReset.setBackgroundColor(getResources().getColor(R.color.abs__bright_foreground_disabled_holo_light));
            }
        }

    }
}
