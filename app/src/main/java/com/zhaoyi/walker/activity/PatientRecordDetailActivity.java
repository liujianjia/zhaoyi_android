package com.zhaoyi.walker.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.ZyApplication;
import com.zhaoyi.walker.custom.CustomProgressDialog;
import com.zhaoyi.walker.model.CurrentHosInfo;
import com.zhaoyi.walker.model.ResultFromServer;
import com.zhaoyi.walker.utils.GeoCoderDecoder;
import com.zhaoyi.walker.utils.GeoType;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jianjia Liu on 2017/4/10.
 */

public class PatientRecordDetailActivity extends FragmentActivity {
    private Context context;
    private ArrayList<String> mList;
    private TextView tvOrderNumber;
    private TextView tvOrderStatus;
    private ImageView ivAvatar;
    private TextView tvDocName;
    private TextView tvHosp;
    private TextView tvDep;
    private TextView tvUserName;
    private TextView tvCreateTime;
    private TextView tvTime;
    private TextView tvCost;
    private TextView tvPayStatus;
    private TextView tvUserNumber;
    private TextView tvAddress;
    private ImageView ivLocation;
    private Button btnDelete;
    private Button btnComment;
    RelativeLayout rlDoc;

    private final int TYPE_REQUEST_COMMENT = 109;
    private final int RESULT_CANCEL_ORDER_OK = 110;
    private final int RESULT_DELETER_ORDER_OK = 111;
    private final int RESULT_COMMENT_OK = 112;
    String order; //订单号

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_detail);

        context = PatientRecordDetailActivity.this;

        tvOrderNumber = (TextView) findViewById(R.id.tv_order_number);
        tvOrderStatus = (TextView) findViewById(R.id.tv_order_status);
        ivAvatar = (ImageView) findViewById(R.id.iv_avatar);
        tvDocName = (TextView) findViewById(R.id.tv_doc);
        tvHosp = (TextView) findViewById(R.id.tv_hosp);
        tvDep = (TextView) findViewById(R.id.tv_dep);
        tvUserName = (TextView) findViewById(R.id.tv_user);
        tvCreateTime = (TextView) findViewById(R.id.tv_create_time);
        tvTime = (TextView) findViewById(R.id.tv_time);
        tvCost = (TextView) findViewById(R.id.tv_cost);
        tvPayStatus = (TextView) findViewById(R.id.tv_pay_status);
        tvUserNumber = (TextView) findViewById(R.id.tv_user_number);
        tvAddress = (TextView) findViewById(R.id.tv_hos_address);
        ivLocation = (ImageView) findViewById(R.id.iv_hos_location);
        btnDelete = (Button) findViewById(R.id.btn_comment);
        btnComment = (Button) findViewById(R.id.btn_delete);
        rlDoc = (RelativeLayout) findViewById(R.id.rl_doc);

        findViewById(R.id.iv_top_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.btn_cancel).setVisibility(Button.GONE);
        findViewById(R.id.iv_right).setVisibility(ImageView.VISIBLE);


        Bundle args = getIntent().getExtras();
        int type = args.getInt("type");
        switch (type) {
            case 0:  // 预约详情
                //btnDelete.setText("删除记录");
                ((TextView)findViewById(R.id.tv_top_title)).setText("预约详情");
                mList = args.getStringArrayList("data");
                setUpOrder();
                onCancelOrderOrDeleteRecord();
                break;
            case 1:  // 就诊记录
                ((TextView)findViewById(R.id.tv_top_title)).setText("就诊记录");
                btnComment.setText("去评价");
                btnDelete.setVisibility(Button.VISIBLE);
                //findViewById(R.id.rl_attention).setVisibility(RelativeLayout.INVISIBLE);
                //onCancelOrderOrDeleteRecord(1);
                order = args.getString("data");
                getOrder();
                onDeleteRecord();
                break;
        }

        rlDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Param> params = new ArrayList<>();
                String docName = mList.get(3);
                String hosp = mList.get(4);
                String dep = mList.get(5);
                String docId = mList.get(13);
                String hospAddress = mList.get(12);

                if(TextUtils.isEmpty(docName)) {
                    Toast.makeText(context, "内部错误：1001", Toast.LENGTH_SHORT).show();
                    Log.e("Fatal Error: ", "docName shouldn't be use as empty.");
                    return;
                }
                if(TextUtils.isEmpty(hosp)) {
                    Toast.makeText(context, "内部错误：1001", Toast.LENGTH_SHORT).show();
                    Log.e("Fatal Error: ", "hosName shouldn't be use as empty.");
                    return;
                }
                if(TextUtils.isEmpty(dep)) {
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
                if(TextUtils.isEmpty(hospAddress)) {
                    Toast.makeText(context, "内部错误：1001", Toast.LENGTH_SHORT).show();
                    Log.e("Fatal Error: ", "hospAddress shouldn't be use as empty.");
                    return;
                }

                String userId = ZyApplication.getInstance()
                        .getUserJson()
                        .getString(MyContants.JSON_KEY_ZYID);

                if(TextUtils.isEmpty(userId)) {
                    userId = "";
                }

                CurrentHosInfo.setCurHos(hosp);
                CurrentHosInfo.setCurDepartment(dep);
                CurrentHosInfo.setCurHosAddress(hospAddress);

                params.add(new Param("docname", docName));
                params.add(new Param("hosname", hosp));
                params.add(new Param("offname", dep));
                params.add(new Param("userId", userId));
                params.add(new Param("docId", docId));

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
                                startActivity(new Intent(PatientRecordDetailActivity.this, DoctorDetailInfoActivity.class));
                                //finish();
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            Toast.makeText(PatientRecordDetailActivity.this,
                                    "服务器繁忙请重试...", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {

                    }

                });
            }
        });
        //getOrder();
    }
    private void getOrder() {
        //final Bundle args = getIntent().getExtras();
        //order = args.getString("order");
        List<Param> params = new ArrayList<>();
        if(order == null) {
            return;
        }
        params.add(new Param("orderId", order));
        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.GETRECORDDETAIL,
                new OkHttpManager.HttpCallBack() {
                    @Override
                    public void onResponse(JSONObject json) {
                        int code = json.getInteger("code");
                        if(code == 1000) {
                            Map<String, Object> map = json.getObject("result", Map.class);
                            JSONArray array = (JSONArray) map.get("RecordInfo");
                            //int size = array.size();
                            mList = new ArrayList<>();
                            Map<String, String> m = array.getObject(0, Map.class);

                            mList.add(m.get("orderid"));
                            mList.add(m.get("done"));
                            mList.add(MyContants.BASE_URL + m.get("image"));
                            mList.add(m.get("docName"));
                            mList.add(m.get("hosName"));
                            mList.add(m.get("offName"));
                            mList.add(m.get("realName"));
                            mList.add(m.get("time"));
                            mList.add(m.get("visitTime"));
                            mList.add(m.get("hosCost"));
                            mList.add(m.get("pay"));
                            mList.add(m.get("number"));
                            mList.add(m.get("hosAddress"));
                            mList.add(m.get("docId"));
                            mList.add(m.get("title"));

                            setUpOrder();

                            if(mList.get(1).equals("待评价")) {
                                btnComment.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(PatientRecordDetailActivity.this, CommentActivity.class);
                                        intent.putExtra("order", order);
                                        PatientRecordDetailActivity.this.startActivityForResult(intent, TYPE_REQUEST_COMMENT);
                                    }
                                });
                            }
                            else {
                                btnComment.setEnabled(false);
                                btnComment.setText("已评价");
                                //btnComment.setBackgroundColor(0xd3d3d3);
                            }
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        Toast.makeText(PatientRecordDetailActivity.this, "拉取记录失败了", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void setUpOrder() {
        order = mList.get(0);
        tvOrderNumber.setText(mList.get(0));
        tvOrderStatus.setText(mList.get(1));
        //ivAvatar.setText(mList.get(2));
        Glide.with(PatientRecordDetailActivity.this)
                .load(mList.get(2))
                .into(ivAvatar);
        tvDocName.setText(mList.get(3));
        tvHosp.setText(mList.get(4));
        tvDep.setText(mList.get(5));
        tvUserName.setText(mList.get(6));
        tvCreateTime.setText(mList.get(7));
        tvTime.setText(mList.get(8));
        tvCost.setText(mList.get(9));
        tvPayStatus.setText(mList.get(10));
        tvUserNumber.setText(mList.get(11));
        tvAddress.setText(mList.get(12));
        findViewById(R.id.rl_hosp_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object[] obj = { mList.get(12) };
                CustomProgressDialog pg = new CustomProgressDialog(PatientRecordDetailActivity.this);
                GeoCoderDecoder.init(PatientRecordDetailActivity.this);
                GeoCoderDecoder.getInstance().getLocation(obj, GeoType.Code, pg);
                pg.show();
            }
        });
    }

    void onDeleteRecord() {

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(order == null) {
                    return;
                }
                List<Param> params = new ArrayList<>();
                params.add(new Param("order", order));
                OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.DELETERECORD,
                        new OkHttpManager.HttpCallBack() {
                            @Override
                            public void onResponse(JSONObject json) {
                                int code = json.getInteger("code");
                                if(code == 1000) {
                                    Toast.makeText(PatientRecordDetailActivity.this, "已删除", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_DELETER_ORDER_OK);
                                    finish();
                                }
                                else {
                                    Toast.makeText(PatientRecordDetailActivity.this, "内部错误 code: " + code, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(String errorMsg) {
                                Toast.makeText(PatientRecordDetailActivity.this, "未知错误，请稍后重试",Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    void onCancelOrderOrDeleteRecord() {
        if(mList.get(1).equals("已取消") || mList.get(1).equals("已超时")) {
            btnComment.setText("删除记录");
        }

        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder confirmCancelDlg= new AlertDialog.Builder(PatientRecordDetailActivity.this);
                String msg = mList.get(1).equals("已取消") ? "删除" : "取消";
                confirmCancelDlg.setTitle("提示")
                        .setMessage("确定要" + msg + "吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(order == null) {
                                    return;
                                }
                                String extUrl = mList.get(1).equals("已预约") ?
                                        MyContants.CANCELORDER : MyContants.DELETERECORD;

                                List<Param> params = new ArrayList<>();
                                params.add(new Param("order", order));
                                OkHttpManager.getInstance().post(params, MyContants.BASE_URL + extUrl,
                                        new OkHttpManager.HttpCallBack() {
                                    @Override
                                    public void onResponse(JSONObject json) {
                                        int code = json.getInteger("code");
                                        if(code == 1000) {
                                            if(mList.get(1).equals("已预约")) {
                                                Toast.makeText(PatientRecordDetailActivity.this, "预约取消成功", Toast.LENGTH_SHORT).show();
                                                tvOrderStatus.setText("已取消");
                                                mList.set(1, "已取消");
                                                onCancelOrderOrDeleteRecord();
                                                setResult(RESULT_CANCEL_ORDER_OK);
                                            }
                                            else {
                                                Toast.makeText(PatientRecordDetailActivity.this, "已删除", Toast.LENGTH_SHORT).show();
                                                setResult(RESULT_DELETER_ORDER_OK);
                                                finish();
                                            }

                                        }
                                        else if(code == 2000) {
                                            String text = (String)json.get("result");
                                            if(!TextUtils.isEmpty(text)) {
                                                Toast.makeText(PatientRecordDetailActivity.this, text, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        else {
                                            Toast.makeText(PatientRecordDetailActivity.this, "内部错误 code: " + code, Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(String errorMsg) {
                                        Toast.makeText(PatientRecordDetailActivity.this, "未知错误，请稍后重试",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("不", null)
                        .show();

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if(requestCode == TYPE_REQUEST_COMMENT) {
                mList.set(1, "已评价");
                tvOrderStatus.setText(mList.get(1));
                btnComment.setText(mList.get(1));
                btnComment.setEnabled(false);
                setResult(RESULT_COMMENT_OK);
            }
        }
    }
}
