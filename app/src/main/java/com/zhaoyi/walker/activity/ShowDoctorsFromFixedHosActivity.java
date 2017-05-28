package com.zhaoyi.walker.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.ZyApplication;
import com.zhaoyi.walker.adapter.DoctorAdapter;
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
 * Created by jianjia Liu on 2017/3/27.
 */

public class ShowDoctorsFromFixedHosActivity extends FragmentActivity {
    private ListView lvCityHospital;
    private ImageView ivBack;
    private TextView tvTitle;
    private DoctorAdapter adapter;
    private ArrayList<String> docId;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hosp_list);

        context = ShowDoctorsFromFixedHosActivity.this;
        lvCityHospital = (ListView) this.findViewById(R.id.list_city_hospital);

        ivBack = (ImageView) findViewById(R.id.iv_top_back);
        tvTitle = (TextView) findViewById(R.id.tv_top_title);
        tvTitle.setText(CurrentHosInfo.getCurDepartment());
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.ll_ui_container).setVisibility(LinearLayout.GONE);

        List<String> url = new ArrayList<>();
        List<String> name = new ArrayList<>();
        List<String> desc = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("0.0");
        int size = ResultFromServer.getAllDoctor().size();
        docId = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Map<String, Object> result = ResultFromServer.getAllDoctor().get(i);
            url.add(MyContants.BASE_URL + result.get("image"));
            name.add((String)result.get("docname"));
            desc.add(String.format(result.get("title") + "    " +
                    df.format(result.get("score"))));
            docId.add((String)result.get("id"));
        }
        adapter = new DoctorAdapter(this, url, name, desc);

        lvCityHospital.setAdapter(adapter);
        lvCityHospital.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                enterDetailPage(position);
            }
        });
    }
    private void enterDetailPage(int pos) {
        List<Param> params = new ArrayList<Param>();

        String docName = null;
        try {
            docName = (String)adapter.getItem(pos);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        String isLogin = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.JSON_KEY_ISLOGIN);
        String userId = "";
        if(!TextUtils.isEmpty(isLogin) && isLogin.equals("1")) {
            userId = ZyApplication.getInstance()
                    .getUserJson()
                    .getString(MyContants.JSON_KEY_ZYID);
        }

        if(TextUtils.isEmpty(docName)) {
            Toast.makeText(context, "内部错误：1001", Toast.LENGTH_SHORT).show();
            Log.e("Fatal Error: ", "docName shouldn't be use as empty.");
            return;
        }
        if(TextUtils.isEmpty(CurrentHosInfo.getCurHos())) {
            Toast.makeText(context, "内部错误：1001", Toast.LENGTH_SHORT).show();
            Log.e("Fatal Error: ", "hosName shouldn't be use as empty.");
            return;
        }
        if(TextUtils.isEmpty(CurrentHosInfo.getCurDepartment())) {
            Toast.makeText(context, "内部错误：1001", Toast.LENGTH_SHORT).show();
            Log.e("Fatal Error: ", "department shouldn't be use as empty.");
            return;
        }
        if(TextUtils.isEmpty(docId.get(pos))) {
            Toast.makeText(context, "内部错误：1001", Toast.LENGTH_SHORT).show();
            Log.e("Fatal Error: ", "docId shouldn't be use as empty.");
            return;
        }

        params.add(new Param("docname", docName));
        params.add(new Param("hosname", CurrentHosInfo.getCurHos()));
        params.add(new Param("offname", CurrentHosInfo.getCurDepartment()));
        params.add(new Param("docId", docId.get(pos)));
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
                            startActivity(new Intent(ShowDoctorsFromFixedHosActivity.this, DoctorDetailInfoActivity.class));
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else if(code == 2001){
                        Toast.makeText(ShowDoctorsFromFixedHosActivity.this,
                                "暂时没有医生", Toast.LENGTH_SHORT)
                                .show();
                    }
            }

            @Override
            public void onFailure(String errorMsg) {

            }

        });
    }
}
