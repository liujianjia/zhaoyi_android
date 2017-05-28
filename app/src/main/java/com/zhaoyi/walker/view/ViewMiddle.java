package com.zhaoyi.walker.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.activity.ShowDoctorsFromFixedHosActivity;
import com.zhaoyi.walker.adapter.TextAdapter;
import com.zhaoyi.walker.model.CurrentHosInfo;
import com.zhaoyi.walker.model.ResultFromServer;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/* 医生科室列表 */
public class ViewMiddle extends LinearLayout implements ViewBaseAction {
	
	private ListView regionListView;
	private ListView plateListView;
	private ArrayList<String> groups = new ArrayList<String>();
	private LinkedList<String> childrenItem = new LinkedList<String>();
	private SparseArray<LinkedList<String>> children = new SparseArray<LinkedList<String>>();
	private TextAdapter plateListViewAdapter;
	private TextAdapter earaListViewAdapter;
	private OnSelectListener mOnSelectListener;
	private int tEaraPosition = 0;
	private int tBlockPosition = 0;
	private String showString = "不限";
	private List<List<String>> mData;
	private Context mContent;

	public ViewMiddle(Context context) {
		super(context);
		mContent = context;
		init(context);
	}

	public ViewMiddle(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContent = context;
		init(context);
	}

	public ViewMiddle(Context context, List<List<String>> data) {
		super(context);
		mContent = context;
		mData = data;
		init(context);
	}

	public void updateShowText(String showArea, String showBlock) {
		if (showArea == null || showBlock == null) {
			return;
		}
		for (int i = 0; i < groups.size(); i++) {
			if (groups.get(i).equals(showArea)) {
				earaListViewAdapter.setSelectedPosition(i);
				childrenItem.clear();
				if (i < children.size()) {
					childrenItem.addAll(children.get(i));
				}
				tEaraPosition = i;
				break;
			}
		}
		for (int j = 0; j < childrenItem.size(); j++) {
			if (childrenItem.get(j).replace("不限", "").equals(showBlock.trim())) {
				plateListViewAdapter.setSelectedPosition(j);
				tBlockPosition = j;
				break;
			}
		}
		setDefaultSelect();
	}

	private void init(final Context context) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_region, this, true);
		regionListView = (ListView) findViewById(R.id.listView);
		plateListView = (ListView) findViewById(R.id.listView2);
		//setBackgroundDrawable(getResources().getDrawable(
				//R.drawable.choosearea_bg_left));
		setBackgroundColor(Color.WHITE);

		/*for(int i=0;i<10;i++){
			groups.add(i+"行");
			LinkedList<String> tItem = new LinkedList<String>();
			for(int j=0;j<15;j++){
				
				tItem.add(i+"行"+j+"列");
				
			}
			children.put(i, tItem);
		}*/
		if(mData == null)
			return;
		int index = 0;
		for(List<String> i : mData) {
			groups.add(i.get(0));
			int size = i.size();
			LinkedList<String> tItem = new LinkedList<String>();
			for(int k = 1; k < size; k++) {
				tItem.add(i.get(k));
			}
			children.put(index, tItem);
			index++;
		}

		earaListViewAdapter = new TextAdapter(context, groups,
				R.drawable.choose_item_selected,
				R.drawable.choose_eara_item_selector);
		earaListViewAdapter.setTextSize(17);
		earaListViewAdapter.setSelectedPositionNoNotify(tEaraPosition);
		regionListView.setAdapter(earaListViewAdapter);
		earaListViewAdapter
				.setOnItemClickListener(new TextAdapter.OnItemClickListener() {

					@Override
					public void onItemClick(View view, int position) {
						if (position < children.size()) {
							childrenItem.clear();
							childrenItem.addAll(children.get(position));
							plateListViewAdapter.notifyDataSetChanged();
							//Toast.makeText(context, "you click " + position, Toast.LENGTH_SHORT).show();
						}
						tEaraPosition = position;
					}
				});
		if (tEaraPosition < children.size())
			childrenItem.addAll(children.get(tEaraPosition));
		plateListViewAdapter = new TextAdapter(context, childrenItem,
				R.drawable.choose_item_right,
				R.drawable.choose_plate_item_selector);
		plateListViewAdapter.setTextSize(15);
		plateListViewAdapter.setSelectedPositionNoNotify(tBlockPosition);
		plateListView.setAdapter(plateListViewAdapter);
		plateListViewAdapter
				.setOnItemClickListener(new TextAdapter.OnItemClickListener() {

					@Override
					public void onItemClick(View view, final int position) {
						
						showString = childrenItem.get(position);
						if (mOnSelectListener != null) {
							
							mOnSelectListener.getValue(showString);
						}
						//Toast.makeText(context, "you click " + position, Toast.LENGTH_SHORT).show();
						//sendDepartment();
						String name = children.get(tEaraPosition).get(position);
						sendDepartment(name);
					}
				});
		if (tBlockPosition < childrenItem.size())
			showString = childrenItem.get(tBlockPosition);
		if (showString.contains("不限")) {
			showString = showString.replace("不限", "");
		}
		setDefaultSelect();

	}

	private void sendDepartment(String name) {
		List<Param> params = new ArrayList<Param>();
		params.add(new Param("hosname", CurrentHosInfo.getCurHos()));
		params.add(new Param("offname", name));
		//department = HosDepartmentFragment.getGridViewItems(i);
		CurrentHosInfo.setCurDepartment(name);
		OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.CHOOSEDEPARTMENT, new OkHttpManager.HttpCallBack() {
			@Override
			public void onResponse(JSONObject json) {
				try {
					int code = json.getInteger("code");

					if (code == 1000) {
						try {
							Map<String, Object> map = json.getObject("result", Map.class);

							JSONArray hi = (JSONArray) map.get("DocInfo");
							ResultFromServer.initAllDoctor();
							for(int i = 0; i < hi.size(); i++) {
								Map<String, Object> n = hi.getObject(i, Map.class);
								ResultFromServer.getAllDoctor().add(n);
							}
							mContent.startActivity(new Intent(mContent, ShowDoctorsFromFixedHosActivity.class));
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					else if(code == 2001) {
						Toast.makeText(mContent, "该科室暂时没有安排医生", Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(mContent,
								"服务器繁忙请重试...", Toast.LENGTH_SHORT)
								.show();
					}
				}
				catch (JSONException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onFailure(String errorMsg) {

			}
		});
	}
	public void setDefaultSelect() {
		regionListView.setSelection(tEaraPosition);
		plateListView.setSelection(tBlockPosition);
	}

	public String getShowText() {
		return showString;
	}

	public void setOnSelectListener(OnSelectListener onSelectListener) {
		mOnSelectListener = onSelectListener;
	}

	public interface OnSelectListener {
		public void getValue(String showText);
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}
}
