package com.zhaoyi.walker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.ZyApplication;
import com.zhaoyi.walker.adapter.PatientRecordAdapter;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by jianjia Liu on 2017/4/10.
 */

public class PatientRecordActivity extends FragmentActivity {
    private ArrayList<ArrayList<String>> mList;
    private ListView lvRecord;
    private ArrayList<String> mOrder;
    private int lastEntryPos;
    private PatientRecordAdapter patientRecordAdapter;
    private ArrayList<String> mData;

    private final int TYPE_REQUEST_ORDER_STATUS = 107;  // 请求订单状态
    private final int RESULT_DELETER_ORDER_OK = 111;  // 删除订单成功
    private final int RESULT_COMMENT_OK = 112;  // 评论成功

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_record);

        lvRecord = (ListView) findViewById(R.id.lv_patient_record);

        findViewById(R.id.iv_top_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ((TextView)findViewById(R.id.tv_top_title)).setText("就诊记录");

        requestRecord();

        mList = new ArrayList<>();
        mOrder = new ArrayList<>();
    }

    private void requestRecord() {
        String isLogin = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.JSON_KEY_ISLOGIN);
        if(TextUtils.isEmpty(isLogin) || !isLogin.equals("1")) {
            startActivity(new Intent(PatientRecordActivity.this, LoginActivity.class));
            finish();
        }
        String userId = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.JSON_KEY_ZYID);
        if(TextUtils.isEmpty(userId)) {
            Log.e("Internal error: ", "userId is empty.");
            return;
        }

        List<Param> params = new ArrayList<>();
        params.add(new Param("userId", userId));
        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.GETRECORD,
                new OkHttpManager.HttpCallBack() {
                    @Override
                    public void onResponse(JSONObject json) {
                        int code = json.getInteger("code");
                        if(code == 1000) {
                            Map<String, Object> jsonObj = json.getObject("result", Map.class);
                            JSONArray array = (JSONArray)jsonObj.get("RecordInfo");
                            int size = array.size();

                            for(int i = 0; i < size; i++) {
                                Map<String, String> map = (Map<String, String>)array.get(i);
                                String status = map.get("done");
                                if(status.equals("已评价")) {
                                    status = "";
                                }

                                mData = new ArrayList<>();
                                mData.add(map.get("orderid"));
                                mData.add(map.get("visitTime"));
                                mData.add(status);
                                mList.add(mData);
                            }
                            initRecord();
                        } else {
                            Toast.makeText(PatientRecordActivity.this, "当前没有就诊记录",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {

                    }
                });
    }

    private void initRecord() {
        Collections.sort(mList, new Comparator<ArrayList<String>>() {
            @Override
            public int compare(ArrayList<String> o1, ArrayList<String> o2) {
                return o2.get(1).compareTo(o1.get(1));
            }
        });
        patientRecordAdapter = new PatientRecordAdapter(PatientRecordActivity.this, mList);

        mOrder = new ArrayList<>();
        for(List<String> m : mList) {
            mOrder.add(m.get(0));
        }

        lvRecord.setAdapter(patientRecordAdapter);
        lvRecord.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lastEntryPos = position;
                Intent intent = new Intent(PatientRecordActivity.this, PatientRecordDetailActivity.class);
                intent.putExtra("type", 1);
                intent.putExtra("data", mOrder.get(position));
                PatientRecordActivity.this.startActivityForResult(intent, TYPE_REQUEST_ORDER_STATUS);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TYPE_REQUEST_ORDER_STATUS) {
            if(resultCode == RESULT_DELETER_ORDER_OK && lastEntryPos < mOrder.size()) {
                mList.remove(lastEntryPos);
                patientRecordAdapter.notifyDataSetChanged();
            }
            else if(resultCode == RESULT_COMMENT_OK && lastEntryPos < mOrder.size()) {
                mList.get(lastEntryPos).remove(2);
                patientRecordAdapter.notifyDataSetChanged();
            }
        }
    }
}
