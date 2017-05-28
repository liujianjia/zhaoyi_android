package com.zhaoyi.walker.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.view.BaseViewHolder;

import java.util.ArrayList;

import static com.zhaoyi.walker.utils.OkHttpManager.context;

/**
 * Created by jianjia Liu on 2017/5/2.
 */

public class UserCommentAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<ArrayList<String>> content;

    public UserCommentAdapter(Context context) {
        mContext = context;
        content = new ArrayList<>();
    }

    public UserCommentAdapter(Context context, ArrayList<ArrayList<String>> m) {
        mContext = context;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.lv_comment_item, parent, false);
        }

        final ImageView ivAvatar = BaseViewHolder.get(convertView, R.id.iv_avatar);
        Glide.with(mContext)
                .load(content.get(position).get(0))
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.zy_default_useravatar)
                .into(new BitmapImageViewTarget(ivAvatar) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        ivAvatar.setImageDrawable(circularBitmapDrawable);
                    }
                });

        TextView tvName = BaseViewHolder.get(convertView, R.id.tv_name);
        tvName.setText(content.get(position).get(1));

        RatingBar rbStars = BaseViewHolder.get(convertView, R.id.rb_stars);
        rbStars.setRating(Float.parseFloat(content.get(position).get(2)));

        TextView date = BaseViewHolder.get(convertView, R.id.tv_date);
        date.setText(content.get(position).get(3));

        TextView comment = BaseViewHolder.get(convertView, R.id.tv_comment);
        comment.setText(content.get(position).get(4));

        return convertView;
    }
}
