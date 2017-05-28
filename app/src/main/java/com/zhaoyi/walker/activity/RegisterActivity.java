package com.zhaoyi.walker.activity;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
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
import com.zhaoyi.walker.utils.LocalUserUtil;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by jianjia Liu on 2017/3/23.
 */

public class RegisterActivity extends FragmentActivity {
    EditText etUsername;
    EditText etUserRealname;
    EditText etUserIdCard;
    EditText etPasswd;
    EditText etConfirmPasswd;
    Button btnRegister;
    ImageView ivBack;
    private Resources resource;
    private ColorStateList cs0;
    private ColorStateList cs1;
    private String phone;
    private boolean isUserNameValid = false;
    private boolean isIdCardValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        etUsername = (EditText)findViewById(R.id.et_username);
        etUserRealname = (EditText)findViewById(R.id.et_userrealname);
        etUserIdCard = (EditText)findViewById(R.id.et_useridcard);
        etPasswd = (EditText) findViewById(R.id.et_userpasswd);
        etConfirmPasswd = (EditText)findViewById(R.id.et_userconfirmpasswd);
        btnRegister = (Button)findViewById(R.id.btn_register);
        ivBack = (ImageView)findViewById(R.id.iv_top_back);

        // if user changed, clear the password
        TextChange textChange = new TextChange();
        etUsername.addTextChangedListener(textChange);
        etUserRealname.addTextChangedListener(textChange);
        etUserIdCard.addTextChangedListener(textChange);
        etPasswd.addTextChangedListener(textChange);
        etConfirmPasswd.addTextChangedListener(textChange);

        Bundle args = getIntent().getExtras();
        phone = args.getString("phone");

        resource = getBaseContext().getResources();
        cs0 = resource.getColorStateList(R.color.abs__bright_foreground_disabled_holo_light) ;
        cs1 = resource.getColorStateList(R.color.theme_default_color);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isIdCardValid || !isUserNameValid) {
                    Toast.makeText(RegisterActivity.this, "用户名或身份证不可用", Toast.LENGTH_SHORT).show();
                    return;
                }
                String userName = etUsername.getText().toString().trim();
                String userRealName = etUserRealname.getText().toString().trim();
                String userIdCard = etUserIdCard.getText().toString().trim();
                String passwd = etPasswd.getText().toString();
                String confirmPasswd = etConfirmPasswd.getText().toString();

                if(!passwd.equals(confirmPasswd)) {
                    Toast.makeText(RegisterActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }

                String userNameRegEx = "_?[0-9]{0,15}([a-z]{1,16})";
                String userIdCardRegEx = "[0-9]{6}((1[8|9][0-9][0-9])|20(0[0-9]|1[0-7]))" +
                        "(0[1-9]|1[0-2])(0[1-9]|((1|2)[0-9])|3(0|1))[0-9]{3}([0-9]|x)";

                if(userName.length() < 6 || !Pattern.compile(userNameRegEx).matcher(userName).matches()) {
                    Toast.makeText(RegisterActivity.this, "用户名格式不正确", Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    if(!Pattern.compile(userIdCardRegEx).matcher(userIdCard).matches()) {
                        Toast.makeText(RegisterActivity.this, "身份证格式不正确", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else {
                        register(userName, userRealName, userIdCard, passwd);
                    }
                }
            }
        });
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        etUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    etUsername.setBackgroundResource(R.drawable.bg_edit_selected);
                } else {
                    etUsername.setBackgroundResource(R.drawable.bg_edit_unselected);
                    checkUserNameIsValid(etUsername.getText().toString().trim());
                }
            }
        });
        etUserRealname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    etUserRealname.setBackgroundResource(R.drawable.bg_edit_selected);
                } else {
                    etUserRealname.setBackgroundResource(R.drawable.bg_edit_unselected);
                }
            }
        });
        etUserIdCard.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    etUserIdCard.setBackgroundResource(R.drawable.bg_edit_selected);
                } else {
                    etUserIdCard.setBackgroundResource(R.drawable.bg_edit_unselected);
                    checkIdCardIsValid(etUserIdCard.getText().toString().trim());
                }
            }
        });
        etPasswd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    etPasswd.setBackgroundResource(R.drawable.bg_edit_selected);
                } else {
                    etPasswd.setBackgroundResource(R.drawable.bg_edit_unselected);
                }
            }
        });
        etConfirmPasswd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    etConfirmPasswd.setBackgroundResource(R.drawable.bg_edit_selected);
                } else {
                    etConfirmPasswd.setBackgroundResource(R.drawable.bg_edit_unselected);
                }
            }
        });
    }

    private void checkUserNameIsValid(String name) {
        if(TextUtils.isEmpty(name)) {
            return;
        }
        List<Param> params = new ArrayList<>();
        params.add(new Param("userName", name));
        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.CHECKUSERNAMEISVALID,
                new OkHttpManager.HttpCallBack() {
                    @Override
                    public void onResponse(JSONObject json) {
                        int code = json.getInteger("code");
                        if (code == 1000) {
                            isUserNameValid = true;
                        } else if(code == 2001) {
                            isUserNameValid = false;
                            Toast.makeText(RegisterActivity.this, "抱歉，该用户名已被注册", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {

                    }
                });
    }

    private void checkIdCardIsValid(String idCard) {
        if(TextUtils.isEmpty(idCard)) {
            return;
        }
        List<Param> params = new ArrayList<>();
        params.add(new Param("credit", idCard));
        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.CHECKIDCARDISVALID,
                new OkHttpManager.HttpCallBack() {
                    @Override
                    public void onResponse(JSONObject json) {
                        int code = json.getInteger("code");
                        if (code == 1000) {
                            isIdCardValid = true;
                        } else if(code == 2001) {
                            isIdCardValid = false;
                            Toast.makeText(RegisterActivity.this, "抱歉，该身份证已被注册", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {

                    }
                });
    }
    private void register(final String userName, final String userRealName, final String userIdCard, final String passwd) {
        List<Param> params = new ArrayList<Param>();
        params.add(new Param("userName", userName));
        params.add(new Param("trueName", userRealName));
        params.add(new Param("credit", userIdCard));
        params.add(new Param("password", passwd));
        params.add(new Param("phone", phone));

        final ProgressDialog pd = new ProgressDialog(RegisterActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage(getString(R.string.registerring));
        pd.show();
        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.REGISTER, new OkHttpManager.HttpCallBack() {
            @Override
            public void onResponse(JSONObject json) {
                int code = json.getInteger("code");
                if (code == 1000) {
                    Toast.makeText(RegisterActivity.this,
                            "注册成功！", Toast.LENGTH_SHORT)
                            .show();
                    pd.dismiss();
                    JSONObject userJson = new JSONObject();
                    LocalUserUtil.getInstance().removeCurrentUserInfo();  //删除当前用户缓存
                    userJson.put(MyContants.JSON_KEY_NAME, userName);
                    userJson.put(MyContants.JSON_KEY_PASSWORD, passwd);
                    userJson.put(MyContants.JSON_KEY_REAL_NAME, userRealName);
                    userJson.put(MyContants.JSON_KEY_IDCARD, userIdCard);
                    userJson.put(MyContants.JSON_KEY_PHONE, phone);
                    ZyApplication.getInstance().setUserJson(userJson);

                    Log.i(userName, ": register success.");

                    finish();
                }
                else {
                    pd.dismiss();
                    Toast.makeText(RegisterActivity.this,
                            "服务器繁忙请重试...", Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                pd.dismiss();
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

            boolean Sign1 = etUsername.getText().length() > 0;
            boolean Sign2 = etUserRealname.getText().length() > 0;
            boolean Sign3 = etUserIdCard.getText().length() > 0;
            boolean Sign4 = etPasswd.getText().length() > 0;
            boolean Sign5 = etConfirmPasswd.getText().length() > 0;

            if (Sign1 && Sign2 && Sign3 && Sign4 && Sign5) {
                btnRegister.setEnabled(true);
                btnRegister.setBackgroundColor(cs1.getDefaultColor());
            }
            // 在layout文件中，对Button的text属性应预先设置默认值，否则刚打开程序的时候Button是无显示的
            else {
                btnRegister.setEnabled(false);
                btnRegister.setBackgroundColor(cs0.getDefaultColor());
            }
        }

    }
}
