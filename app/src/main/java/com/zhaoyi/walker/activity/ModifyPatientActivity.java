package com.zhaoyi.walker.activity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.Fragment.InputDialogFragment;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.ZyApplication;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jianjia Liu on 2017/5/4.
 */

public class ModifyPatientActivity extends FragmentActivity implements View.OnClickListener,
        InputDialogFragment.InputListener {
    private TextView tvName;
    private TextView tvIdCard;
    private TextView tvSex;
    private TextView tvBirth;
    private TextView tvPhone;
    private TextView tvSocNumber;
    private TextView tvAddress;
    private CheckBox cbIsDefault;
    private ArrayList<String> info;  // 就诊人信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_patient);

        tvName = (TextView) findViewById(R.id.tv_name);
        tvIdCard = (TextView) findViewById(R.id.tv_id_card);
        tvSex = (TextView) findViewById(R.id.tv_sex);
        tvBirth = (TextView) findViewById(R.id.tv_birth);
        tvPhone = (TextView) findViewById(R.id.tv_phone);
        tvSocNumber = (TextView) findViewById(R.id.tv_se_nmber);
        tvAddress = (TextView) findViewById(R.id.tv_address);
        cbIsDefault = (CheckBox) findViewById(R.id.cb_default);

        Bundle args = getIntent().getExtras();
        info = args.getStringArrayList("data");

        tvName.setText(info.get(1));
        tvIdCard.setText(info.get(2));
        tvSex.setText(info.get(3));
        tvBirth.setText(info.get(4));
        tvPhone.setText(info.get(5));
        tvSocNumber.setText(info.get(6));
        tvAddress.setText(info.get(7));
        if(info.get(8).equals("1")) {
            cbIsDefault.setChecked(true);
            cbIsDefault.setEnabled(false);
        }
        ((Button) findViewById(R.id.btn_save)).setText("修改");

        ((TextView)findViewById(R.id.tv_top_title)).setText("修改就诊人");
        findViewById(R.id.iv_top_back).setOnClickListener(this);

        findViewById(R.id.rl_sex).setOnClickListener(this);
        findViewById(R.id.rl_birth).setOnClickListener(this);
        findViewById(R.id.rl_phone).setOnClickListener(this);
        findViewById(R.id.rl_se_number).setOnClickListener(this);
        findViewById(R.id.rl_address).setOnClickListener(this);
        cbIsDefault.setOnClickListener(this);
        findViewById(R.id.btn_save).setVisibility(Button.GONE);
    }

    @Override
    public void onClick(View v) {
        InputDialogFragment inputDialogFragment;

        switch(v.getId()) {
            case R.id.rl_sex:
                inputDialogFragment = InputDialogFragment.newInstance(2);
                inputDialogFragment.show(getFragmentManager(), "Dialog");
                break;
            case R.id.rl_birth:
                DatePickerDialog datePickerDialog = new DatePickerDialog(ModifyPatientActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                int monthPlus = month + 1;
                                String value = year + "-" + monthPlus + "-" + dayOfMonth;
                                updateInServer("birth", value);
                            }
                        }, 1990, 1, 1);
                datePickerDialog.show();
                break;
            case R.id.rl_phone:
                inputDialogFragment = InputDialogFragment.newInstance(5);
                inputDialogFragment.show(getFragmentManager(), "Dialog");
                break;
            case R.id.rl_se_number:
                inputDialogFragment = InputDialogFragment.newInstance(6);
                inputDialogFragment.show(getFragmentManager(), "Dialog");
                break;
            case R.id.rl_address:
                inputDialogFragment = InputDialogFragment.newInstance(7);
                inputDialogFragment.show(getFragmentManager(), "Dialog");
                break;
            case R.id.iv_top_back:
                finish();
                break;
            case R.id.cb_default:
                String isDefault = cbIsDefault.isChecked() ? "1" : "0";
                updateInServer("is_default", isDefault);
                break;
            default:
                break;
        }
    }

    @Override
    public void onInputComplete(Object obj, int index) {
        if (obj instanceof String) {
            String value = (String) obj;
            String key = null;
            switch (index) {
                case 2:
                    key = "sex";
                    break;
                case 5:
                    key = "phone";
                    break;
                case 6:
                    key = "soc_card";
                    break;
                case 7:
                    key = "address";
                    break;
                default:
                    break;
            }
            updateInServer(key, value);
        }
    }

    private void updateInServer(final String key, final String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在更新...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        List<Param> params = new ArrayList<Param>();
        params.add(new Param("key", key));
        params.add(new Param("value", value));
        String userId = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.JSON_KEY_ZYID);
        if (TextUtils.isEmpty(userId)) {
            return;
        }
        params.add(new Param("userId", userId));
        params.add(new Param("id", info.get(0)));

        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.MODIFYPATIENTIFNO,
                new OkHttpManager.HttpCallBack() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        progressDialog.dismiss();
                        int code = jsonObject.getIntValue("code");
                        if (code == 1000) {
                            switch (key) {
                                case "sex":
                                    tvSex.setText(value);
                                    break;
                                case "birth":
                                    tvBirth.setText(value);
                                    break;
                                case "phone":
                                    tvPhone.setText(value);
                                    break;
                                case "soc_card":
                                    tvSocNumber.setText(value);
                                    break;
                                case "address":
                                    tvAddress.setText(value);
                                    break;
                            }
                            Toast.makeText(getApplicationContext(), "更新成功", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                        } else {
                            Toast.makeText(getApplicationContext(), "更新失败,code:" + code, Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        progressDialog.dismiss();
                    }
                });
    }
}
