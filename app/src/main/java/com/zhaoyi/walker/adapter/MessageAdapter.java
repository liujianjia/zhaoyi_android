package com.zhaoyi.walker.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.view.BaseViewHolder;

import java.util.ArrayList;

/**
 * Created by jianjia Liu on 2017/4/7.
 */

public class MessageAdapter extends BaseAdapter {
    private Context mContext;
    ArrayList<ArrayList<String>> msg;

    public MessageAdapter(Context mContext, ArrayList<ArrayList<String>> m) {
        super();
        this.mContext = mContext;
        msg = m;
    }
    @Override
    public int getCount() {
        return msg.size();
    }

    @Override
    public Object getItem(int position) {
        return msg.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.listview_item, parent, false);
        }
        ImageView imageView = BaseViewHolder.get(convertView, R.id.iv_icon);
        Glide.with(mContext).load(msg.get(position).get(0)).into(imageView);
        TextView tvTitle = BaseViewHolder.get(convertView, R.id.tv_name);
        tvTitle.setText(msg.get(position).get(1));
        TextView tvDesc = BaseViewHolder.get(convertView, R.id.tv_desc);
        tvDesc.setText(msg.get(position).get(2));

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(msg.get(position).get(1))
                        .setMessage(msg.get(position).get(3))
                        .setPositiveButton("确定", null)
                        .show();
            }
        });

        return convertView;
    }
}
