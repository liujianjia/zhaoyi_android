package com.zhaoyi.walker.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.ZyApplication;
import com.zhaoyi.walker.adapter.MyRecycleViewAdapter;
import com.zhaoyi.walker.custom.MyItemClickListener;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016-04-20.
 */
public class MyAppointmentActivity extends AppCompatActivity {
    private Context context;
    private RecyclerView myRecyclerView;
    private ProgressBar progressBar;
    private TextView tvText;
    private TextView tvTitle;

    private MyRecycleViewAdapter mAdapter;
    private ArrayList<ArrayList<String>> mData; // 订单数据

    private static final int TYPE_REQUEST_ORDER_STATUS = 107;  // 请求订单状态
    private final int RESULT_CANCEL_ORDER_OK = 110;  // 取消订单成功
    private final int RESULT_DELETER_ORDER_OK = 111;  // 删除订单成功

    private int lastEntryPos = 0;  // 最后一次选择的位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myappointment);
        //ButterKnife.bind(this);
        context = MyAppointmentActivity.this;

        myRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tvText = (TextView) findViewById(R.id.tv_text);
        findViewById(R.id.iv_top_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvTitle = (TextView) findViewById(R.id.tv_top_title);
        tvTitle.setText("我的预约");

        requestOrder();
    }

    private void initView() {
        myRecyclerView.setHasFixedSize(true);
        /**
         * 设置RecycleView适配器
         */
        mAdapter = new MyRecycleViewAdapter(MyAppointmentActivity.this, mData);
        myRecyclerView.setAdapter(mAdapter);
        LinearLayoutManager manager = new LinearLayoutManager(MyAppointmentActivity.this);
        myRecyclerView.setLayoutManager(manager);
        mAdapter.setOnItemClickListener(new MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(mData == null) {
                    return;
                }

                lastEntryPos = position;
                Intent intent = new Intent(MyAppointmentActivity.this, PatientRecordDetailActivity.class);
                intent.putExtra("type", 0);
                intent.putStringArrayListExtra("data", mData.get(position));
                MyAppointmentActivity.this.startActivityForResult(intent, TYPE_REQUEST_ORDER_STATUS);
            }
        });
    }

    private void requestOrder() {
        String isLogin = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.JSON_KEY_ISLOGIN);
        if(TextUtils.isEmpty(isLogin) || !isLogin.equals("1")) {
            startActivity(new Intent(MyAppointmentActivity.this, LoginActivity.class));
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
        params.add(new Param("userid", userId));

        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.GETORDER,
                new OkHttpManager.HttpCallBack() {
            @Override
            public void onResponse(JSONObject json) {
                int code = json.getInteger("code");
                if(code == 1000) {
                    Map<String, Object> result = json.getObject("result", Map.class);
                    JSONArray array = (JSONArray)result.get("OrderInfo");
                    int size = array.size();
                    mData = new ArrayList<>();
                    for(int i =  0; i < size; i++) {
                        Map<String, String> m = array.getObject(i, Map.class);

                        ArrayList<String> str = new ArrayList<>();
                        str.add(m.get("orderid"));
                        str.add(m.get("done"));
                        str.add(MyContants.BASE_URL + m.get("image"));
                        str.add(m.get("docName"));
                        str.add(m.get("hosName"));
                        str.add(m.get("offName"));
                        str.add(m.get("realName"));
                        str.add(m.get("time"));
                        str.add(m.get("visitTime"));
                        str.add(m.get("hosCost"));
                        str.add(m.get("pay"));
                        str.add(m.get("number"));
                        str.add(m.get("hosAddress"));
                        str.add(m.get("docId"));
                        str.add(m.get("title"));

                        mData.add(str);
                    }
                    initView();
                }
                else {
                    Toast.makeText(MyAppointmentActivity.this, "当前没有预约", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                Toast.makeText(MyAppointmentActivity.this, "获取预约信息失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TYPE_REQUEST_ORDER_STATUS) {
            if(resultCode == RESULT_DELETER_ORDER_OK) {
                mData.remove(lastEntryPos);
                mAdapter.notifyDataSetChanged();
            }
            else if(resultCode == RESULT_CANCEL_ORDER_OK) {
                mData.get(lastEntryPos).set(1, "已取消");
            }
        }
    }
}

