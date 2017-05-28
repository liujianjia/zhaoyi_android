package com.zhaoyi.walker.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
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
import java.util.regex.Pattern;

/**
 * Created by jianjia Liu on 2017/5/4.
 */

public class AddNewPatientActivity extends FragmentActivity implements View.OnClickListener,
        InputDialogFragment.InputListener {
    private TextView tvName;
    private TextView tvIdCard;
    private TextView tvSex;
    private TextView tvBirth;
    private TextView tvPhone;
    private TextView tvSocNumber;
    private TextView tvAddress;
    private CheckBox cbIsDefault;
    private ArrayList<String> info;
    String[] newPatient = new String[8];

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

        ((TextView)findViewById(R.id.tv_top_title)).setText("添加就诊人");

        findViewById(R.id.iv_top_back).setOnClickListener(this);
        findViewById(R.id.rl_name).setOnClickListener(this);
        findViewById(R.id.rl_id_card).setOnClickListener(this);
        findViewById(R.id.rl_sex).setOnClickListener(this);
        findViewById(R.id.rl_birth).setOnClickListener(this);
        findViewById(R.id.rl_phone).setOnClickListener(this);
        findViewById(R.id.rl_se_number).setOnClickListener(this);
        findViewById(R.id.rl_address).setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        InputDialogFragment inputDialogFragment;
        switch (v.getId()) {
            case R.id.rl_sex:
                inputDialogFragment = InputDialogFragment.newInstance(2);
                inputDialogFragment.show(getFragmentManager(), "Dialog");
                break;
            case R.id.rl_birth:
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddNewPatientActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                //tvBirth.setText(year + "-" + month + "-" + dayOfMonth);
                                int monthPlus = month + 1;
                                String value = year + "-" + monthPlus + "-" + dayOfMonth;
                                newPatient[3] = value;
                                tvBirth.setText(value);
                            }
                        }, 1990, 1, 1);
                datePickerDialog.show();
                break;
            case R.id.rl_name:
                inputDialogFragment = InputDialogFragment.newInstance(3);
                inputDialogFragment.show(getFragmentManager(), "Dialog");
                break;
            case R.id.rl_id_card:
                inputDialogFragment = InputDialogFragment.newInstance(4);
                inputDialogFragment.show(getFragmentManager(), "Dialog");
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
            case R.id.btn_save:
                postNewPatientInfo();
                break;
            case R.id.iv_top_back:
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * 输入框调用完成回调接口
     * @param obj 输入内容
     * @param index 调用者
     */
    @Override
    public void onInputComplete(Object obj, int index) {
        if(obj instanceof String) {
            String value = (String)obj;
            switch (index) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    newPatient[2] = value;
                    tvSex.setText(value);
                    break;
                case 3:
                    newPatient[0] = value;
                    tvName.setText(value);
                    break;
                case 4:
                    newPatient[1] = value;
                    tvIdCard.setText(value);
                    break;
                case 5:
                    newPatient[4] = value;
                    tvPhone.setText(value);
                    break;
                case 6:
                    newPatient[5] = value;
                    tvSocNumber.setText(value);
                    break;
                case 7:
                    newPatient[6] = value;
                    tvAddress.setText(value);
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 根据用户输入的信息进行检查并向服务器添加数据
     */
    private void postNewPatientInfo() {
        if(TextUtils.isEmpty(tvName.getText())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AddNewPatientActivity.this);
            builder.setPositiveButton("确定", null)
                    .setMessage("请填写一下姓名哦")
                    .show();
            return;
        }
        if(TextUtils.isEmpty(tvIdCard.getText())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AddNewPatientActivity.this);
            builder.setPositiveButton("确定", null)
                    .setMessage("请填写一下身份证哦")
                    .show();
            return;
        }

        String userId = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.JSON_KEY_ZYID);
        if(TextUtils.isEmpty(userId)) {
            return;
        }
        String phoneRegEx = "1[0-9]{10}";
        String phone = tvPhone.getText().toString().trim();
        if(!TextUtils.isEmpty(phone) && !Pattern.compile(phoneRegEx).matcher(phone).matches()) {
            Toast.makeText(AddNewPatientActivity.this, "手机号码格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        String userIdCardRegEx = "[0-9]{6}((1[8|9][0-9][0-9])|20(0[0-9]|1[0-7]))" +
                "(0[1-9]|1[0-2])(0[1-9]|((1|2)[0-9])|3(0|1))[0-9]{3}([0-9]|x)";
        if(!Pattern.compile(userIdCardRegEx).matcher(tvIdCard.getText().toString()).matches()) {
            Toast.makeText(AddNewPatientActivity.this, "身份证格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!TextUtils.isEmpty(tvSocNumber.getText().toString().trim()) &&
                !Pattern.compile(userIdCardRegEx).matcher(tvSocNumber.getText().toString().trim()).matches()) {
            Toast.makeText(AddNewPatientActivity.this, "社保卡号格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Param> params = new ArrayList<>();
        params.add(new Param("user_id", userId));
        params.add(new Param("real_name", tvName.getText().toString()));
        params.add(new Param("id_card", tvIdCard.getText().toString()));
        params.add(new Param("sex", tvSex.getText().toString()));
        params.add(new Param("birth", tvBirth.getText().toString()));
        params.add(new Param("phone", TextUtils.isEmpty(phone) ? "" :
                tvPhone.getText().toString()));
        params.add(new Param("soc_card", TextUtils.isEmpty(tvSocNumber.getText()) ? "" :
                tvSocNumber.getText().toString()));
        params.add(new Param("address", TextUtils.isEmpty(tvAddress.getText()) ? "" :
                tvAddress.getText().toString()));
        String isDefault = cbIsDefault.isChecked() ? "1" : "0";
        params.add(new Param("is_default", isDefault));

        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.ADDNEWPATIENT,
                new OkHttpManager.HttpCallBack() {
                    @Override
                    public void onResponse(JSONObject json) {
                        int code = json.getInteger("code");
                        if(code == 1000) {
                            Toast.makeText(AddNewPatientActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }
                        else if(code == 2000){
                            Toast.makeText(AddNewPatientActivity.this, "添加失败，已达到最大就诊人限制", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(AddNewPatientActivity.this, "添加失败，未知错误", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {

                    }
                });
    }
}
