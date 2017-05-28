package com.zhaoyi.walker.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.ZyApplication;
import com.zhaoyi.walker.adapter.SwipeAdapter;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageFragment extends Fragment{
	//private SwipeLayout swipeLayout;
	private ListView mListView;
	private ArrayList<ArrayList<String>> msg;
	private SwipeAdapter messageAdapter;
	private String id;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.message_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		this.getActivity().findViewById(R.id.iv_top_back).setVisibility(ImageView.INVISIBLE);
		((TextView) this.getActivity().findViewById(R.id.tv_top_title)).setText("消息中心");
		mListView = (ListView) this.getActivity().findViewById(R.id.lv_msg_list);

		//SwipeAdapter swipeAdapter = new SwipeAdapter(getActivity(), data);
		//mListView.setAdapter(swipeAdapter);
		msg = new ArrayList<>();
		id = ZyApplication.getInstance()
				.getUserJson()
				.getString(MyContants.JSON_KEY_ZYID);

		getMessage(false);

	}

	public void getMessage(final boolean isRefresh) {

		String userId = ZyApplication.getInstance()
				.getUserJson()
				.getString(MyContants.JSON_KEY_ZYID);
		if(TextUtils.isEmpty(userId)) {
			Log.e("Error: ", "userId can't be empty.");
			return;
		}

		msg.clear();

		List<Param> params = new ArrayList<>();
		params.add(new Param("userId", userId));

		OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.GETMESSAGE,
				new OkHttpManager.HttpCallBack() {
					@Override
					public void onResponse(JSONObject json) {
						int code = json.getInteger("code");
						if(code == 1000) {

							JSONObject obj = json.getJSONObject("result");
							JSONArray array = obj.getJSONArray("MsgInfo");

							int size = array.size();
							for(int i = 0; i < size; i++) {
								Map<String, Object> map = (Map<String, Object>) array.get(i);
								ArrayList<String> iList = new ArrayList<>();
								iList.add(MyContants.BASE_URL + map.get("image"));
								iList.add((String) map.get("title"));
								iList.add((String) map.get("abstract_"));
								iList.add((String) map.get("url"));
								iList.add((String) map.get("serial"));
								msg.add(iList);
							}
							if(isRefresh) {
								messageAdapter.notifyDataSetChanged();
							} else {
								messageAdapter = new SwipeAdapter(MessageFragment.this.getActivity(), msg);
								mListView.setAdapter(messageAdapter);
							}
						} else if(code == 2001) {
							//Toast.makeText(MessageFragment.this.getActivity(), "获取消息失败",
									//Toast.LENGTH_SHORT).show();
						}
					}

					@Override
					public void onFailure(String errorMsg) {

					}
				});
	}

}
