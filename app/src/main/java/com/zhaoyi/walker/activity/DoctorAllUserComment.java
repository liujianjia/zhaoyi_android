package com.zhaoyi.walker.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zhaoyi.walker.R;
import com.zhaoyi.walker.adapter.UserCommentAdapter;

import java.util.ArrayList;

/**
 * Created by jianjia Liu on 2017/5/3.
 */

public class DoctorAllUserComment extends FragmentActivity {
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
        comment = DoctorDetailInfoActivity.getAllComment();

        tvTitle.setText("所有评价");
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        UserCommentAdapter userCommentAdapter = new UserCommentAdapter(DoctorAllUserComment.this, comment);
        lvComment.setAdapter(userCommentAdapter);
    }
}
