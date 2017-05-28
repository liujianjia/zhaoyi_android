package com.zhaoyi.walker.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.custom.MyItemClickListener;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016-04-21.
 */
public class MyRecycleViewAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private Context context;
    private ArrayList<ArrayList<String>> mList;
    private LayoutInflater mInflater;
    private View view;
    private MyItemClickListener listener;

    public MyRecycleViewAdapter(Context context, ArrayList<ArrayList<String>> mList) {
        this.context = context;
        this.mList = mList;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemCount() {
        return mList.size();

    }

    @Override
    public MyViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        view = mInflater.inflate(R.layout.recycleview_item, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view, listener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        if(mList == null) {
            return;
        }
        final ArrayList<String> info = mList.get(position);
        holder.tvOrderNumber.setText(info.get(0));
        holder.tvOrderStatus.setText(info.get(1));
        if(info.get(1).equals("已预约")) {
            holder.btnCancel.setVisibility(Button.VISIBLE);
        }
        Glide.with(context)
                .load(info.get(2))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.zy_default_useravatar)
                .centerCrop()
                .into(holder.ivAvatar);
        holder.tvDocName.setText(info.get(3));
        holder.tvHosp.setText(info.get(4));
        holder.tvDep.setText(info.get(5));
        holder.tvUserName.setText(info.get(6));
        holder.tvCreateTime.setText(info.get(7));
        holder.tvTime.setText(info.get(8));
        holder.tvCost.setText(info.get(9));
        holder.tvPayStatus.setText(info.get(10));
        holder.tvUserNumber.setText(info.get(11));
        holder.tvAddress.setText(info.get(12));

        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder confirmCancelDlg= new AlertDialog.Builder(context);
                confirmCancelDlg.setTitle("提示")
                        .setMessage("确定要取消吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                List<Param> params = new ArrayList<>();

                                params.add(new Param("order", info.get(0)));
                                OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.CANCELORDER,
                                        new OkHttpManager.HttpCallBack() {
                                    @Override
                                    public void onResponse(JSONObject json) {
                                        int code = json.getInteger("code");
                                        if(code == 1000) {
                                            Toast.makeText(context, "预约取消成功", Toast.LENGTH_SHORT).show();
                                            holder.tvOrderStatus.setText("已取消");
                                            holder.btnCancel.setVisibility(Button.GONE);
                                            mList.get(position).set(1, "已取消");
                                        }
                                        else if(code == 2000) {
                                            String text = (String)json.get("result");
                                            if(!TextUtils.isEmpty(text)) {
                                                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        else {
                                            Toast.makeText(context, "服务器内部错误", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(String errorMsg) {
                                        Toast.makeText(context, "未知错误，请稍后重试",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("不", null)
                        .show();

            }
        });
    }

    /**
     * 设置Item点击监听
     *
     * @param listener
     */
    public void setOnItemClickListener(MyItemClickListener listener) {
        this.listener = listener;
    }
}

class MyViewHolder extends RecyclerView.ViewHolder {
    ImageView ivAvatar;
    View itemView;
    TextView tvOrderNumber;
    TextView tvOrderStatus;
    TextView tvDocName;
    TextView tvHosp;
    TextView tvDep;
    TextView tvUserName;
    TextView tvCreateTime;
    TextView tvTime;
    TextView tvCost;
    TextView tvPayStatus;
    TextView tvUserNumber;
    TextView tvAddress;
    Button btnCancel;
    MyItemClickListener mListener;

    public MyViewHolder(View itemView, final MyItemClickListener mListener) {
        super(itemView);
        this.itemView = itemView;
        ivAvatar = (ImageView) itemView.findViewById(R.id.iv_avatar);
        tvOrderNumber = (TextView) itemView.findViewById(R.id.tv_order_number);
        tvOrderStatus = (TextView) itemView.findViewById(R.id.tv_order_status);
        tvDocName = (TextView) itemView.findViewById(R.id.tv_doc);
        tvHosp = (TextView) itemView.findViewById(R.id.tv_hosp);
        tvDep = (TextView) itemView.findViewById(R.id.tv_dep);
        tvUserName = (TextView) itemView.findViewById(R.id.tv_user);
        tvCreateTime = (TextView) itemView.findViewById(R.id.tv_create_time);
        tvTime = (TextView) itemView.findViewById(R.id.tv_time);
        tvCost = (TextView) itemView.findViewById(R.id.tv_cost);
        tvPayStatus = (TextView) itemView.findViewById(R.id.tv_pay_status);
        tvUserNumber = (TextView) itemView.findViewById(R.id.tv_user_number);
        tvAddress = (TextView) itemView.findViewById(R.id.tv_hos_address);
        btnCancel = (Button) itemView.findViewById(R.id.btn_cancel);
        btnCancel.setVisibility(Button.GONE);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(v, getAdapterPosition());
            }
        });
    }

}