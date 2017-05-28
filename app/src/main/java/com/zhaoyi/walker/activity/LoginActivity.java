package com.zhaoyi.walker.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.ZyApplication;
import com.zhaoyi.walker.utils.LocalUserUtil;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jianjia Liu on 2017/3/23.
 */

public class LoginActivity extends FragmentActivity {
    private EditText et_username;
    private EditText et_password;
    private Button btn_login;
    private Button btn_register;
    private TextView tv_forgot_passwd;
    private ImageView iv_login_back;

    private static final int TYPE_USER_REGISTER = 101;// 注册
    private static final int TYPE_USER_FORGOT_PASSWORD = 102;// 忘记密码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_userpwd);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_register = (Button) findViewById(R.id.btn_register);
        tv_forgot_passwd = (TextView) findViewById(R.id.tv_find_passwd);
        iv_login_back = (ImageView) findViewById(R.id.iv_login_back);

        // if user changed, clear the password
        et_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                et_password.setText(null);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        // 监听多个输入框
        TextChange textChange = new TextChange();
        et_username.addTextChangedListener(textChange);
        et_password.addTextChangedListener(textChange);

        //TODO 此处可预置上次登陆的手机号
        //		if (DemoHelper.getInstance().getCurrentUsernName() != null) {
        //			et_usertel.setText(DemoHelper.getInstance().getCurrentUsernName());
        //		}


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginInSever(et_username.getText().toString().trim(), et_password.getText().toString().trim());
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, VerifyPhoneActivity.class);
                intent.putExtra("type", 0);
                startActivityForResult(intent, TYPE_USER_REGISTER);
                //finish();
            }
        });

        tv_forgot_passwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, VerifyPhoneActivity.class);
                intent.putExtra("type", 1);
                startActivityForResult(intent, TYPE_USER_FORGOT_PASSWORD);
            }
        });

        iv_login_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        et_username.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    et_username.setBackgroundResource(R.drawable.bg_edit_selected);
                } else {
                    et_username.setBackgroundResource(R.drawable.bg_edit_unselected);
                }
            }
        });
        et_password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    et_password.setBackgroundResource(R.drawable.bg_edit_selected);
                } else {
                    et_password.setBackgroundResource(R.drawable.bg_edit_unselected);
                }
            }
        });
        et_password.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER
                        && event.getRepeatCount() == 0) {
                    String userName = et_username.getText().toString().trim();
                    String password  = et_password.getText().toString().trim();
                    loginInSever(userName, password);

                    return true;
                }
                return false;
            }
        });

        //NetworkUtils.networkStateTips(LoginActivity.this);  // 检查网络状态
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            finish();
        }
    }

    private void loginInSever(String user, String password) {
        if(TextUtils.isEmpty(user) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage(getString(R.string.Is_landing));
        pd.show();
        List<Param> params = new ArrayList<Param>();
        params.add(new Param("username", user));
        params.add(new Param("password", password));
        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.LOGIN, new OkHttpManager.HttpCallBack() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                int code = jsonObject.getInteger("code");
                if (code == 1000) {
                    //JSONObject json = jsonObject.getJSONObject("userInfo");
                    setUpLogin(jsonObject);
                } else if (code == 2001) {
                    Toast.makeText(LoginActivity.this,
                            "用户名或密码错误...", Toast.LENGTH_SHORT)
                            .show();
                }else {
                }
                pd.dismiss();
            }
            @Override
            public void onFailure(String errorMsg) {
                pd.dismiss();
                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUpLogin(JSONObject jsonObject) {
        Map<String, Object> map = jsonObject.getObject("result", Map.class);

        JSONArray hi = (JSONArray) map.get("userInfo");

        Map<String, String> user = (Map<String, String>)hi.get(0);
        String id = user.get("serial");

        String lastId = ZyApplication.getInstance().getUserJson().getString(
                MyContants.JSON_KEY_ZYID);
        if(TextUtils.isEmpty(lastId) || !id.equals(lastId)) {  //当前用户已改变
            List<Param> params = new ArrayList<>();
            params.add(new Param("id", id));

            OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.GETUSERINFO, new
                    OkHttpManager.HttpCallBack() {
                        @Override
                        public void onResponse(JSONObject json) {
                            setUpUserJson(json);
                        }

                        @Override
                        public void onFailure(String errorMsg) {
                            Toast.makeText(LoginActivity.this, "获取用户信息失败", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        String isLogin = ZyApplication.getInstance().getUserJson().getString(MyContants.JSON_KEY_ISLOGIN);

        if(TextUtils.isEmpty(isLogin) || !isLogin.equals("1")) {
            JSONObject userJson = ZyApplication.getInstance().getUserJson();
            userJson.put(MyContants.JSON_KEY_ISLOGIN, "1");
            ZyApplication.getInstance().setUserJson(userJson);
        }
        Log.i("Id: ", id + " has been activity_login.");
        setResult(RESULT_OK);
        finish();
    }

    /**
     * 当用户id为空或用户已改变时，重新设置用户信息
     * @param jsonObject 用户数据
     */
    private void setUpUserJson(JSONObject jsonObject) {
        int code = jsonObject.getInteger("code");
        if(code == 1000) {
            Map<String, Object> map = jsonObject.getObject("result", Map.class);
            JSONArray array = (JSONArray) map.get("userInfo");
            if(!array.isEmpty()) {
                LocalUserUtil.getInstance().removeCurrentUserInfo();

                Map<String, String> user = (Map<String, String>) array.get(0);
                JSONObject userJson = new JSONObject();
                userJson.put(MyContants.JSON_KEY_ZYID, user.get("serial"));
                userJson.put(MyContants.JSON_KEY_NAME, user.get("userName"));
                userJson.put(MyContants.JSON_KEY_REAL_NAME, user.get("realName"));
                userJson.put(MyContants.JSON_KEY_PASSWORD, et_password.getText().toString().trim());
                userJson.put(MyContants.JSON_KEY_PHONE, user.get("phone"));
                userJson.put(MyContants.JSON_KEY_IDCARD, user.get("idCard"));
                userJson.put(MyContants.JSON_KEY_SEX, user.get("sex"));
                userJson.put(MyContants.JSON_KEY_BIRTH, user.get("birth"));
                userJson.put(MyContants.JSON_KEY_BLOODTYPE, user.get("blood"));
                userJson.put(MyContants.JSON_KEY_ADDRESS, user.get("address"));
                userJson.put(MyContants.JSON_KEY_AVATAR, user.get("image"));
                userJson.put(MyContants.JSON_KEY_ISLOGIN, "1");
                ZyApplication.getInstance().setUserJson(userJson);
            }
        } else {
            Toast.makeText(LoginActivity.this, "服务器返回信息错误", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String isLogin = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.JSON_KEY_ISLOGIN);
        if(!TextUtils.isEmpty(isLogin) && isLogin.equals("1")) {
            return;
        }
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

            boolean Sign1 = et_username.getText().length() > 0;
            boolean Sign2 = et_password.getText().length() > 0;

            if (Sign1 && Sign2) {
                btn_login.setEnabled(true);
                btn_login.setBackgroundColor(getResources().getColor(R.color.theme_default_color));
            }
            // 在layout文件中，对Button的text属性应预先设置默认值，否则刚打开程序的时候Button是无显示的
            else {
                btn_login.setEnabled(false);
                btn_login.setBackgroundColor(getResources().getColor(R.color.abs__bright_foreground_disabled_holo_light));
            }
        }

    }

}
