package com.zhaoyi.walker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.view.BaseViewHolder;

import java.util.ArrayList;

/**
 * Created by jianjia Liu on 2017/5/4.
 */

public class MyCollectionDocsAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<ArrayList<String>> mList;

    public MyCollectionDocsAdapter(Context context) {
        mContext = context;
        mList = new ArrayList<>();
    }
    public MyCollectionDocsAdapter(Context context, ArrayList<ArrayList<String>> m) {
        mContext = context;
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
                    R.layout.lv_common_doctor_item, parent, false);
        }
        final ImageView ivAvatar = BaseViewHolder.get(convertView, R.id.iv_doctor_pic);

        Glide.with(mContext)
                .load(mList.get(position).get(1))
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.zy_default_useravatar)
                .into(ivAvatar);

        TextView tvName = BaseViewHolder.get(convertView, R.id.tv_doctor_name);
        tvName.setText(mList.get(position).get(2));

        TextView tvStars = BaseViewHolder.get(convertView, R.id.tv_stars);
        tvStars.setText(mList.get(position).get(3));

        TextView tvAppointment = BaseViewHolder.get(convertView, R.id.tv_appointment);
        tvAppointment.setText(mList.get(position).get(4));

        TextView tvLevel = BaseViewHolder.get(convertView, R.id.tv_level);
        tvLevel.setText(mList.get(position).get(5));

        TextView tvDep = BaseViewHolder.get(convertView, R.id.tv_dep);
        tvDep.setText(mList.get(position).get(6));

        TextView tvHosp = BaseViewHolder.get(convertView, R.id.tv_hosp);
        tvHosp.setText(mList.get(position).get(7));

        return convertView;
    }
}
