package com.zhaoyi.walker.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.ZyApplication;
import com.zhaoyi.walker.model.CurrentHosInfo;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jianjia Liu on 2017/4/1.
 */

public class ConfirmAppointmentActivity extends FragmentActivity {
    private TextView tvBasicInfo;
    private ImageView  ivBack;
    private TextView tvTitle;
    private TextView tvUser;
    private Button btnUser;
    private Button btnSubmit;
    private RadioButton rbMyWallet;
    private RadioButton rbPayLater;
    private ArrayList<String> patientId;
    private ArrayList<String> patientName;
    private ArrayList<String> isDefault;
    private String[] patName;
    private int lastSelectedItem;  // 选择的就诊人

    private Bundle args;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_appointment);

        tvBasicInfo = (TextView)findViewById(R.id.tv_basic_info);
        tvTitle = (TextView)findViewById(R.id.tv_top_title);
        tvUser = (TextView)findViewById(R.id.tv_user);
        ivBack = (ImageView)findViewById(R.id.iv_top_back);
        btnUser = (Button) findViewById(R.id.btn_user);
        btnSubmit = (Button)findViewById(R.id.btn_submit);
        rbMyWallet = (RadioButton) findViewById(R.id.rb_my_wallet);
        rbPayLater = (RadioButton) findViewById(R.id.rb_pay_later);

        tvTitle.setText("确认预约");
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        args = getIntent().getExtras();
        patientId = args.getStringArrayList("patientId");
        patientName = args.getStringArrayList("patientName");
        isDefault = args.getStringArrayList("isDefault");

        int size = patientName.size();
        patName = new String[size];
        lastSelectedItem = 0;
        for(int i = 0; i < size; i++) {
            patName[i] = patientName.get(i);
            if(isDefault.get(i).equals("1")) {
                patName[i] += "（默认）";
                btnUser.setText(patName[i]);
                lastSelectedItem = i;
            }
        }

        btnUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder inputDialog = new AlertDialog.Builder(ConfirmAppointmentActivity.this);
                inputDialog.setItems(patName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       btnUser.setText(patName[which]);
                        lastSelectedItem = which;
                    }
                }).show();
            }
        });

        tvBasicInfo.setText("就诊医院：" + CurrentHosInfo.getCurHos() + "\n" +
            "就诊科室：" + CurrentHosInfo.getCurDepartment() + "\n" +
            "就诊地址：" + CurrentHosInfo.getCurHosAddress() + "\n" +
            "就诊医生：" + CurrentHosInfo.getCurDoctor() + "\n" +
            "医生职称：" + CurrentHosInfo.getCurDoctorRole() + "\n" +
            "费用：" + CurrentHosInfo.getCurCost() + "\n" +
            "就诊日期：" + args.getString("date") + "\n" +
            "就诊时间：" + args.getString("time") + "\n " +
            "号签：" + args.getString("number"));
        rbMyWallet.setChecked(true);
        ((RadioGroup) findViewById(R.id.rg_pay_type)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.rb_my_wallet:
                        btnSubmit.setText("支付");
                        break;
                    case R.id.rb_pay_later:
                        btnSubmit.setText("预约");
                        break;
                    default:
                        break;
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payAtAccountOrLater();
            }
        });
    }

    /**
     * 选择支付方式
     */
    private void payAtAccountOrLater() {
        if(rbMyWallet.isChecked()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ConfirmAppointmentActivity.this);
            final EditText editText = new EditText(ConfirmAppointmentActivity.this);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            builder.setView(editText);
            builder.setTitle("支付密码")
                    .setPositiveButton("支付", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String password = editText.getText().toString().trim();
                            if (TextUtils.isEmpty(password)) {
                                Toast.makeText(ConfirmAppointmentActivity.this, "密码不能为空",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            makeAppointment(password);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            makeAppointment("");
        }
    }

    /**
     * 提交挂号请求
     * @param password 支付密码
     */
    private void makeAppointment(String password) {
        if(password == null) {
            Log.e("Error: ", "pay Password can't be null.");
            return;
        }
        List<Param> params = new ArrayList<Param>();
        params.add(new Param("hosName", CurrentHosInfo.getCurHos()));
        params.add(new Param("offName", CurrentHosInfo.getCurDepartment()));
        params.add(new Param("docName", CurrentHosInfo.getCurDoctor()));
        params.add(new Param("cost", CurrentHosInfo.getCurCost()));
        params.add(new Param("realName", patientName.get(lastSelectedItem)));
        params.add(new Param("userid", ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.JSON_KEY_ZYID)));
        params.add(new Param("patId", patientId.get(lastSelectedItem)));
        params.add(new Param("date", args.getString("date")));
        params.add(new Param("time", args.getString("afternoon")));
        params.add(new Param("number", args.getString("number")));
        String payType = rbMyWallet.isChecked() ? "1" : "0";
        params.add(new Param("method", payType));
        params.add(new Param("payPwd", password));

        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.MAKEAPPOINTMENT,
                new OkHttpManager.HttpCallBack() {
                    @Override
                    public void onResponse(JSONObject json) {
                        try {
                            int code = json.getInteger("code");
                            if (code == 1000) {
                                try {
                                    Log.i(ZyApplication.getInstance()
                                            .getUserJson()
                                            .getString(MyContants.JSON_KEY_NAME) +
                                            "(" +
                                            ZyApplication.getInstance()
                                                    .getUserJson()
                                                    .getString(MyContants.JSON_KEY_ZYID) +
                                            "): ", "get number success.");
                                    Toast.makeText(ConfirmAppointmentActivity.this, "挂号成功", Toast.LENGTH_LONG).show();
                                    setResult(RESULT_OK);
                                    finish();
                                }
                                catch (Exception e) {
                                    Log.e("Error: ", e.getMessage());
                                    e.printStackTrace();
                                }
                            } else if(code == 2000) {
                                Toast.makeText(ConfirmAppointmentActivity.this, "您已达到最大挂号数", Toast.LENGTH_LONG).show();
                            } else if(code == 2001) {
                                Toast.makeText(ConfirmAppointmentActivity.this, "您不能重复预约同一时段", Toast.LENGTH_LONG).show();
                            } else if(code == 2002){
                                Toast.makeText(ConfirmAppointmentActivity.this,
                                        "您的帐户余额不足", Toast.LENGTH_SHORT)
                                        .show();
                            } else if(code == 2003){
                                Toast.makeText(ConfirmAppointmentActivity.this,
                                        "支付密码错误", Toast.LENGTH_SHORT)
                                        .show();
                            } else if(code == 2004) {
                                Toast.makeText(ConfirmAppointmentActivity.this,
                                        "未知错误", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {

                    }
                }
        );
    }
}
