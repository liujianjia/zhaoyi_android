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

import java.util.List;

/**
 * Created by jianjia Liu on 2017/4/7.
 */

public class ListViewAdapter extends BaseAdapter {
    private Context mContext;
    List<String> url;
    List<String> name;
    List<String> desc;

    public ListViewAdapter(Context mContext, List<String> url, List<String> name, List<String> desc) {
        super();
        this.mContext = mContext;
        this.url = url;
        this.name = name;
        this.desc = desc;
    }
    @Override
    public int getCount() {
        return name.size();
    }

    @Override
    public Object getItem(int position) {
        return name.get(position);
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
        Glide.with(mContext)
                .load(url.get(position))
                .into(imageView);
        TextView tvName = BaseViewHolder.get(convertView, R.id.tv_name);
        tvName.setText(name.get(position));
        TextView tvDesc = BaseViewHolder.get(convertView, R.id.tv_desc);
        tvDesc.setText(desc.get(position));

        return convertView;
    }
}
