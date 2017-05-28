package com.zhaoyi.walker.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.zhaoyi.walker.Fragment.DoctorDutyFragment;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.ZyApplication;
import com.zhaoyi.walker.adapter.UserCommentAdapter;
import com.zhaoyi.walker.custom.ListViewForScrollView;
import com.zhaoyi.walker.model.CurrentHosInfo;
import com.zhaoyi.walker.model.ResultFromServer;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.zhaoyi.walker.utils.OkHttpManager.context;

/**
 * Created by jianjia Liu on 2017/3/29.
 */

public class DoctorDetailInfoActivity extends FragmentActivity {
    private FragmentManager mFragmentManager;
    private DoctorDutyFragment dFragment;
    private ImageView ivDoctorPhoto;
    private TextView tvDoctorName;
    private TextView tvDoctorTitle;
    private TextView tvDoctorSimpleInfo;
    private TextView tvDoctorGoodAt;
    private TextView tvStars;
    private TextView tvAppointment;
    private RadioButton rbGetNumber;
    private RadioButton rbAddNumber;
    private ListViewForScrollView lvUserComment;
    private ImageView ivTopBack;
    private TextView tvTopTile;
    private TextView tvCommentCount;
    private Button btnAllComment;
    private Button btnCollection;
    private int lastIdx;
    private String commentCount;
    private ArrayList<ArrayList<String>> comment;
    private String isCollection;
    private String docId;
    private static ArrayList<ArrayList<String>> allComment;

    private final int TYPE_REQUEST_APPOINTMENT = 109;
    private final int RESULT_CANCEL_COLLECTION_OK = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_detail_info);
        mFragmentManager = getSupportFragmentManager();

        ivTopBack = (ImageView)findViewById(R.id.iv_top_back);
        tvTopTile = (TextView)findViewById(R.id.tv_top_title);
        tvDoctorName = (TextView)findViewById(R.id.tv_doctor_name);
        tvDoctorTitle = (TextView)findViewById(R.id.tv_doctor_title);
        tvDoctorSimpleInfo = (TextView)findViewById(R.id.tv_doctor_simple_info);
        tvDoctorGoodAt = (TextView)findViewById(R.id.tv_doctor_goodat);
        rbGetNumber = (RadioButton)findViewById(R.id.rb_get_number);
        rbAddNumber = (RadioButton)findViewById(R.id.rb_add_number);
        lvUserComment = (ListViewForScrollView)findViewById(R.id.lv_user_comment);
        ivDoctorPhoto = (ImageView) findViewById(R.id.ci_doctor_photo);
        btnAllComment = (Button) findViewById(R.id.btn_all_comment);
        tvCommentCount = (TextView) findViewById(R.id.tv_comment_count);
        btnCollection = (Button) findViewById(R.id.btn_collection);
        tvStars = (TextView) findViewById(R.id.tv_star);
        tvAppointment = (TextView) findViewById(R.id.tv_appointment);

        Map<String, Object> result = ResultFromServer.getDoctor().get(0);
        JSONArray commentList = (JSONArray) result.get("assess");  // 评价
        comment = new ArrayList<>();
        allComment = new ArrayList<>();
        int size = commentList.size();
        commentCount = (String)result.get("ass_count");

        ((ScrollView) findViewById(R.id.sv_doctor_comment)).smoothScrollTo(0, 0);  // 移动到顶端，嵌套ListView使用

        for(int i = 0; i < size; i++) {
            Map<String, Object> iComment = (Map<String, Object>)commentList.get(i);
            ArrayList<String> iList = new ArrayList<>();
            iList.add(MyContants.BASE_URL + iComment.get("image"));
            iList.add((String)iComment.get("userName"));
            iList.add((String)iComment.get("star"));
            iList.add((String)iComment.get("time"));
            iList.add((String)iComment.get("comment"));
            allComment.add(iList);
            if(i < 3) {
                comment.add(iList);
            }
        }
        Glide.with(DoctorDetailInfoActivity.this)
                .load(MyContants.BASE_URL + result.get("image"))
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.zy_default_useravatar)
                .into(new BitmapImageViewTarget(ivDoctorPhoto) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        ivDoctorPhoto.setImageDrawable(circularBitmapDrawable);
                    }
                });

        tvDoctorName.setText((String) result.get("docname"));
        DecimalFormat df = new DecimalFormat("0.0");
        tvDoctorTitle.setText((String) result.get("title"));
        tvStars.setText(df.format(result.get("score")));
        tvAppointment.setText((String) result.get("reg_count"));

        isCollection = (String) result.get("is_store");
        docId = (String) result.get("id");
        startDoctorDutyFragment();

        CurrentHosInfo.setCurDoctor((String)tvDoctorName.getText());
        CurrentHosInfo.setCurDoctorRole((String)result.get("title"));
        CurrentHosInfo.setCurCost((String)result.get("cost"));
        tvTopTile.setText("医生主页");

        ivTopBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tvDoctorSimpleInfo.setText((String) result.get("introduction"));
        tvDoctorGoodAt.setText((String) result.get("good"));
        btnAllComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DoctorDetailInfoActivity.this, DoctorAllUserComment.class);
                //intent.putExtra("allComment", AllComment);
                startActivity(intent);
            }
        });

        if(!TextUtils.isEmpty(isCollection) && isCollection.equals("1")) {
            btnCollection.setText("取消收藏");
        }
        btnCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectionOrCancel();
            }
        });
        tvCommentCount.setText("(" + commentCount + ")");

        UserCommentAdapter userCommentAdapter = new UserCommentAdapter(DoctorDetailInfoActivity.this, comment);
        lvUserComment.setAdapter(userCommentAdapter);
    }

    private void collectionOrCancel() {
        if(TextUtils.isEmpty(isCollection)) {
            Log.e("Error: ", "isCollection can't be empty.");
            return;
        }
        String addictionUrl = isCollection.equals("1") ?
                MyContants.CANCELCOLLECTIONDOCTOR : MyContants.COLLECTIONDOCTIOR;
        List<Param> params = new ArrayList<>();
        String userId = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.JSON_KEY_ZYID);

        if(TextUtils.isEmpty(userId)) {
            Toast.makeText(DoctorDetailInfoActivity.this, "请先登陆", Toast.LENGTH_SHORT).show();
            Log.e("Error: ", "userId can't be empty.");
            return;
        }
        if(TextUtils.isEmpty(docId)) {
            Log.e("Error: ", "docId can't be empty.");
            return;
        }

        String isLogin = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.JSON_KEY_ISLOGIN);

        if(TextUtils.isEmpty(isLogin) || !isLogin.equals("1")) {
            Toast.makeText(DoctorDetailInfoActivity.this, "请先登陆", Toast.LENGTH_SHORT).show();
            Log.i("Info: ", "user not login.");
            return;
        }

        params.add(new Param("userId", userId));
        params.add(new Param("docId", docId));

        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + addictionUrl,
                new OkHttpManager.HttpCallBack() {
                    @Override
                    public void onResponse(JSONObject json) {
                        int code = json.getInteger("code");
                        if(code == 1000) {
                            if(isCollection.equals("1")) {
                                Toast.makeText(DoctorDetailInfoActivity.this,
                                        "取消收藏成功", Toast.LENGTH_SHORT).show();
                                btnCollection.setText("收藏");
                                isCollection = "0";
                                setResult(RESULT_CANCEL_COLLECTION_OK);
                            }
                            else if(isCollection.equals("0")) {
                                Toast.makeText(DoctorDetailInfoActivity.this,
                                        "收藏成功", Toast.LENGTH_SHORT).show();
                                btnCollection.setText("取消收藏");
                                isCollection = "1";
                                setResult(RESULT_OK);
                            }
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {

                    }
                });
    }

    private void startDoctorDutyFragment() {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        dFragment = new DoctorDutyFragment();
        ft.add(R.id.ll_doctor_duty_container, dFragment, "DoctorDutyFragment");
        ft.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == TYPE_REQUEST_APPOINTMENT) {
            /* 刷新挂号表 */
            refreshData();
        }
    }


    private void refreshDocFragmentData() {
        dFragment.parseAppointmentData();
    }

    /**
     * 挂号成功后刷新挂号资源
     */
    private void refreshData() {
        List<Param> params = new ArrayList<>();

        String userId = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.JSON_KEY_ZYID);
        if(TextUtils.isEmpty(userId)) {
            Toast.makeText(context, "内部错误：1001", Toast.LENGTH_SHORT).show();
            Log.e("Fatal Error: ", "userId shouldn't be use as empty.");
            return;
        }
        if(TextUtils.isEmpty(docId)) {
            Toast.makeText(context, "内部错误：1001", Toast.LENGTH_SHORT).show();
            Log.e("Fatal Error: ", "docId shouldn't be use as empty.");
            return;
        }
        params.add(new Param("docname", CurrentHosInfo.getCurDoctor()));
        params.add(new Param("hosname", CurrentHosInfo.getCurHos()));
        params.add(new Param("offname", CurrentHosInfo.getCurDepartment()));
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
                        for (int i = 0; i < hi.size(); i++) {  // size = 1
                            Map<String, Object> n = hi.getObject(i, Map.class);
                            ResultFromServer.getDoctor().add(n);
                        }
                        //refreshDoctorDutyFragment();
                        refreshDocFragmentData();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    Toast.makeText(DoctorDetailInfoActivity.this,
                            "未知错误", Toast.LENGTH_SHORT)
                            .show();
                }

            }

            @Override
            public void onFailure(String errorMsg) {

            }
        });
    }

    public static ArrayList<ArrayList<String>> getAllComment() {
        return allComment;
    }
}
