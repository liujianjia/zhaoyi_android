package com.zhaoyi.walker.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.zhaoyi.walker.R;

/**
 * Created by jianjia Liu on 2017/4/7.
 */

public class AboutActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        String htmlFormat = "<head>找医</head><body><p>找医app，专注于解决看病难、挂号难的问题。</p>" +
                "<p>作者：邱敏航、蒋琦、刘建嘉" +
                "<p>联系作者：</p>" +
                "<p>gxzpljj@163.com</p>" +
                "<p>version 1.0.0</p></body>";
        ((TextView)findViewById(R.id.tv_about)).setText(Html.fromHtml(htmlFormat));
        ((TextView)findViewById(R.id.tv_top_title)).setText("关于");
        findViewById(R.id.iv_top_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
