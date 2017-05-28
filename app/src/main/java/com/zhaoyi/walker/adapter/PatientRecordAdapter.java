package com.zhaoyi.walker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoyi.walker.R;
import com.zhaoyi.walker.view.BaseViewHolder;

import java.util.ArrayList;

/**
 * Created by jianjia Liu on 2017/4/7.
 */

public class PatientRecordAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<ArrayList<String>> mList;

    public PatientRecordAdapter(Context mContext, ArrayList<ArrayList<String>> m) {
        super();
        this.mContext = mContext;
        mList = m;
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.listview_item, parent, false);
        }
        ImageView imageView = BaseViewHolder.get(convertView, R.id.iv_icon);
        imageView.setVisibility(ImageView.GONE);
        TextView tvName = BaseViewHolder.get(convertView, R.id.tv_name);
        tvName.setText(mList.get(position).get(1));
        TextView tvDesc = BaseViewHolder.get(convertView, R.id.tv_desc);
        tvDesc.setText(mList.get(position).get(2));

        return convertView;
    }
}
