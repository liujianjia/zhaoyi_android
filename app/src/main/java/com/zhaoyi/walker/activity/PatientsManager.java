package com.zhaoyi.walker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.ZyApplication;
import com.zhaoyi.walker.adapter.PatientManagerAdapter;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jianjia Liu on 2017/5/4.
 */

public class PatientsManager extends FragmentActivity {
    private TextView tvDesc;
    private ListView lvPatients;
    private ArrayList<ArrayList<String>> info;
    private int maxPatient;
    private final int TYPE_REQUEST_ITEM_STATUS = 114;
    private final int TYPE_REQUEST_PATIENT_STATUS = 115;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypatients);

        lvPatients = (ListView)findViewById(R.id.lv_patient);
        tvDesc = (TextView) findViewById(R.id.tv_desc);
        findViewById(R.id.rl_new_patient).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PatientsManager.this, AddNewPatientActivity.class);
                PatientsManager.this.startActivityForResult(intent, TYPE_REQUEST_PATIENT_STATUS);
            }
        });

        ((TextView) findViewById(R.id.tv_top_title)).setText("就诊人管理");
        findViewById(R.id.iv_top_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        info = new ArrayList<>();
        getPatientInfo();
        lvPatients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PatientsManager.this, AddNewPatientActivity.class);
                PatientsManager.this.startActivity(intent);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if(requestCode == TYPE_REQUEST_ITEM_STATUS ||
                    requestCode == TYPE_REQUEST_PATIENT_STATUS) {
                refreshPatientList();
            }
        }
    }

    private void refreshPatientList() {
        info.clear();
        getPatientInfo();
    }

    private void getPatientInfo() {
        List<Param> params = new ArrayList<>();
        String userId = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.JSON_KEY_ZYID);
        if(TextUtils.isEmpty(userId)) {
            return;
        }
        params.add(new Param("userid", userId));
        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.GETPAINTINFO,
                new OkHttpManager.HttpCallBack() {
                    @Override
                    public void onResponse(JSONObject json) {
                        int code = json.getInteger("code");
                        if(code == 1000) {
                            JSONObject obj = (JSONObject)json.get("result");
                            JSONArray patientInfo = (JSONArray)obj.get("PatientInfo");
                            maxPatient = obj.getInteger("maxPatient");
                            int size = patientInfo.size();
                            int remain = maxPatient - size;
                            if(remain < 0) {
                                remain = 0;
                            }
                            tvDesc.setText("（已添加" + size + "人，还可以添加" + remain + "人）");
                            for(int i = 0; i < size; i++) {
                                Map<String, Object> m = (Map<String, Object>)patientInfo.get(i);
                                ArrayList<String> iList = new ArrayList<>();
                                iList.add((String)m.get("id"));
                                iList.add((String)m.get("realName"));
                                iList.add((String)m.get("idCard"));
                                iList.add((String)m.get("sex"));
                                iList.add((String)m.get("birth"));
                                iList.add((String)m.get("phone"));
                                iList.add((String)m.get("socCard"));
                                iList.add((String)m.get("address"));
                                iList.add((String)m.get("isDefault"));
                                iList.add((String)m.get("isUser"));
                                info.add(iList);
                            }
                            PatientManagerAdapter patientManagerAdapter = new
                                    PatientManagerAdapter(PatientsManager.this, info);
                            lvPatients.setAdapter(patientManagerAdapter);
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {

                    }
                });
    }
}
