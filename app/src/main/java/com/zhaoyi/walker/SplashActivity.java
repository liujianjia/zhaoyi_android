package com.zhaoyi.walker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.view.animation.textsurface.TextSurface;
import com.zhaoyi.walker.model.ResultFromServer;
import com.zhaoyi.walker.utils.CookieThumper;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.NetworkStatus;
import com.zhaoyi.walker.utils.NetworkUtils;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SplashActivity extends Activity {
	private Context context;
	private TextSurface tv_surface;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_splash);
		context = SplashActivity.this;

		tv_surface = (TextSurface) findViewById(R.id.tv_surface);
		tv_surface.postDelayed(new Runnable() {
			@Override
			public void run() {
				showAnim();
			}
		}, 500);

		/* 请求首页内容 */
		if(NetworkUtils.isNetworkAvailable(SplashActivity.this)) {
            NetworkStatus.isCurrentNetworkAvailable = true;
            getData();
		} else {
            NetworkStatus.isCurrentNetworkAvailable = false;
        }

		/* 开始定位 */
		//mLocationClient.start();
	}

	/* 展示启动页面 */
	private void showAnim() {
		tv_surface.reset();
		CookieThumper.play(tv_surface, getAssets());

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(context, MainActivity.class);
				startActivity(intent);
				finishActivity();
			}
		}, 5000);
	}

	private void finishActivity() {
		this.finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/* 请求首页内容 */
	private void getData() {
		List<Param> params = new ArrayList<Param>();
		String city = ZyApplication.getInstance()
				.getUserJson()
				.getString(MyContants.JSON_KEY_LASTCITY);

		if(TextUtils.isEmpty(city)) {
			city = "北京";
		}

		params.add(new Param("address", city));

		OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.RECOMMENDDOCTOR,
				new OkHttpManager.HttpCallBack() {
					@Override
					public void onResponse(JSONObject json) {
						int code = json.getInteger("code");
						if(code == 1000) {
							Map<String, Object> map = json.getObject("result", Map.class);

							JSONArray hi = (JSONArray) map.get("DocInfo");
							JSONArray homeInfo = (JSONArray)map.get("HomeInfo");
							ResultFromServer.initAllDoctor();
							ResultFromServer.initHomeInfo();
							if(hi != null) {
								for (int i = 0; i < hi.size(); i++) {
									Map<String, Object> n = hi.getObject(i, Map.class);
									ResultFromServer.getAllDoctor().add(n);
								}
							}
							if(homeInfo != null) {
								for(int i = 0; i < homeInfo.size(); i++) {
									ResultFromServer.addHomeInfo(homeInfo.getObject(i, Map.class));
								}
							}
						}
					}

					@Override
					public void onFailure(String errorMsg) {

					}
				}
		);
	}
}
