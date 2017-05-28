package com.zhaoyi.walker.activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jianjia Liu on 2017/4/12.
 */

public class CommentActivity extends FragmentActivity implements TextWatcher {
    private EditText tvComment;
    private RatingBar rbStars;
    private Button btnComment;

    private String order;  //订单号
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acivity_comment);

        tvComment = (EditText) findViewById(R.id.tv_comment);
        rbStars = (RatingBar) findViewById(R.id.rb_comment);
        btnComment = (Button) findViewById(R.id.btn_comment);
        findViewById(R.id.iv_top_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ((TextView) findViewById(R.id.tv_top_title)).setText("评价");
        Bundle args = getIntent().getExtras();
        order = args.getString("order");
        tvComment.addTextChangedListener(this);
        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(order == null) {
                    return;
                }
                float stars = rbStars.getRating();
                if(stars == 0) {
                    Toast.makeText(CommentActivity.this, "请先评分哦", Toast.LENGTH_SHORT).show();
                    return;
                }
                String comment = tvComment.getText().toString().trim();
                Cursor rulesCursor = getCommentRulesCursor();
                String rules = null;
                if(rulesCursor.moveToFirst()) {
                    do {
                        rules = rulesCursor.getString(0);
                        if (comment.contains(rules)) {
                            Toast.makeText(CommentActivity.this, "请规范您的文明用语！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }while(rulesCursor.moveToNext());
                }
                List<Param> params = new ArrayList<>();
                params.add(new Param("orderId", order));
                params.add(new Param("comment", comment ));
                params.add(new Param("star", stars + ""));
                OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.POSTCOMMENT, new
                        OkHttpManager.HttpCallBack() {
                            @Override
                            public void onResponse(JSONObject json) {
                                int code = json.getInteger("code");
                                if(code == 1000) {
                                    Toast.makeText(CommentActivity.this, "评价成功", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            }

                            @Override
                            public void onFailure(String errorMsg) {
                                Toast.makeText(CommentActivity.this, "服务器繁忙，请稍后再试", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        boolean sig = tvComment.length() > 0 ? true : false;
        if(sig) {
            btnComment.setEnabled(true);
            btnComment.setBackgroundColor(getResources().getColor(R.color.theme_default_color));
        }
        else {
            btnComment.setEnabled(false);
            btnComment.setBackgroundColor(getResources().getColor(R.color.abs__bright_foreground_disabled_holo_light));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /**
     * 返回评论规则集
     * @return 游标结果集
     */
    public Cursor getCommentRulesCursor() {

        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(
                CommentActivity.this.getFilesDir() + "/comment_rules.db", null);

        Cursor cursor = null;
        try {
            String querySql = "select rule from tb_comment_rules";
            //String querySql = "drop table tb_comment_rules";

            cursor = db.rawQuery(querySql, null);

        } catch (SQLiteException e) {

            String sql = "create table tb_comment_rules (_id integer primary key autoincrement,rule varchar(20),note varchar(20))";

            db.execSQL(sql);

            String insertSql = "insert into tb_comment_rules values (null,?,null)";

            db.execSQL(insertSql, new Object[] { "不好" });

            db.execSQL(insertSql, new Object[] { "傻逼" });

            db.execSQL(insertSql, new Object[] { "傻B" });

            db.execSQL(insertSql, new Object[] { "傻叉" });

            db.execSQL(insertSql, new Object[] { "他妈" });

            db.execSQL(insertSql, new Object[] { "弱智" });

            db.execSQL(insertSql, new Object[] { "stupid" });

            String querySql = "select rule from tb_comment_rules";

            cursor = db.rawQuery(querySql, null);
        }

        return cursor;
    }
}
