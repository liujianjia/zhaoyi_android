package com.zhaoyi.walker.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;
import com.zhaoyi.walker.view.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jianjia Liu on 2017/5/2.
 */

public class MyCommentAdapter extends BaseAdapter {
    private Context mContent;
    private ArrayList<ArrayList<String>> content;

    public MyCommentAdapter(Context context) {
        mContent = context;
        content = new ArrayList<>();
    }

    public MyCommentAdapter(Context context, ArrayList<ArrayList<String>> m) {
        mContent = context;
        content = m;
    }

    @Override
    public int getCount() {
        return content.size();
    }

    @Override
    public Object getItem(int position) {
        return content.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContent).inflate(
                    R.layout.lv_mycomment_item, parent, false);
        }

        TextView tvHospName = BaseViewHolder.get(convertView, R.id.tv_hosp_name);
        tvHospName.setText(content.get(position).get(0));
        TextView tvDocName = BaseViewHolder.get(convertView, R.id.tv_doc_name);
        tvDocName.setText(content.get(position).get(2));
        RatingBar rbStars = BaseViewHolder.get(convertView, R.id.rb_stars);
        rbStars.setRating(Float.parseFloat(content.get(position).get(3)));
        TextView comment = BaseViewHolder.get(convertView, R.id.tv_comment);
        comment.setText(content.get(position).get(4));
        TextView date = BaseViewHolder.get(convertView, R.id.tv_date);
        Button btnDelete = BaseViewHolder.get(convertView, R.id.btn_delete);
        date.setText(content.get(position).get(5));
        RelativeLayout rlHosp = BaseViewHolder.get(convertView, R.id.rl_hosp);
        RelativeLayout rlDoc = BaseViewHolder.get(convertView, R.id.rl_doc);

        /*rlHosp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Param> params = new ArrayList<>();
                String hosp = content.get(position).get(0);
                if(TextUtils.isEmpty(hosp)) {
                    return;
                }

                params.add(new Param("hosname", hosp));

                OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.ENTERDETAILPAGE,
                        new OkHttpManager.HttpCallBack() {
                    @Override
                    public void onResponse(JSONObject json) {
                        int code = json.getInteger("code");

                        if (code == 1000) {
                            Map<String, Object> map = json.getObject("result", Map.class);

                            JSONArray hi = (JSONArray) map.get("HosInfo");
                            ResultFromServer.initHosInfo();
                            for(int i = 0; i < hi.size(); i++) {
                                Map<String, Object> n = hi.getObject(i, Map.class);
                                ResultFromServer.addHosInfo(n);
                            }
                            try {
                                mContent.startActivity(new Intent(mContent, HosDetailInfoActivity.class));
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            Toast.makeText(mContent,
                                    "服务器繁忙请重试...", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {

                    }

                });
            }
        });
        rlDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Param> params = new ArrayList<>();
                String docName = content.get(position).get(2);
                String hosp = content.get(position).get(0);
                String dep = content.get(position).get(1);
                if(TextUtils.isEmpty(docName) || TextUtils.isEmpty(hosp) || TextUtils.isEmpty(dep)) {
                    return;
                }
                params.add(new Param("docname", docName));
                params.add(new Param("hosname", hosp));
                params.add(new Param("offname", dep));
                OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.ENTERDETAILDOCTORPAGE,
                        new OkHttpManager.HttpCallBack() {
                    @Override
                    public void onResponse(JSONObject json) {
                        int code = json.getInteger("code");
                        if (code == 1000) {
                            try {
                                Map<String, Object> map = json.getObject("result", Map.class);

                                JSONArray hi = (JSONArray) map.get("DocInfo");
                                ResultFromServer.initDoctor();
                                for(int i = 0; i < hi.size(); i++) {
                                    Map<String, Object> n = hi.getObject(i, Map.class);
                                    ResultFromServer.getDoctor().add(n);
                                }
                                mContent.startActivity(new Intent(mContent, DoctorDetailInfoActivity.class));
                                //finish();
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            Toast.makeText(mContent,
                                    "服务器繁忙请重试...", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {

                    }

                });
            }
        });*/

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = content.get(position).get(6);

                if(TextUtils.isEmpty(id)) {
                    Log.e("Error: ", "id can't be empty.");
                    return;
                }

                List<Param> params = new ArrayList<>();
                params.add(new Param("id", id));

                OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.DELETECOMMENT,
                        new OkHttpManager.HttpCallBack() {
                            @Override
                            public void onResponse(JSONObject json) {
                                int code = json.getInteger("code");
                                if(code == 1000) {
                                    Toast.makeText(mContent, "删除成功", Toast.LENGTH_SHORT).show();
                                    content.remove(position);
                                    MyCommentAdapter.this.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onFailure(String errorMsg) {

                            }
                        });
            }
        });
        return convertView;
    }
}
