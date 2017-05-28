package com.zhaoyi.walker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.ZyApplication;
import com.zhaoyi.walker.adapter.MyCommentAdapter;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jianjia Liu on 2017/5/3.
 */

public class MyCommentActivity extends FragmentActivity {
    private ImageView ivBack;
    private TextView tvTitle;
    private ListView lvComment;
    private ArrayList<ArrayList<String>> comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_all_user_comment);
        ivBack = (ImageView) findViewById(R.id.iv_top_back);
        tvTitle = (TextView) findViewById(R.id.tv_top_title);
        lvComment = (ListView) findViewById(R.id.lv_comment);

        tvTitle.setText("我的评价");
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        comment = new ArrayList<>();

        getComment();
    }
    private void getComment() {
        String isLogin = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.JSON_KEY_ISLOGIN);
        if(TextUtils.isEmpty(isLogin) || !isLogin.equals("1")) {
            startActivity(new Intent(MyCommentActivity.this, LoginActivity.class));
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

        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.GETMYCOMMENT,
                new OkHttpManager.HttpCallBack() {
                    @Override
                    public void onResponse(JSONObject json) {
                        int code = json.getInteger("code");
                        if(code == 1000){
                            JSONObject obj = (JSONObject)json.get("result");
                            JSONArray c = (JSONArray)obj.get("AssessInfo");
                            int size = c.size();
                            for(int i = 0; i < size; i++) {
                                Map<String, Object> iComment = (Map<String, Object>)c.get(i);
                                ArrayList<String> iList = new ArrayList<>();
                                iList.add((String)iComment.get("hosName"));
                                iList.add((String)iComment.get("offName"));
                                iList.add((String)iComment.get("docName"));
                                iList.add((String)iComment.get("star"));
                                iList.add((String)iComment.get("comment"));
                                iList.add((String)iComment.get("time"));
                                iList.add((String)iComment.get("id"));
                                comment.add(iList);
                            }
                            MyCommentAdapter userCommentAdapter = new MyCommentAdapter(MyCommentActivity.this, comment);
                            lvComment.setAdapter(userCommentAdapter);
                        } else if(code == 2001) {
                            Toast.makeText(MyCommentActivity.this, "当前没有评价哦", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {

                    }
                });
    }
}
