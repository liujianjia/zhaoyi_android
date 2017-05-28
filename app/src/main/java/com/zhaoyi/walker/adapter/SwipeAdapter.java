package com.zhaoyi.walker.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jianjia Liu on 2017/4/9.
 */

public class SwipeAdapter extends BaseSwipeAdapter {
    private Context mContext;
    private ArrayList<ArrayList<String>> mData;

    public SwipeAdapter(Context mContext, ArrayList<ArrayList<String>> m) {
        this.mContext = mContext;
        mData = m;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.rl_swipe_layout;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.swipe_layout, null);
        SwipeLayout swipeLayout = (SwipeLayout)v.findViewById(getSwipeLayoutResourceId(position));
        swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
                //YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.trash));
            }
        });
        swipeLayout.setOnDoubleClickListener(new SwipeLayout.DoubleClickListener() {
            @Override
            public void onDoubleClick(SwipeLayout layout, boolean surface) {
                //Toast.makeText(mContext, "DoubleClick", Toast.LENGTH_SHORT).show();
            }
        });
        v.findViewById(R.id.iv_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteItem(position);
                //Toast.makeText(mContext, "click delete", Toast.LENGTH_SHORT).show();
            }
        });
        swipeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(mData.get(position).get(1))
                        .setMessage(mData.get(position).get(3))
                        .setPositiveButton("确定", null)
                        .show();
            }
        });

        return v;
    }

    @Override
    public void fillValues(int position, View convertView) {
        //TextView t = (TextView)convertView.findViewById(R.id.position);
        //t.setText((position + 1) + ".");
        ImageView imageView = (ImageView) convertView.findViewById(R.id.iv_item_image);
        TextView tvItemTitle = (TextView) convertView.findViewById(R.id.tv_item_title);
        TextView tvItemDesc = (TextView) convertView.findViewById(R.id.tv_item_desc);
        final List<String> mInfo = mData.get(position);
        Glide.with(mContext)
                .load(mInfo.get(0))
                .into(imageView);
        tvItemTitle.setText(mInfo.get(1));
        tvItemDesc.setText(mInfo.get(2));
    }

    private void deleteItem(final int pos) {
        if(pos >= mData.size()) {
            Log.e("Error: ", "mData index out of range.");
            return;
        }
        List<Param> params = new ArrayList<>();

        params.add(new Param("id", mData.get(pos).get(4)));

        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.DELETENOTIFICATION,
                new OkHttpManager.HttpCallBack() {
                    @Override
                    public void onResponse(JSONObject json) {
                        int code = json.getInteger("code");
                        if(code == 1000) {
                            mData.remove(pos);
                            notifyDataSetChanged();
                        } else if(code == 2001) {
                            Toast.makeText(mContext, "删除失败", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, "未知错误", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {

                    }
                });
    }
    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
