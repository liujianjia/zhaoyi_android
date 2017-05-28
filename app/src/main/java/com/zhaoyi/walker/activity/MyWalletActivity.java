package com.zhaoyi.walker.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.ZyApplication;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jianjia Liu on 2017/5/8.
 */

public class MyWalletActivity extends FragmentActivity {
    private TextView tvBalance;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mywallet);

        tvBalance = (TextView) findViewById(R.id.tv_balance);

        findViewById(R.id.rl_charge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                charge();
            }
        });

        userId = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.JSON_KEY_ZYID);

        getBalance();
        findViewById(R.id.rl_charge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                charge();
            }
        });
        findViewById(R.id.rl_change_pay_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyWalletActivity.this, RecoverPayPasswordActivity.class));
            }
        });
    }

    private void charge() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MyWalletActivity.this);
        final EditText editText = new EditText(MyWalletActivity.this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(editText);
        builder.setTitle("充值金额(元)")
                .setPositiveButton("充值", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String s = editText.getText().toString().trim();
                        if(TextUtils.isEmpty(s)) {
                            return;
                        }
                        Float money = Float.parseFloat(s);
                        if(money < 0) {
                            Toast.makeText(MyWalletActivity.this, "金额不能为负", Toast.LENGTH_SHORT).show();
                            return;
                        } else if(money == 0) {
                            Toast.makeText(MyWalletActivity.this, "金额不能为0", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(TextUtils.isEmpty(userId)) {
                            Log.e("Error: ", "user id can't be empty.");
                            return;
                        }
                        List<Param> params = new ArrayList<>();
                        params.add(new Param("userId", userId));
                        params.add(new Param("money", s));

                        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.CHARGEWALLET,
                                new OkHttpManager.HttpCallBack() {
                                    @Override
                                    public void onResponse(JSONObject json) {
                                        int code = json.getInteger("code");
                                        if(code == 1000) {
                                            Toast.makeText(MyWalletActivity.this, "充值成功", Toast.LENGTH_SHORT).show();
                                            getBalance();
                                        } else if(code == 2001) {
                                            Toast.makeText(MyWalletActivity.this, "充值失败", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(String errorMsg) {

                                    }
                                });
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void getBalance() {
        if(TextUtils.isEmpty(userId)) {
            Log.e("Error: ", "user id can't be empty.");
            return;
        }
        List<Param> params = new ArrayList<>();
        params.add(new Param("userId", userId));

        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.GETMONEYCOUNT,
                new OkHttpManager.HttpCallBack() {
                    @Override
                    public void onResponse(JSONObject json) {
                        int code = json.getInteger("code");
                        if(code == 1000) {
                            Map<String, Object> map = json.getObject("result", Map.class);
                            String money = (String) map.get("userMoney");
                            tvBalance.setText(money);
                        } else if(code == 2001) {
                            Toast.makeText(MyWalletActivity.this, "获取余额信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {

                    }
                });
    }
}
