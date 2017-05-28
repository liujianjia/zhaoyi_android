package com.zhaoyi.walker.activity;

import android.content.Context;
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
import com.zhaoyi.walker.adapter.MyCollectionDocsAdapter;
import com.zhaoyi.walker.model.CurrentHosInfo;
import com.zhaoyi.walker.model.ResultFromServer;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jianjia Liu on 2017/5/4.
 */

public class MyCollectionActivity extends FragmentActivity {
    private Context context;
    private ListView lvCollections;
    private ArrayList<ArrayList<String>> mMyCollectionDocs;
    private int lastEntryPos;
    private MyCollectionDocsAdapter myCollectionDocsAdapter;
    private final int RESULT_CANCEL_COLLECTION_OK = 112;
    private final int TYPE_REQUEST_CANCEL_COLLECTION = 113;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mycollections);

        lvCollections = (ListView) findViewById(R.id.lv_collections);

        context = MyCollectionActivity.this;

        ((TextView) findViewById(R.id.tv_top_title)).setText("我的收藏");
        findViewById(R.id.iv_top_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mMyCollectionDocs = new ArrayList<>();
        lvCollections.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mMyCollectionDocs.get(position) == null) {
                    return;
                }
                lastEntryPos = position;
                enterDetailPage(position);
            }
        });
        getMyCollectionsDocs();
    }

    private void getMyCollectionsDocs() {
        List<Param> params = new ArrayList<>();
        String isLogin = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.JSON_KEY_ISLOGIN);
        if(TextUtils.isEmpty(isLogin) || !isLogin.equals("1")) {
            startActivity(new Intent(MyCollectionActivity.this, LoginActivity.class));
            finish();
        }
        String userId = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.JSON_KEY_ZYID);
        if(TextUtils.isEmpty(userId)) {
            Log.e("Internal error: ", "userId is empty.");
            return;
        }
        params.add(new Param("userId", userId));

        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.GETMYCOLLECTIONDOCS,
                new OkHttpManager.HttpCallBack() {
                    @Override
                    public void onResponse(JSONObject json) {
                        int code = json.getInteger("code");
                        if(code == 1000) {
                            JSONObject obj = (JSONObject)json.get("result");
                            JSONArray patientInfo = (JSONArray)obj.get("StoreDoc");

                            int size = patientInfo.size();
                            DecimalFormat df = new DecimalFormat("0.0");

                            for(int i = 0; i < size; i++) {
                                Map<String, Object> m = (Map<String, Object>)patientInfo.get(i);
                                ArrayList<String> iList = new ArrayList<>();
                                iList.add((String)m.get("id"));
                                iList.add(MyContants.BASE_URL + m.get("image"));
                                iList.add((String)m.get("docname"));
                                iList.add(df.format(m.get("score")).toString());
                                iList.add((String)m.get("reg_count"));
                                iList.add((String)m.get("title"));
                                iList.add((String)m.get("offname"));
                                iList.add((String)m.get("hosname"));
                                mMyCollectionDocs.add(iList);
                            }
                            myCollectionDocsAdapter = new
                                    MyCollectionDocsAdapter(MyCollectionActivity.this, mMyCollectionDocs);
                            lvCollections.setAdapter(myCollectionDocsAdapter);
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {

                    }
                });
    }

    private void enterDetailPage(int pos) {
        List<Param> params = new ArrayList<Param>();

        String docName = null;
        String depName = null;
        String hospName = null;
        String docId = null;
        if(mMyCollectionDocs.get(pos) != null) {
            docId = mMyCollectionDocs.get(pos).get(0);
            docName = mMyCollectionDocs.get(pos).get(2);
            hospName = mMyCollectionDocs.get(pos).get(7);
            depName = mMyCollectionDocs.get(pos).get(6);
        }

        String userId = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.JSON_KEY_ZYID);
        if(TextUtils.isEmpty(docName)) {
            Toast.makeText(context, "内部错误：1001", Toast.LENGTH_SHORT).show();
            Log.e("Fatal Error: ", "docName shouldn't be use as empty.");
            return;
        }
        if(TextUtils.isEmpty(hospName)) {
            Toast.makeText(context, "内部错误：1001", Toast.LENGTH_SHORT).show();
            Log.e("Fatal Error: ", "hosName shouldn't be use as empty.");
            return;
        }
        if(TextUtils.isEmpty(depName)) {
            Toast.makeText(context, "内部错误：1001", Toast.LENGTH_SHORT).show();
            Log.e("Fatal Error: ", "department shouldn't be use as empty.");
            return;
        }
        if(TextUtils.isEmpty(docId)) {
            Toast.makeText(context, "内部错误：1001", Toast.LENGTH_SHORT).show();
            Log.e("Fatal Error: ", "docId shouldn't be use as empty.");
            return;
        }
        if(TextUtils.isEmpty(docName)) {
            Toast.makeText(context, "内部错误：1001", Toast.LENGTH_SHORT).show();
            Log.e("Fatal Error: ", "docName shouldn't be use as empty.");
            return;
        }

        CurrentHosInfo.setCurHos(hospName);
        CurrentHosInfo.setCurDepartment(depName);

        params.add(new Param("docname", docName));
        params.add(new Param("hosname", hospName));
        params.add(new Param("offname", depName));
        params.add(new Param("docId", docId));
        params.add(new Param("userId", userId));

        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.ENTERDETAILDOCTORPAGE,
                new OkHttpManager.HttpCallBack() {
            @Override
            public void onResponse(JSONObject json) {
                int code = json.getInteger("code");
                if (code == 1000) {
                    try {
                        Map<String, Object> map = json.getObject("result", Map.class);

                        JSONArray hi = (JSONArray) map.get("DocInfo");
                        ResultFromServer.initDoctor();
                        for(int i = 0; i < hi.size(); i++) {
                            Map<String, Object> n = hi.getObject(i, Map.class);
                            ResultFromServer.getDoctor().add(n);
                        }
                        MyCollectionActivity.this.startActivityForResult(new Intent(MyCollectionActivity.this, DoctorDetailInfoActivity.class),
                                TYPE_REQUEST_CANCEL_COLLECTION);
                        //finish();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    Toast.makeText(MyCollectionActivity.this,
                            "服务器繁忙请重试...", Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(String errorMsg) {

            }

        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TYPE_REQUEST_CANCEL_COLLECTION) {
            if(resultCode == RESULT_CANCEL_COLLECTION_OK) {
                mMyCollectionDocs.remove(lastEntryPos);
                myCollectionDocsAdapter.notifyDataSetChanged();
            }
        }
    }
}
