package com.zhaoyi.walker.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhaoyi.walker.R;
import com.zhaoyi.walker.view.BaseViewHolder;

import java.util.List;

/**
 * @Description:gridview的Adapter
 */
public class FrameGridAdapter extends BaseAdapter {
	private Context mContext;
	private List<String> items;
	private ColorStateList csl;

	/*public int[] imgs = { R.drawable.app_transfer, R.drawable.app_fund,
			R.drawable.app_phonecharge, R.drawable.app_creditcard,
			R.drawable.app_movie, R.drawable.app_lottery,
			R.drawable.app_facepay, R.drawable.app_close, R.drawable.app_plane };*/

	public FrameGridAdapter(Context mContext, List<String> list, ColorStateList csl) {
		super();
		this.mContext = mContext;
		items = list;
		this.csl = csl;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		//return img_text.length;
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	/* 返回需要的view */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.gridview_item, parent, false);
		}
		TextView tv = BaseViewHolder.get(convertView, R.id.text_item);
		//ImageView iv = BaseViewHolder.get(convertView, R.id.iv_imageItem);
		//iv.setBackgroundResource(imgs[position]);

		tv.setText(items.get(position));

		if(items.get(position).equals("预约")) {
			tv.setTextColor(csl);
		}

		return convertView;
	}
}
