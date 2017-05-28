package com.zhaoyi.walker.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.activity.ModifyPatientActivity;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;
import com.zhaoyi.walker.view.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jianjia Liu on 2017/5/4.
 */

public class PatientManagerAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<ArrayList<String>> mList;
    private final int TYPE_REQUEST_ITEM_STATUS = 114;

    public PatientManagerAdapter(Context context) {
        mContext = context;
        mList = new ArrayList<>();
    }
    public PatientManagerAdapter(Context context, ArrayList<ArrayList<String>> m) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.lv_patient_manager, parent, false);
        }

        TextView tvName = BaseViewHolder.get(convertView, R.id.tv_name);
        tvName.setText(mList.get(position).get(1));
        TextView tvDefault = BaseViewHolder.get(convertView, R.id.tv_default);
        if(mList.get(position).get(8).equals("1")) {  // 默认就诊人
            tvDefault.setText("[默认]");
        }
        else {
            tvDefault.setText("");
        }
        TextView tvType = BaseViewHolder.get(convertView, R.id.tv_type);
        tvType.setText(mList.get(position).get(2));
        TextView tvPhone = BaseViewHolder.get(convertView, R.id.tv_phone);
        tvPhone.setText(mList.get(position).get(5));
        ImageButton ibModify = BaseViewHolder.get(convertView, R.id.ib_modify);
        ImageButton ibDelete = BaseViewHolder.get(convertView, R.id.ib_delete);
        ibModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ModifyPatientActivity.class);
                intent.putExtra("data", mList.get(position));
                if(mContext instanceof Activity) {
                    ((Activity)mContext).startActivityForResult(intent, TYPE_REQUEST_ITEM_STATUS);
                } else {
                    Log.e("Error: ", "mContext must an instance of activity.");
                }
            }
        });
        ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mList.get(position).get(8).equals("1")) {
                    Toast.makeText(mContext, "不能删除默认就诊人", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mList.get(position).get(9).equals("1")) {
                    Toast.makeText(mContext, "不能删除用户自身", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Param> params = new ArrayList<>();

                if(TextUtils.isEmpty(mList.get(position).get(0))) {
                    return;
                }
                params.add(new Param("id", mList.get(position).get(0)));
                OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.DELPATIENTIFNO,
                        new OkHttpManager.HttpCallBack() {
                            @Override
                            public void onResponse(JSONObject json) {
                                int code = json.getInteger("code");
                                if(code == 1000) {
                                    Toast.makeText(mContext, "删除成功", Toast.LENGTH_SHORT).show();
                                    mList.remove(position);
                                    PatientManagerAdapter.this.notifyDataSetChanged();

                                    if(mContext instanceof Activity) {
                                        ((TextView)((Activity) mContext).findViewById(R.id.tv_desc))
                                                .setText("（已添加" + mList.size() + "人，还可以添加"
                                                        + (5 - mList.size()) + "人）");  // 最大就诊人数量为5
                                    } else {
                                        Log.e("Error: ", "mContext must an instance of activity.");
                                    }
                                }
                            }

                            @Override
                            public void onFailure(String errorMsg) {

                            }
                        });
            }
        });
        //if(mList.get(position).get(9).equals("1") || mList.get(position).get(8).equals("1")) {
            //ibDelete.setEnabled(false);
        //}

        return convertView;
    }
}
