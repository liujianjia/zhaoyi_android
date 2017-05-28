package com.zhaoyi.walker.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by jianjia Liu on 2017/3/24.
 */

public class NewPhoneActivity extends FragmentActivity {
    EditText etNewPone;
    Button btnChange;
    ImageView ivBack;
    private boolean isPhoneValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_phone);
        etNewPone = (EditText) findViewById(R.id.et_new_phone);
        btnChange = (Button) findViewById(R.id.btn_change);
        ivBack = (ImageView) findViewById(R.id.iv_reset_back);

        TextChange textChange = new TextChange();
        etNewPone.addTextChangedListener(textChange);

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isPhoneValid) {
                    Toast.makeText(NewPhoneActivity.this, "抱歉，该手机已被注册", Toast.LENGTH_SHORT).show();
                    return;
                }
                String phone = etNewPone.getText().toString().trim();
                String phoneRegEx = "1[0-9]{10}";
                if(!Pattern.compile(phoneRegEx).matcher(phone).matches()) {
                    Toast.makeText(NewPhoneActivity.this, "手机号码格式不正确", Toast.LENGTH_SHORT).show();
                    return;
                }
                updatePhone(phone);
            }
        });
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        etNewPone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    etNewPone.setBackgroundResource(R.drawable.bg_edit_selected);
                } else {
                    etNewPone.setBackgroundResource(R.drawable.bg_edit_unselected);
                    checkPhoneIsValid(etNewPone.getText().toString().trim());
                }
            }
        });

        etNewPone.setInputType(InputType.TYPE_CLASS_PHONE);
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
                            Toast.makeText(NewPhoneActivity.this, "抱歉，该手机已被注册", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {

                    }
                });
    }

    private void updatePhone(final String phone) {
        List<Param> params = new ArrayList<Param>();
        params.add(new Param("key", MyContants.JSON_KEY_PHONE));
        params.add(new Param("value", phone));
        params.add(new Param("userId", ZyApplication.getInstance().getUserJson().getString(MyContants.JSON_KEY_ZYID)));
        List<File> files = new ArrayList<File>();
        final ProgressDialog pd = new ProgressDialog(NewPhoneActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage(getString(R.string.zy_changing));
        pd.show();
        OkHttpManager.getInstance().post(params, files, MyContants.BASE_URL + MyContants.UPDATEPROFILE, new OkHttpManager.HttpCallBack() {
            @Override
            public void onResponse(JSONObject json) {
                int code = json.getInteger("code");
                if (code == 1000) {
                    Toast.makeText(NewPhoneActivity.this,
                            "更改成功！", Toast.LENGTH_SHORT)
                            .show();
                    JSONObject userJson = ZyApplication.getInstance()
                            .getUserJson();
                    userJson.put(MyContants.JSON_KEY_PHONE, phone);
                    ZyApplication.getInstance().setUserJson(userJson);

                    Intent intent = new Intent();
                    intent.putExtra("newPhone", phone);
                    setResult(RESULT_OK, intent);
                    pd.dismiss();
                    finish();
                }
                else {
                    pd.dismiss();
                    Toast.makeText(NewPhoneActivity.this,
                            "服务器繁忙请重试...", Toast.LENGTH_SHORT)
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
            boolean Sign1 = etNewPone.getText().length() > 0;
            boolean Sign2 = etNewPone.getText().length() >= 11;

            if (Sign1) {
                btnChange.setEnabled(true);
                btnChange.setBackgroundColor(getResources().getColor(R.color.theme_default_color));
            }
            else {
                btnChange.setEnabled(false);
                btnChange.setBackgroundColor(getResources().getColor(R.color.abs__bright_foreground_disabled_holo_light));
            }

            if(Sign2) {
                etNewPone.clearFocus();
            }
        }

    }
}
