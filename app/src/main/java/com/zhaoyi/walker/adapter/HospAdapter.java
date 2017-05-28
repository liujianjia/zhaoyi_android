package com.zhaoyi.walker.adapter;

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
 * Created by jianjia Liu on 2017/5/2.
 */

public class HospAdapter extends BaseAdapter {
    Context mContent;
    ArrayList<ArrayList<String>> mHosp;

    public HospAdapter(Context context) {
        mContent = context;
        mHosp = new ArrayList<>();
    }
    public HospAdapter(Context context, ArrayList<ArrayList<String>> m) {
        mContent = context;
        mHosp = m;
    }
    @Override
    public int getCount() {
        return mHosp.size();
    }

    @Override
    public Object getItem(int position) {
        return mHosp.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void removeItem(int position) {
        mHosp.remove(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContent).inflate(
                    R.layout.lv_hosp_item, parent, false);
        }
        ImageView hospPic = BaseViewHolder.get(convertView, R.id.iv_hosp_pic);
        Glide.with(mContent)
                .load(mHosp.get(position).get(0))
                .placeholder(R.drawable.zy_default_hospital)
                .into(hospPic);

        TextView hospName = BaseViewHolder.get(convertView, R.id.tv_hosp_name);
        hospName.setText(mHosp.get(position).get(1));

        TextView hospStars = BaseViewHolder.get(convertView, R.id.tv_hosp_star);
        hospStars.setText(mHosp.get(position).get(2));

        TextView hospLevel = BaseViewHolder.get(convertView, R.id.tv_hosp_level);
        hospLevel.setText(mHosp.get(position).get(3));

        TextView hospType = BaseViewHolder.get(convertView, R.id.tv_hosp_type);
        hospType.setText(mHosp.get(position).get(4));

        TextView hospRegion = BaseViewHolder.get(convertView, R.id.tv_hosp_region);
        hospRegion.setText(mHosp.get(position).get(5));

        TextView hospAppointment = BaseViewHolder.get(convertView, R.id.tv_hosp_appointment);
        hospAppointment.setText(mHosp.get(position).get(6));

        TextView hospDistance = BaseViewHolder.get(convertView, R.id.tv_hosp_distance);
        hospDistance.setText(mHosp.get(position).get(7));

        if(!mHosp.get(position).get(7).equals("暂无")) {
            hospDistance.setText(mHosp.get(position).get(7) + "km");
        }
        else {
            hospDistance.setText(mHosp.get(position).get(7));
        }

        return convertView;
    }
}
