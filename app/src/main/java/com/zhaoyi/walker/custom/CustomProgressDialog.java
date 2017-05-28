package com.zhaoyi.walker.custom;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import com.zhaoyi.walker.R;

/**
 * Created by jianjia Liu on 2017/4/4.
 */

public class CustomProgressDialog extends ProgressDialog {
    private TextView tvText;

    public CustomProgressDialog(Context context)
    {
        super(context);
    }

    public CustomProgressDialog(Context context, int theme)
    {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        init(getContext());
    }

    private void init(Context context)
    {
        //设置不可取消，点击其他区域不能取消，实际中可以抽出去封装供外包设置
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        setContentView(R.layout.custom_progress_dialog);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);

        tvText = (TextView)findViewById(R.id.tv_loading_msg);
    }

    public void setText(String text) {
        tvText.setText(text);
    }

}
