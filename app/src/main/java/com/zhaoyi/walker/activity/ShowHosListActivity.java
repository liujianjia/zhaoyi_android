package com.zhaoyi.walker.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.ZyApplication;
import com.zhaoyi.walker.adapter.HospAdapter;
import com.zhaoyi.walker.adapter.TextAdapter;
import com.zhaoyi.walker.model.CurrentHosInfo;
import com.zhaoyi.walker.model.ResultFromServer;
import com.zhaoyi.walker.utils.GeoCoderDecoder;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by jianjia Liu on 2017/4/5.
 */

public class ShowHosListActivity extends FragmentActivity {
    private ListView lvCityHospital;
    private ImageView ivHosListBack;
    private TextView tvHosListTitle;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PopupWindow mSelectRegionPopupWindow;
    private PopupWindow mSortMethodPopupWindow;
    private PopupWindow mFilterPopupWindow;
    private RadioButton rbSelectHosp;
    private RadioButton rbSortMethod;
    private RadioButton rbFilter;
    private ListView lvSelectRegion;
    private ListView lvSortMethod;
    private ListView lvFilter;
    private ArrayList<ArrayList<String>> mHosp;
    private ArrayList<ArrayList<String>> mSelectedRegionHosp;
    private ArrayList<ArrayList<String>> mFilterTypeHosp;
    private ArrayList<ArrayList<String>> mLastHospList;
    private ArrayList<String> mRegion;
    private ArrayList<String> mFilter;
    private ArrayList<String> mSortMethod;
    private int displayWidth;
    private HospAdapter hospAdapter;
    private TextAdapter selectRegionAdapter;
    private TextAdapter sortMethodAdapter;
    private TextAdapter filterAdapter;
    private ColorStateList cs0;
    private ColorStateList cs1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hosp_list);

        lvCityHospital = (ListView) findViewById(R.id.list_city_hospital);
        ivHosListBack = (ImageView) findViewById(R.id.iv_top_back);
        tvHosListTitle = (TextView) findViewById(R.id.tv_top_title);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.rl_swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.bili_red, R.color.green, R.color.orange);
        rbSelectHosp = (RadioButton) findViewById(R.id.rb_select_hos);
        rbSortMethod = (RadioButton) findViewById(R.id.rb_sort_method);
        rbFilter = (RadioButton) findViewById(R.id.rb_filter);

        displayWidth = this.getWindowManager().getDefaultDisplay().getWidth();
        rbSelectHosp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedSelectRegion();
            }
        });
        rbSortMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedSortMethod();
            }
        });
        rbFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedFilter();
            }
        });
        cs0 = getResources().getColorStateList(R.color.theme_default_color);
        cs1 = getResources().getColorStateList(R.color.gray3);

        ((RadioGroup)findViewById(R.id.rg_hos)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.rb_select_hos:
                        rbSelectHosp.setTextColor(cs0);
                        rbSortMethod.setTextColor(cs1);
                        rbFilter.setTextColor(cs1);
                        break;
                    case R.id.rb_sort_method:
                        rbSelectHosp.setTextColor(cs1);
                        rbSortMethod.setTextColor(cs0);
                        rbFilter.setTextColor(cs1);
                        break;
                    case R.id.rb_filter:
                        rbSelectHosp.setTextColor(cs1);
                        rbSortMethod.setTextColor(cs1);
                        rbFilter.setTextColor(cs0);
                        break;
                    default:
                        break;
                }
            }
        });

        Bundle args = getIntent().getExtras();
        mRegion = args.getStringArrayList("regionList");
        mFilter = args.getStringArrayList("typeList");
        mSelectedRegionHosp = new ArrayList<>();
        mFilterTypeHosp = new ArrayList<>();
        mSortMethod = new ArrayList<>();
        mSortMethod.add("智能排序");
        mSortMethod.add("评分优先");
        mSortMethod.add("预约量优先");
        mSortMethod.add("距离优先");

        readDataFromServer();

        hospAdapter = new HospAdapter(this, mHosp);
        lvCityHospital.setAdapter(hospAdapter);
        lvCityHospital.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                enterDetailPage(position);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshHosList();
                swipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //putResult();
                        hospAdapter.notifyDataSetChanged();  // 通知视图底层数据已更改
                        swipeRefreshLayout.setRefreshing(false);  // 刷新已结束
                    }
                }, 1000);
            }
        });

        ivHosListBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvHosListTitle.setText("预约挂号");
        realSort(8);
    }

    private void readDataFromServer() {
        int size = ResultFromServer.getFixedCityHos().size();

        ArrayList<String> pendingUpdateLocation = new ArrayList<>();
        mHosp = new ArrayList<>();
        String strMyLatitude = ZyApplication.getInstance().getUserJson().getString("myLatitude");
        String strMyLongitude = ZyApplication.getInstance().getUserJson().getString("myLongitude");
        LatLng myLocation = null;
        if(!(strMyLatitude == null) && !(strMyLongitude == null)) {
            double myLatitude = Double.parseDouble(strMyLatitude);
            double myLongitude = Double.parseDouble(strMyLongitude);
            myLocation = new LatLng(myLatitude, myLongitude);
        }

        float praise;
        int appointment;

        double scoreWeights = 0.5;
        double appointmentWeights = 0.1;
        double distanceWeights = 0.1;
        double distance;
        DecimalFormat df = new DecimalFormat("0.0");

        /* 读取列表 */
        for (int i = 0; i < size; i++) {
            Map<String, Object> result = ResultFromServer.getFixedCityHos().get(i);
            ArrayList<String> iList = new ArrayList<>();
            iList.add(MyContants.BASE_URL + result.get("image"));
            iList.add((String)result.get("hosName"));
            iList.add((String)result.get("score"));
            iList.add((String)result.get("hosLevel"));
            iList.add((String)result.get("type"));
            iList.add((String)result.get("district"));
            iList.add((String)result.get("reg_count"));

            praise = Float.parseFloat(iList.get(2));
            appointment = Integer.parseInt(iList.get(6));

            if(strMyLatitude == null || strMyLongitude == null) {
                iList.add("暂无");
                distance = 0;
                iList.add(Double.toString(praise * scoreWeights + appointment * appointmentWeights -
                        distance * distanceWeights));
                mHosp.add(iList);
                continue;
            }
            String strDestLatitude = (String)result.get("lat");
            String strDestLongitude = (String)result.get("lot");
            if(strDestLatitude.equals("") || strDestLongitude.equals("")) {
                pendingUpdateLocation.add((String)result.get("hosAddress"));
                iList.add("暂无");
                distance = 0;
                iList.add(Double.toString(praise * scoreWeights + appointment * appointmentWeights -
                        distance * distanceWeights));
                mHosp.add(iList);
                continue;
            }
            double destLatitude = Double.parseDouble(strDestLatitude);
            double destLongitude = Double.parseDouble(strDestLongitude);
            LatLng dest = new LatLng(destLatitude, destLongitude);

            distance = DistanceUtil.getDistance(myLocation, dest);  // 计算当前位置和医院的距离

            if(distance != -1) {
                //iList.add(Double.toString(distance / 1000));
                iList.add(df.format(distance / 1000));  //转换为千米
            }
            else {
                iList.add("暂无");
            }

            iList.add(Double.toString(praise * scoreWeights + appointment * appointmentWeights -
                    distance * distanceWeights));

            mHosp.add(iList);
        }

        mLastHospList = mHosp;

        GeoCoderDecoder.getInstance().init(ShowHosListActivity.this);
        GeoCoderDecoder.getInstance().updateDistance(pendingUpdateLocation);  //更新医院坐标
    }
    private void clickedSelectRegion() {
        if(mSelectRegionPopupWindow == null) {
            //RelativeLayout r = new RelativeLayout(this);
            View  view = LayoutInflater.from(this).inflate(R.layout.select_region, null);
            lvSelectRegion = (ListView)view.findViewById(R.id.listView);
            selectRegionAdapter = new TextAdapter(this, mRegion,
                    R.drawable.choose_item_selected,
                    R.drawable.choose_eara_item_selector);
            selectRegionAdapter.setTextSize(18);
            lvSelectRegion.setAdapter(selectRegionAdapter);
            selectRegionAdapter.setOnItemClickListener(new TextAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    String name = selectRegionAdapter.getItem(position);
                    if(name != null && position != 0) {
                        showSelectedRegionList(name, position);
                        rbSelectHosp.setText(name);
                    }
                    else if(position == 0) {
                        hospAdapter = new HospAdapter(ShowHosListActivity.this, mHosp);
                        lvCityHospital.setAdapter(hospAdapter);
                        rbSelectHosp.setText("全部");
                        mLastHospList = mHosp;
                    }

                    mSelectRegionPopupWindow.dismiss();
                }
            });
            int displayHeight = 150 * mRegion.size();
            displayHeight = displayHeight < 900 ? displayHeight : 900;

            mSelectRegionPopupWindow = new PopupWindow(view, displayWidth, displayHeight);
            mSelectRegionPopupWindow.setAnimationStyle(R.style.PopupWindowAnimation);
            mSelectRegionPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
            mSelectRegionPopupWindow.setFocusable(false);
            mSelectRegionPopupWindow.setOutsideTouchable(true);
        }
        if(!mSelectRegionPopupWindow.isShowing()){
            mSelectRegionPopupWindow.showAsDropDown(findViewById(R.id.rb_select_hos));
        }
        else {
            return;
        }
    }

    private void clickedSortMethod() {
        if(mSortMethodPopupWindow == null) {
            //RelativeLayout r = new RelativeLayout(this);
            View  view = LayoutInflater.from(this).inflate(R.layout.select_region, null);
            lvSortMethod = (ListView)view.findViewById(R.id.listView);
            sortMethodAdapter = new TextAdapter(this, mSortMethod,
                    R.drawable.choose_item_selected,
                    R.drawable.choose_eara_item_selector);
            sortMethodAdapter.setTextSize(18);
            lvSortMethod.setAdapter(sortMethodAdapter);
            sortMethodAdapter.setOnItemClickListener(new TextAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    String method = sortMethodAdapter.getItem(position);
                    showSortedList(position);
                    rbSortMethod.setText(method);
                    mSortMethodPopupWindow.dismiss();
                }
            });
            int displayHeight = 150 * mSortMethod.size();
            displayHeight = displayHeight < 900 ? displayHeight : 900;

            mSortMethodPopupWindow = new PopupWindow(view, displayWidth, displayHeight);
            mSortMethodPopupWindow.setAnimationStyle(R.style.PopupWindowAnimation);
            mSortMethodPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
            mSortMethodPopupWindow.setFocusable(false);
            mSortMethodPopupWindow.setOutsideTouchable(true);
        }
        if(!mSortMethodPopupWindow.isShowing()){
            mSortMethodPopupWindow.showAsDropDown(findViewById(R.id.rb_sort_method));
        }
        else {
            return;
        }
    }

    private void clickedFilter() {
        if(mFilterPopupWindow == null) {
            //RelativeLayout r = new RelativeLayout(this);
            View  view = LayoutInflater.from(this).inflate(R.layout.select_region, null);
            lvFilter = (ListView)view.findViewById(R.id.listView);
            filterAdapter = new TextAdapter(this, mFilter,
                    R.drawable.choose_item_selected,
                    R.drawable.choose_eara_item_selector);
            filterAdapter.setTextSize(18);
            lvFilter.setAdapter(filterAdapter);
            filterAdapter.setOnItemClickListener(new TextAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    String filter = filterAdapter.getItem(position);
                    showFilterList(filter);
                    rbFilter.setText(filter);
                    mFilterPopupWindow.dismiss();
                }
            });
            int displayHeight = 150 * mFilter.size();
            displayHeight = displayHeight < 900 ? displayHeight : 900;

            mFilterPopupWindow = new PopupWindow(view, displayWidth, displayHeight);
            mFilterPopupWindow.setAnimationStyle(R.style.PopupWindowAnimation);
            mFilterPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
            mFilterPopupWindow.setFocusable(false);
            mFilterPopupWindow.setOutsideTouchable(true);
        }
        if(!mFilterPopupWindow.isShowing()){
            mFilterPopupWindow.showAsDropDown(findViewById(R.id.rb_filter));
        }
        else {
            return;
        }
    }

    private void showSelectedRegionList(String specificRegion, int pos) {
        mSelectedRegionHosp.clear();
        int size = mHosp.size();
        String hospRegion;
        for(int i = 0; i < size; i++) {
            hospRegion = mHosp.get(i).get(5);
            if(hospRegion.equals(specificRegion)) {
                mSelectedRegionHosp.add((ArrayList<String>)mHosp.get(i).clone());
            }
        }
        hospAdapter = new HospAdapter(this, mSelectedRegionHosp);
        lvCityHospital.setAdapter(hospAdapter);
        mLastHospList = mSelectedRegionHosp;
    }

    private void showSortedList(int pos) {
        switch(pos) {
            case 0:
                smartMethod();
                break;
            case 1:
                praiseMethod();
                break;
            case 2:
                appointmentMethod();
                break;
            case 3:
                distanceMethod();
                break;
            default:
                smartMethod();
                break;
        }

    }

    private void showFilterList(String specificType) {
        mFilterTypeHosp.clear();
        int size = mHosp.size();
        String hospType;
        for(int i = 0; i < size; i++) {
            hospType = mHosp.get(i).get(4);
            if(specificType.equals(hospType)) {
                mFilterTypeHosp.add((ArrayList<String>) mHosp.get(i).clone());
            }
        }
        hospAdapter = new HospAdapter(this, mFilterTypeHosp);
        lvCityHospital.setAdapter(hospAdapter);
        mLastHospList = mFilterTypeHosp;
    }

    /* 智能排序 */
    private void smartMethod() {
        realSort(8);
    }

    /* 好评优先 */
    private void praiseMethod() {
        realSort(2);
    }

    /* 预约量优先 */
    private void appointmentMethod() {
        realSort(6);
    }

    /* 距离优先 */
    private void distanceMethod() {
        realSort(7);
    }

    //@SuppressWarnings("Since15")
    private void realSort(final int method) {
        if(method >= 0 && method < 9 && !mLastHospList.isEmpty()) {
            Collections.sort(mLastHospList, new Comparator<ArrayList<String>>() {
                @Override
                public int compare(ArrayList<String> o1, ArrayList<String> o2) {
                    if(method == 7) {
                        if (!o1.get(method).equals("暂无") && !o1.get(method).equals("暂无") &&
                                !mLastHospList.isEmpty()) {
                            return (int) (Float.parseFloat(o1.get(7)) * 10 - Float.parseFloat(o2.get(7)) * 10);
                        } else {
                            return 0;
                        }
                    } else {
                        return (int) (Float.parseFloat(o2.get(method)) * 10 - Float.parseFloat(o1.get(method)) * 10);
                    }
                }
            });
            hospAdapter.notifyDataSetChanged();
        }
    }
    // 刷新
    private void refreshHosList() {
        List<Param> params = new ArrayList<Param>();

        params.add(new Param("address", CurrentHosInfo.getCurCity()));

        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.CHOOSECITY, new OkHttpManager.HttpCallBack() {
            @Override
            public void onResponse(JSONObject json) {
                try {
                    int code = json.getInteger("code");
                    if (code == 1000) {
                        Map<String, Object> map = json.getObject("result", Map.class);

                        JSONArray hi = (JSONArray) map.get("HosInfo");
                        ResultFromServer.initFixedCityHos();
                        ResultFromServer.getFixedCityHos().clear();
                        for (int i = 0; i < hi.size(); i++) {
                            Map<String, Object> n = hi.getObject(i, Map.class);
                            ResultFromServer.addFixedCityHos(n);
                        }
                        readDataFromServer();
                    }
                    else {
                        Toast.makeText(ShowHosListActivity.this,
                                "未知错误", Toast.LENGTH_SHORT)
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
    private void enterDetailPage(int pos) {
        List<Param> params = new ArrayList<Param>();

        String hosName = null;
        try {
            hosName = ((ArrayList<String>)hospAdapter.getItem(pos)).get(1);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        params.add(new Param("hosname", hosName));
        OkHttpManager.init(ShowHosListActivity.this);
        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.ENTERDETAILPAGE, new OkHttpManager.HttpCallBack() {
            @Override
            public void onResponse(JSONObject json) {
                try {
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
                            startActivity(new Intent(ShowHosListActivity.this, HosDetailInfoActivity.class));
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        Toast.makeText(ShowHosListActivity.this,
                                "未知错误", Toast.LENGTH_SHORT)
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
}
