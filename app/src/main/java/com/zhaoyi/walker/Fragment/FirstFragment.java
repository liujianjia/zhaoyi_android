package com.zhaoyi.walker.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.ZyApplication;
import com.zhaoyi.walker.activity.AllCityActivity;
import com.zhaoyi.walker.activity.DoctorDetailInfoActivity;
import com.zhaoyi.walker.activity.MySearchViewActivity;
import com.zhaoyi.walker.activity.ShowHosListActivity;
import com.zhaoyi.walker.activity.WebViewActivity;
import com.zhaoyi.walker.adapter.MyCollectionDocsAdapter;
import com.zhaoyi.walker.custom.GlideImageLoader;
import com.zhaoyi.walker.custom.ListViewForScrollView;
import com.zhaoyi.walker.model.CurrentHosInfo;
import com.zhaoyi.walker.model.ResultFromServer;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//import android.support.v4.widget.SimpleCursorAdapter;

public class FirstFragment extends Fragment{
	private Context context;
	private static final String[] homeOperation = {"预约挂号", /*"科室挂号", "免费导诊", "咨询医生",
	"陪诊服务", "口腔护理", "母婴呵护", */"健康资讯"};

	//private GridView gvHomeOperation;
	private TextView tvCity;
	private ListViewForScrollView lvRecommendDoctor;
	private SwipeRefreshLayout swipeRefreshLayout;
	private MyCollectionDocsAdapter myCollectionDocsAdapter;
	private ArrayList<ArrayList<String>> alDoctor;
	private final int TYPE_REQUEST_CITY = 105;
	private ArrayList<String> mPicUrl;

    Banner banner; //图片轮播
	private EditText search;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.first_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		context = FirstFragment.this.getActivity();

		//gvHomeOperation = (GridView) this.getActivity().findViewById(R.id.gv_home_index_icon);
		tvCity = (TextView)this.getActivity().findViewById(R.id.tv_city);
		lvRecommendDoctor = (ListViewForScrollView) this.getActivity().findViewById(R.id.lv_recommend_doctor);
		swipeRefreshLayout = (SwipeRefreshLayout)this.getActivity().findViewById(R.id.rl_swipe_layout);
		swipeRefreshLayout.setColorSchemeResources(R.color.bili_red, R.color.green, R.color.orange);
		search = (EditText) getActivity().findViewById(R.id.et_search);

		//((ScrollView) getActivity().findViewById(R.id.sv_home)).smoothScrollTo(0, 0);

		tvCity.setText(CurrentHosInfo.getCurCity());
		search.setFocusable(false);
		search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FirstFragment.this.getActivity(), MySearchViewActivity.class);
				startActivity(intent);
			}
		});

		getActivity().findViewById(R.id.iv_register).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				submitUserGetNumber(CurrentHosInfo.getCurCity());
			}
		});
		getActivity().findViewById(R.id.iv_info).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FirstFragment.this.getActivity(),
						WebViewActivity.class);
				intent.putExtra("url", "http://health.sina.com.cn/");  // 健康资讯
				startActivity(intent);
			}
		});

        final List<Object> imagesUrl = new ArrayList<>();
		mPicUrl = new ArrayList<>();

		if(ResultFromServer.getHomeInfo() !=  null) {
			int size = ResultFromServer.getHomeInfo().size();
			for (int i = 0; i < size; i++) {
				imagesUrl.add(MyContants.BASE_URL + ResultFromServer.getHomeInfo().get(i).get("image"));
				mPicUrl.add((String) ResultFromServer.getHomeInfo().get(i).get("imageUrl"));
			}
		}

		banner = (Banner) this.getActivity().findViewById(R.id.banner);
        //设置banner样式
        //banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE);
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        //设置图片集合
        banner.setImages(imagesUrl);
        //设置banner动画效果
        //banner.setBannerAnimation(Transformer.DepthPage);
        //设置标题集合（当banner样式有显示title时）
        //banner.setBannerTitles(titles);
        //设置自动轮播，默认为true
        //banner.isAutoPlay(true);
        //设置轮播时间
        //banner.setDelayTime(1500);
        //设置指示器位置（当banner模式中有指示器时）
        //banner.setIndicatorGravity(BannerConfig.CENTER);
        //banner设置方法全部调用完毕时最后调用
		banner.start();
		banner.setOnBannerListener(new OnBannerListener() {
			@Override
			public void OnBannerClick(int position) {
				Intent intent = new Intent(FirstFragment.this.getActivity(),
						WebViewActivity.class);
				intent.putExtra("url", mPicUrl.get(position));
				startActivity(intent);
			}
		});

		getActivity().findViewById(R.id.rl_city).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FirstFragment.this.getActivity(), AllCityActivity.class);
				FirstFragment.this.getActivity().startActivityForResult(intent, TYPE_REQUEST_CITY);
			}
		});

		alDoctor = new ArrayList<>();

		showRecommendDoctor();

		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refreshDocList(CurrentHosInfo.getCurCity());
				swipeRefreshLayout.postDelayed(new Runnable() {
					@Override
					public void run() {
						myCollectionDocsAdapter.notifyDataSetChanged();  // 通知视图底层数据已更改
						swipeRefreshLayout.setRefreshing(false);  // 刷新已结束
					}
				}, 1000);
			}
		});
		lvRecommendDoctor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				enterDocDetailPage(position);
			}
		});
	}

	/* 预约挂号 */
	private void submitUserGetNumber(final String city) {
		if(TextUtils.isEmpty(city)) {
			Log.d("current city is ----->>", "null");
			return;
		}
		List<Param> params = new ArrayList<Param>();

		params.add(new Param("address", city));

		OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.CHOOSECITY,
				new OkHttpManager.HttpCallBack() {
			@Override
			public void onResponse(JSONObject json) {
				try {
					int code = json.getInteger("code");
                    if (code == 1000) {
                        Map<String, Object> map = json.getObject("result", Map.class);

                        JSONArray hi = (JSONArray) map.get("HosInfo");
						JSONArray region = (JSONArray) map.get("districts");
						JSONArray types = (JSONArray) map.get("types");
                        ResultFromServer.initFixedCityHos();
						ResultFromServer.getFixedCityHos().clear();
						ArrayList<String> regionList = new ArrayList<>();
						regionList.add("全部");
						int size = region.size();
						for(int i = 0; i < size; i++) {
							String re = region.getString(i);
							regionList.add(re);
						}

						size = hi.size();
                        for (int i = 0; i < size; i++) {
                            Map<String, Object> n = hi.getObject(i, Map.class);
                            ResultFromServer.addFixedCityHos(n);
                        }
                        Intent intent = new Intent(FirstFragment.this.getActivity(), ShowHosListActivity.class);
						intent.putExtra("regionList", regionList);
						intent.putExtra("typeList", types);
                        startActivity(intent);
                    }
					else if (code == 2001) {
						Toast.makeText(FirstFragment.this.getActivity(), "暂时没有结果", Toast.LENGTH_SHORT).show();
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

	/*private void setUpHomeOperation()  {
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < homeOperation.length; i++) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("imageItem", image[i]);
			item.put("textItem", homeOperation[i]);
			items.add(item);
		}

		//实例化一个适配器
		SimpleAdapter adapter = new SimpleAdapter(FirstFragment.this.getActivity(),
				items,
				R.layout.gridview_item,
				new String[]{"imageItem", "textItem"},
				new int[]{R.id.iv_imageItem, R.id.text_item});

		gvHomeOperation.setAdapter(adapter);
		gvHomeOperation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				switch (i) {
					case 0:
                        submitUserGetNumber(CurrentHosInfo.getCurCity());
						break;
                    case 1:
                        Intent intent = new Intent(FirstFragment.this.getActivity(),
                                WebViewActivity.class);
                        intent.putExtra("url", "http://health.sina.com.cn/");  // 健康资讯
                        startActivity(intent);
                        break;
					default:
					    break;
				}
			}
		});
	}*/

	/* 刷新推荐列表 */
	public void refreshDocList(String city) {
		alDoctor.clear();
		List<Param> params = new ArrayList<Param>();
		if(TextUtils.isEmpty(city)) {
			city = "北京";
		}

		params.add(new Param("address", city));

		OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.RECOMMENDDOCTOR,
				new OkHttpManager.HttpCallBack() {
					@Override
					public void onResponse(JSONObject json) {
						try {
							int code = json.getInteger("code");
							if(code == 1000) {
								Map<String, Object> map = json.getObject("result", Map.class);

								JSONArray hi = (JSONArray) map.get("DocInfo");
								JSONArray homeInfo = (JSONArray)map.get("HomeInfo");
								ResultFromServer.initAllDoctor();
								ResultFromServer.initHomeInfo();
								for (int i = 0; i < hi.size(); i++) {
									Map<String, Object> n = hi.getObject(i, Map.class);
									ResultFromServer.getAllDoctor().add(n);
								}
								ResultFromServer.addHomeInfo(homeInfo.getObject(0, Map.class));
								showRecommendDoctor();
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onFailure(String errorMsg) {

					}
				}
		);

	}

	/* 显示首页推荐医生 */
	public void showRecommendDoctor() {
		try {
			alDoctor.clear();
			if (ResultFromServer.getAllDoctor() != null) {
				DecimalFormat df = new DecimalFormat("0.0");
				int size = ResultFromServer.getAllDoctor().size();
				for (int i = 0; i < size; i++) {
					Map<String, Object> map = ResultFromServer.getAllDoctor().get(i);
					ArrayList<String> iList = new ArrayList<>();
					iList.add((String) map.get("id"));
					iList.add(MyContants.BASE_URL + map.get("image"));
					iList.add((String) map.get("docname"));
					iList.add(df.format(map.get("score")));
					iList.add((String) map.get("reg_count"));
					iList.add((String) map.get("title"));
					iList.add((String) map.get("offname"));
					iList.add((String) map.get("hosname"));
					iList.add((String) map.get("hosaddress"));
					alDoctor.add(iList);
				}
				if(myCollectionDocsAdapter != null) {
					myCollectionDocsAdapter.notifyDataSetChanged();
				} else {
					myCollectionDocsAdapter = new MyCollectionDocsAdapter(context, alDoctor);
					lvRecommendDoctor.setAdapter(myCollectionDocsAdapter);
				}
			}
			else {

			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
    public void onStart() {
        super.onStart();
        //开始轮播
        banner.startAutoPlay();
    }
    @Override
    public void onStop() {
        super.onStop();
        //结束轮播
        banner.stopAutoPlay();
    }

	private void enterDocDetailPage(int pos) {
		List<Param> params = new ArrayList<Param>();

		if(pos >= alDoctor.size()) {
			Toast.makeText(context, "内部错误，没有这样的元素", Toast.LENGTH_SHORT).show();
			return;
		}

		String docName = alDoctor.get(pos).get(2);

		String isLogin = ZyApplication.getInstance()
				.getUserJson()
				.getString(MyContants.JSON_KEY_ISLOGIN);

		String userId = "";
		if(!TextUtils.isEmpty(isLogin) && isLogin.equals("1")) {
			userId = ZyApplication.getInstance()
					.getUserJson()
					.getString(MyContants.JSON_KEY_ZYID);
		}

		String dep = alDoctor.get(pos).get(6);
		String hosp = alDoctor.get(pos).get(7);
		String docId = alDoctor.get(pos).get(0);
		String hosAddress = alDoctor.get(pos).get(8);

		if(TextUtils.isEmpty(docName)) {
			Toast.makeText(context, "内部错误：1001", Toast.LENGTH_SHORT).show();
			Log.e("Fatal Error: ", "docName shouldn't be use as empty.");
			return;
		}
		if(TextUtils.isEmpty(hosp)) {
			Toast.makeText(context, "内部错误：1001", Toast.LENGTH_SHORT).show();
			Log.e("Fatal Error: ", "hosName shouldn't be use as empty.");
			return;
		}
		if(TextUtils.isEmpty(dep)) {
			Toast.makeText(context, "内部错误：1001", Toast.LENGTH_SHORT).show();
			Log.e("Fatal Error: ", "department shouldn't be use as empty.");
			return;
		}
		if(TextUtils.isEmpty(docId)) {
			Toast.makeText(context, "内部错误：1001", Toast.LENGTH_SHORT).show();
			Log.e("Fatal Error: ", "docId shouldn't be use as empty.");
			return;
		}
		if(TextUtils.isEmpty(docName)) {
			Toast.makeText(context, "内部错误：1001", Toast.LENGTH_SHORT).show();
			Log.e("Fatal Error: ", "docName shouldn't be use as empty.");
			return;
		}
		if(TextUtils.isEmpty(hosAddress)) {
			Toast.makeText(context, "内部错误：1001", Toast.LENGTH_SHORT).show();
			Log.e("Fatal Error: ", "hosAddress shouldn't be use as empty.");
			return;
		}

		CurrentHosInfo.setCurHos(hosp);
		CurrentHosInfo.setCurDepartment(dep);
		CurrentHosInfo.setCurHosAddress(hosAddress);

		params.add(new Param("docname", docName));
		params.add(new Param("hosname", hosp));
		params.add(new Param("offname", dep));
		params.add(new Param("docId", docId));
		params.add(new Param("userId", userId));
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
						startActivity(new Intent(context, DoctorDetailInfoActivity.class));
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {

				}
			}

			@Override
			public void onFailure(String errorMsg) {

			}
		});
	}
}