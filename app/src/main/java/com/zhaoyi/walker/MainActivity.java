package com.zhaoyi.walker;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.zhaoyi.walker.Fragment.FirstFragment;
import com.zhaoyi.walker.Fragment.MeFragment;
import com.zhaoyi.walker.Fragment.MessageFragment;
import com.zhaoyi.walker.activity.LoginActivity;
import com.zhaoyi.walker.model.CurrentHosInfo;
import com.zhaoyi.walker.model.ResultFromServer;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.NetworkStatus;
import com.zhaoyi.walker.utils.NetworkType;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;
import com.zhaoyi.walker.view.ImageViewPlus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity {
	//private RadioButton rbFirstPage;
	//private RadioGroup rgTabGroup;
	private Context context;
	private static FragmentManager mFragmentManager;
	/* fragment */
	private FirstFragment firstFragment;
	private MessageFragment messageFragment;
	private MeFragment meFragment;
    private BroadcastReceiver connectionReceiver;

	/* 定位相关 */
	private final int SDK_PERMISSION_REQUEST = 127;
	private String permissionInfo;
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();

	AHBottomNavigation bottomNavigation;  //底部导航栏

	private final int TYPE_REQUEST_CITY = 105;
    private final int TYPE_REQUEST_LOGIN_ME = 106;
	private final int TYPE_REQUEST_LOGOUT = 108;
	private final int TYPE_REQUEST_LOGIN_MSG = 113;
	private final int TYPE_REQUEST_UPDATE_INFO_STATUS = 116;

	private String lastCity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		//获取FragmentManager实例
		mFragmentManager = getSupportFragmentManager();
		context = MainActivity.this;

		/*rbFirstPage = (RadioButton)findViewById(R.id.rb_first_page);
		rgTabGroup = (RadioGroup)findViewById(R.id.rg_tab_group);

		rgTabGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
				//Toast.makeText(MainActivity.this, "You clicked " + i, Toast.LENGTH_SHORT).show();
				switch (i) {
					case R.id.rb_first_page:
						setTabSelection(1);
						break;
					case R.id.rb_find_page:
						setTabSelection(2);
						break;
					case R.id.rb_me_page:
						setTabSelection(3);
						break;
					default:
						break;
				}
			}
		});*/

		//CurrentHosInfo.setCurCity("成都"); //test purpose, delete later
		//initFragment();
		//CurrentHosInfo.setCurCity("成都");
		//setTabSelection(1);
		//rbFirstPage.setChecked(true);
		lastCity = ZyApplication.getInstance()
				.getUserJson()
				.getString(MyContants.JSON_KEY_LASTCITY);
		if (TextUtils.isEmpty(lastCity)) {
			lastCity = "北京";
		}
		CurrentHosInfo.setCurCity(lastCity);

		//dealBottomButtonsClickEvent();
		// Create items
		bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);

		AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.zy_home,
				R.drawable.zy_tab_item_home_unselected, R.color.theme_default_color);
		AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.zy_msg,
				R.drawable.zy_tab_item_message_unselected, R.color.theme_default_color);
		AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.zy_me,
					R.drawable.zy_tab_item_me_unselected, R.color.theme_default_color);

// Add items
		bottomNavigation.addItem(item1);
		bottomNavigation.addItem(item2);
		bottomNavigation.addItem(item3);

		// Set background color
		bottomNavigation.setDefaultBackgroundColor(Color.parseColor("#20d2c3"));

// Disable the translation inside the CoordinatorLayout
		bottomNavigation.setBehaviorTranslationEnabled(false);

// Enable the translation of the FloatingActionButton
		//bottomNavigation.manageFloatingActionButtonBehavior(floatingActionButton);

// Change colors
		bottomNavigation.setAccentColor(Color.parseColor("#F63D2B"));
		bottomNavigation.setInactiveColor(Color.parseColor("#747474"));

// Force to tint the drawable (useful for font with icon for example)
		bottomNavigation.setForceTint(true);

// Display color under navigation bar (API 21+)
// Don't forget these lines in your style-v21
// <item name="android:windowTranslucentNavigation">true</item>
// <item name="android:fitsSystemWindows">true</item>
		bottomNavigation.setTranslucentNavigationEnabled(true);

// Manage titles
		//bottomNavigation.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE);
		//bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
		//bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_HIDE);

// Use colored navigation with circle reveal effect
		bottomNavigation.setColored(true);

// Set current item programmatically
		bottomNavigation.setCurrentItem(0);
		setTabSelection(0);

// Customize notification (title, background, typeface)
		bottomNavigation.setNotificationBackgroundColor(Color.parseColor("#F63D2B"));

// Add or remove notification for each item
		//bottomNavigation.setNotification("1", 3);
// OR
		/*AHNotification notification = new AHNotification.Builder()
				.setText("1")
				.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.color_notification_back))
				.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.color_notification_text))
				.build();
		bottomNavigation.setNotification(notification, 1);*/

// Set listeners
		bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
			@Override
			public boolean onTabSelected(int position, boolean wasSelected) {
				// Do something cool here...
				setTabSelection(position);

				return true;
			}
		});
		bottomNavigation.setOnNavigationPositionListener(new AHBottomNavigation.OnNavigationPositionListener() {
			@Override public void onPositionChange(int y) {
				// Manage the new y position
				bottomNavigation.hideBottomNavigation();
			}
		});

		//CurrentHosInfo.setCurCity(json.getString("curCity"));
		//CurrentHosInfo.setCurCity("成都");

		//NetworkUtils.networkStateTips(context);  // 检查网络状态
		isShowNetworkUnavailableAlert();

		//声明LocationClient类
		mLocationClient = new LocationClient(getApplicationContext());

		//注册监听函数
		mLocationClient.registerLocationListener( myListener );

		/* 设置定位参数 */
		initLocation();

		/* 动态获取权限 */
		getPersimmions();
		mLocationClient.start();

        networkBroadcastReceiverRegister();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(connectionReceiver, intentFilter);
	}

	public static FragmentManager getfMgr() {
		return mFragmentManager;
	}

	private void networkBroadcastReceiverRegister() {
        connectionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if(wifiNetInfo.isConnected()) {
                    NetworkStatus.currentNetworkStatus = NetworkType.NetworkWifi;
                    NetworkStatus.isCurrentNetworkAvailable = true;
                }
                else if(mobNetInfo.isConnected()) {
                    NetworkStatus.currentNetworkStatus = NetworkType.NetworkWifi;
                    NetworkStatus.isCurrentNetworkAvailable = true;
                } else {
                    NetworkStatus.currentNetworkStatus = NetworkType.NetworkUnavailable;
                    NetworkStatus.isCurrentNetworkAvailable = false;
                }
                isShowNetworkUnavailableAlert();
                //if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
                    //Log.i("Info: ", "unconnect");
                    // unconnect network
                    //NetworkStatus.CurrentNetworkStatus = NetworkType.NetworkUnavailable;
                //}else {

                    // connect network
               // }
            }
        };
    }
	/**
	 * 处理底部点击事件
	 */
	private void setTabSelection(int i) {
		//mFragmentManager = getSupportFragmentManager();
		if(i == 0 && firstFragment != null && firstFragment.isVisible()) {
			return;
		} else if(i == 1 && messageFragment != null && messageFragment.isVisible()) {
			return;
		} else if(i == 2 && meFragment != null && meFragment.isVisible()) {
			return;
		}

		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		hideFragments(transaction);
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

		String isLogin = ZyApplication.getInstance()
				.getUserJson()
				.getString(MyContants.JSON_KEY_ISLOGIN);

		switch (i) {
			case 0:
				if (firstFragment == null) {
					firstFragment = new FirstFragment();
					transaction.add(R.id.fragmentRoot, firstFragment, "FirstFragment");
				} else {
					transaction.show(firstFragment);
					//isShowNetworkUnavailableAlert();
				}
				break;
			case 1:
				if(!TextUtils.isEmpty(isLogin) && isLogin.equals("1")) {
					if (messageFragment == null) {
						messageFragment = new MessageFragment();
						transaction.add(R.id.fragmentRoot, messageFragment, "MessageFragment");
					} else {
						transaction.show(messageFragment);
						messageFragment.getMessage(true);
					}
				} else {
					startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), TYPE_REQUEST_LOGIN_MSG);
				}
				break;
			case 2:
				if(!TextUtils.isEmpty(isLogin) && isLogin.equals("1")) {
					if (meFragment == null) {
						meFragment = new MeFragment();
						transaction.add(R.id.fragmentRoot, meFragment, "MeFragment");
					} else {
						transaction.show(meFragment);
					}
				}
				else {
						startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), TYPE_REQUEST_LOGIN_ME);
				}
				break;
		}
		transaction.commit();
	}

	private void isShowNetworkUnavailableAlert() {
		RelativeLayout rl_alert = (RelativeLayout) findViewById(R.id.rl_network_unavailable);
		if(rl_alert != null) {
			if (NetworkStatus.isCurrentNetworkAvailable) {
				rl_alert.setVisibility(RelativeLayout.GONE);
			} else {
				rl_alert.setVisibility(RelativeLayout.VISIBLE);
			}
		}
    }

	/**
	 * 将所有的Fragment都置为隐藏状态
	 *
	 * @param transaction
	 *            用于对Fragment执行操作的事务
	 */
	private void hideFragments(FragmentTransaction transaction) {
		if (firstFragment != null && firstFragment.isVisible()) {
			transaction.hide(firstFragment);
		}
		if (messageFragment != null && messageFragment.isVisible()) {
			transaction.hide(messageFragment);
		}
		if (meFragment != null && meFragment.isVisible()) {
			transaction.hide(meFragment);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK) {
			if(requestCode == TYPE_REQUEST_CITY) {
				if(TextUtils.isEmpty(CurrentHosInfo.getCurCity())) {
					CurrentHosInfo.setCurCity("北京");
				}

				((TextView)mFragmentManager.findFragmentByTag("FirstFragment").getView()
						.findViewById(R.id.tv_city)).setText(CurrentHosInfo.getCurCity());

				new Handler().post(new Runnable() {
					@Override
					public void run() {
						//firstFragment.refreshDocList(CurrentHosInfo.getCurCity());
						firstFragment.showRecommendDoctor();
					}
				});
			}
            else if (requestCode == TYPE_REQUEST_LOGIN_ME) {
                //bottomNavigation.setCurrentItem(2);
				new Handler().post(new Runnable() {
					@Override
					public void run() {
						setTabSelection(2);
					}
				});
            } else if(requestCode == TYPE_REQUEST_LOGIN_MSG) {
				new Handler().post(new Runnable() {
					@Override
					public void run() {
						setTabSelection(1);
					}
				});
			}
            else if(requestCode == TYPE_REQUEST_LOGOUT) {
				new Handler().post(new Runnable() {
					@Override
					public void run() {
						bottomNavigation.setCurrentItem(0);
						setTabSelection(0);
					}
				});
			}
			else if(requestCode == TYPE_REQUEST_UPDATE_INFO_STATUS) {
				final ImageViewPlus ivAvatar = (ImageViewPlus) findViewById(R.id.iv_avatar);

				if(ivAvatar != null) {
					String avatarUrl = MyContants.BASE_URL + ZyApplication.getInstance()
							.getUserJson()
							.getString(MyContants.JSON_KEY_AVATAR);

					Glide.with(MainActivity.this)
							.load(avatarUrl)
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
				}
			}

        }
        else if(resultCode == RESULT_CANCELED) {
			if(requestCode == TYPE_REQUEST_LOGIN_ME || requestCode == TYPE_REQUEST_LOGIN_MSG) {
				new Handler().post(new Runnable() {
					@Override
					public void run() {
						bottomNavigation.setCurrentItem(0);
						setTabSelection(0);
					}
				});
			}
		}
	}

	/* 定位可选项 */
	private void initLocation(){
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
		//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

		option.setCoorType("bd09ll");
		//可选，默认gcj02，设置返回的定位结果坐标系

		option.setIsNeedAddress(true);
		//返回地址信息，默认不返回

		//int span=1000;
		//option.setScanSpan(span);
		//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

		//option.setIsNeedAddress(true);
		//可选，设置是否需要地址信息，默认不需要

		//option.setOpenGps(true);
		//可选，默认false,设置是否使用gps

		//option.setLocationNotify(true);
		//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

		//option.setIsNeedLocationDescribe(true);
		//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

		//option.setIsNeedLocationPoiList(true);
		//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

		option.setIgnoreKillProcess(false);
		//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

		option.SetIgnoreCacheException(false);
		//可选，默认false，设置是否收集CRASH信息，默认收集

		//option.setEnableSimulateGps(false);
		//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

		mLocationClient.setLocOption(option);
	}

	/* 定位回调监听接口 */
	class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(final BDLocation location) {
			//获取定位结果
			StringBuffer sb = new StringBuffer(256);

			sb.append("time : ");
			sb.append(location.getTime());    //获取定位时间

			sb.append("\nerror code : ");
			sb.append(location.getLocType());    //获取类型

			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());    //获取纬度信息

			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());    //获取经度信息

			sb.append("\nradius : ");
			sb.append(location.getRadius());    //获取定位精准度

			if (location.getLocType() == BDLocation.TypeGpsLocation){

				// GPS定位结果
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());    // 单位：公里每小时

				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());    //获取卫星数

				sb.append("\nheight : ");
				sb.append(location.getAltitude());    //获取海拔高度信息，单位米

				sb.append("\ndirection : ");
				sb.append(location.getDirection());    //获取方向信息，单位度

				sb.append("\naddr : ");
				sb.append(location.getAddrStr());    //获取地址信息

				sb.append("\ndescribe : ");
				sb.append("gps定位成功");

			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation){

				// 网络定位结果
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());    //获取地址信息

				sb.append("\noperationers : ");
				sb.append(location.getOperators());    //获取运营商信息

				sb.append("\ndescribe : ");
				sb.append("网络定位成功");

			} else if (location.getLocType() == BDLocation.TypeOffLineLocation) {

				// 离线定位结果
				sb.append("\ndescribe : ");
				sb.append("离线定位成功，离线定位结果也是有效的");

			} else if (location.getLocType() == BDLocation.TypeServerError) {

				sb.append("\ndescribe : ");
				sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");

			} else if (location.getLocType() == BDLocation.TypeNetWorkException) {

				sb.append("\ndescribe : ");
				sb.append("网络不同导致定位失败，请检查网络是否通畅");

			} else if (location.getLocType() == BDLocation.TypeCriteriaException) {

				sb.append("\ndescribe : ");
				sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");

			}

			sb.append("\nlocationdescribe : ");
			sb.append(location.getLocationDescribe());    //位置语义化信息

			List<Poi> list = location.getPoiList();    // POI数据
			if (list != null) {
				sb.append("\npoilist size = : ");
				sb.append(list.size());
				for (Poi p : list) {
					sb.append("\npoi= : ");
					sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
				}
			}

			Log.i("BaiduLocationApiDem", sb.toString());

			//CurrentHosInfo.setCurCity(city);
			//cacheCurCity(city);
			//if(!TextUtils.isEmpty(city)) {
				//getData(city);
			//}
			final String curCity = location.getCity().toString().substring(0,
					location.getCity().length() - 1);

            if((!TextUtils.isEmpty(curCity) && !curCity.equals(lastCity))) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
				dialog.setTitle("提示")
                .setMessage("当前城市与定位到的不一致，是否切换？")
                .setPositiveButton("切换", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
						JSONObject userJson = ZyApplication.getInstance()
								.getUserJson();

                        userJson.put(MyContants.JSON_KEY_LASTCITY, curCity);  // 去除市

                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((TextView)((MainActivity.getfMgr().findFragmentByTag("FirstFragment").getView()).findViewById(
                                        R.id.tv_city))).setText(curCity);

								getData(curCity, false);
                            }
                        });
						CurrentHosInfo.setCurCity(curCity);
                        ZyApplication.getInstance().setUserJson(userJson);

                    }
                }).setNegativeButton("不切换", null)
				.show();
            }
            String myLatitude = ZyApplication.getInstance()
					.getUserJson()
					.getString(MyContants.JSON_KEY_LATITUDE);
			String myLongitude = ZyApplication.getInstance()
					.getUserJson()
					.getString(MyContants.JSON_KEY_LONGITUDE);
			if(TextUtils.isEmpty(myLatitude) || TextUtils.isEmpty(myLongitude) ||
					!myLatitude.equals(location.getLatitude()) ||
					!myLongitude.equals(location.getLongitude())) {
				JSONObject userJson = ZyApplication.getInstance().getUserJson();
				userJson.put(MyContants.JSON_KEY_LATITUDE, location.getLatitude());
				userJson.put(MyContants.JSON_KEY_LONGITUDE, location.getLongitude());
				ZyApplication.getInstance().setUserJson(userJson);
			}
			mLocationClient.stop();
		}

		@Override
		public void onConnectHotSpotMessage(String s, int i) {

		}
	}

	@TargetApi(23)
	private void getPersimmions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			ArrayList<String> permissions = new ArrayList<String>();
			/***
			 * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
			 */
			// 定位精确位置
			if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
				permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
			}
			if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
				permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
			}
			/*
			 * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
			// 读写权限
			/*if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
			}
			// 读取电话状态权限
			if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
				permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
			}*/

			if (permissions.size() > 0) {
				requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
			}
			else {
				mLocationClient.start();
			}
		}
		else {
			mLocationClient.start();
		}
	}
	@TargetApi(23)
	private boolean addPermission(ArrayList<String> permissionsList, String permission) {
		if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
			if (shouldShowRequestPermissionRationale(permission)){
				return true;
			}else{
				permissionsList.add(permission);
				return false;
			}

		}else{
			return true;
		}
	}

	@TargetApi(23)
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		// TODO Auto-generated method stub
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch(requestCode) {

			// requestCode即所声明的权限获取码，在checkSelfPermission时传入
			case SDK_PERMISSION_REQUEST:
				//BAIDU_READ_PHONE_STATE:
				for(int i = 0; i < grantResults.length; i++) {
					if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
						// 未获取到权限，作相应处理（调用定位SDK应当确保相关权限均被授权，否则可能引起定位失败）
						return;
					}
				}
				mLocationClient.start();
				break;

			default:
				break;

		}
	}

	/* 获取推荐医生内容 */
	private void getData(String city, final boolean isRefresh) {
		List<Param> params = new ArrayList<Param>();
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
							ResultFromServer.initAllDoctor();
							for (int i = 0; i < hi.size(); i++) {
								Map<String, Object> n = hi.getObject(i, Map.class);
								ResultFromServer.getAllDoctor().add(n);
							}
						} else if(code == 2001) {
							ResultFromServer.getAllDoctor().clear();
							Toast.makeText(MainActivity.this, "暂时没有推荐医生", Toast.LENGTH_SHORT).show();
						}
						if(!isRefresh) {
							FragmentTransaction transaction = mFragmentManager.beginTransaction();
							firstFragment = new FirstFragment();
							transaction.replace(R.id.fragmentRoot, firstFragment, "FirstFragment");
							transaction.commit();
						}
					}

					@Override
					public void onFailure(String errorMsg) {

					}
				}
		);
	}
	@Override
	protected void onStop() {
		super.onStop();
		mLocationClient.unRegisterLocationListener( myListener );
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		String lastCity = ZyApplication.getInstance()
				.getUserJson()
				.getString(MyContants.JSON_KEY_LASTCITY);
		String curCity = CurrentHosInfo.getCurCity();

		if(!TextUtils.isEmpty(curCity)) {
			if(TextUtils.isEmpty(lastCity) || !lastCity.equals(curCity)) {
					JSONObject userJson = ZyApplication.getInstance()
							.getUserJson();
					userJson.put(MyContants.JSON_KEY_LASTCITY, curCity);
					ZyApplication.getInstance().setUserJson(userJson);
			}
		}
		if(connectionReceiver != null) {
			unregisterReceiver(connectionReceiver);
		}
	}
}
