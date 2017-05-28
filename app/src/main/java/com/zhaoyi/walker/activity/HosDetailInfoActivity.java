package com.zhaoyi.walker.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.baidu.mapapi.model.LatLng;
import com.bumptech.glide.Glide;
import com.zhaoyi.walker.Fragment.HosDepartmentFragment;
import com.zhaoyi.walker.Fragment.HosServiceFragment;
import com.zhaoyi.walker.Fragment.HosSimpleInfoFragment;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.custom.CustomProgressDialog;
import com.zhaoyi.walker.location.MultiRoutePlan;
import com.zhaoyi.walker.model.CurrentHosInfo;
import com.zhaoyi.walker.model.ResultFromServer;
import com.zhaoyi.walker.utils.GeoCoderDecoder;
import com.zhaoyi.walker.utils.GeoType;
import com.zhaoyi.walker.utils.MyContants;

import java.util.Map;


public class HosDetailInfoActivity extends FragmentActivity {
    private ImageView ivHosPic;
    private TextView tvHosName;
    private TextView tvHosAddr;
    private ImageView ivHosBack;
    private TextView tvHosDetailPage;
    private TextView tvStars;
    private TextView tvLevel;
    private TextView tvType;
    private TextView tvAppointment;
    private RadioButton rbHosSimpleInfo;
    private RadioButton rbHosService;
    private RadioButton rbHosDepartment;
    private RadioGroup radioGroup;
    private FragmentManager mFragmentManager;
    private Resources resource;
    private ColorStateList cs0;
    private ColorStateList cs1;
    private ImageView ivHosLocation;
    private LatLng location;

    private HosSimpleInfoFragment hosSimpleInfoFragment;
    private HosServiceFragment hosServiceFragment;
    private HosDepartmentFragment hosDepartmentFragment;

    //CustomProgressDialog pg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hosp_detail_info);

        mFragmentManager = getSupportFragmentManager();
        ivHosPic = (ImageView)findViewById(R.id.iv_hosp_pic);
        tvHosName = (TextView)findViewById(R.id.tv_hosp_name);
        tvHosAddr = (TextView)findViewById(R.id.tv_hosp_region);
        ivHosBack = (ImageView)findViewById(R.id.iv_top_back);
        tvHosDetailPage = (TextView)findViewById(R.id.tv_top_title);
        ivHosLocation = (ImageView)findViewById(R.id.iv_hosp_location);
        tvStars = (TextView) findViewById(R.id.tv_hosp_star);
        tvLevel = (TextView) findViewById(R.id.tv_hosp_level);
        tvType = (TextView) findViewById(R.id.tv_hosp_type);
        tvAppointment = (TextView) findViewById(R.id.tv_hosp_appointment);

        rbHosSimpleInfo = (RadioButton)findViewById(R.id.rb_hos_simple_info);
        rbHosService = (RadioButton)findViewById(R.id.rb_hos_service);
        rbHosDepartment = (RadioButton)findViewById(R.id.rb_hos_department);
        radioGroup = (RadioGroup)findViewById(R.id.rg_hos);

        rbHosSimpleInfo.setChecked(true);

        resource = getBaseContext().getResources();
        cs0 = resource.getColorStateList(R.color.gray1) ;
        cs1 = resource.getColorStateList(R.color.theme_default_color);



        Map<String, Object> result = ResultFromServer.getHosInfo().get(0);
        JSONArray hosList = (JSONArray) result.get("offinfo");
        int size = hosList.size();
        ResultFromServer.initDepartment();
        for(int i = 0; i < size; i++) {
            ResultFromServer.getDepartment().add((Map<String, Object>)hosList.get(i));
        }

        Glide.with(HosDetailInfoActivity.this)
                .load(MyContants.BASE_URL + result.get("image"))
                .placeholder(R.drawable.zy_default_hospital)
                .into(ivHosPic);
        tvHosName.setText((String)result.get("hosName"));
        tvLevel.setText((String) result.get("hosLevel"));
        tvType.setText((String) result.get("type"));
        tvStars.setText((String) result.get("score"));
        tvAppointment.setText((String) result.get("reg_count"));
        tvHosAddr.setText((String) result.get("hosAddress"));

        String strLatitude = (String)result.get("lat");
        String strLongitude = (String)result.get("lot");
        if(!strLatitude.equals("") && !strLongitude.equals("")) {
            location = new LatLng(Double.parseDouble(strLatitude), Double.parseDouble(strLongitude));
        }
        else {
            location = null;
        }
        tvHosDetailPage.setText("医院主页");
        ivHosBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        /* 设置当前医院信息 */
        CurrentHosInfo.setCurHos((String)tvHosName.getText());
        //CurrentHosInfo.setCurCost((String)result.get("hosCost"));
        CurrentHosInfo.setCurHosAddress((String)result.get("hosAddress"));

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.rb_hos_department:
                        //setTabSelection(2);
                        replaceHosDepartmentFragment();
                        rbHosDepartment.setTextColor(cs1.getDefaultColor());
                        rbHosSimpleInfo.setTextColor(cs0.getDefaultColor());
                        rbHosService.setTextColor(cs0.getDefaultColor());
                        break;
                    case R.id.rb_hos_service:
                        //setTabSelection(1);
                        replaceHosServiceFragment();
                        rbHosService.setTextColor(cs1.getDefaultColor());
                        rbHosSimpleInfo.setTextColor(cs0.getDefaultColor());
                        rbHosDepartment.setTextColor(cs0.getDefaultColor());
                        break;
                    case R.id.rb_hos_simple_info:
                        //setTabSelection(0);
                        replaceHosSimpleInfoFragment();
                        rbHosSimpleInfo.setTextColor(cs1.getDefaultColor());
                        rbHosService.setTextColor(cs0.getDefaultColor());
                        rbHosDepartment.setTextColor(cs0.getDefaultColor());
                        break;
                    default:
                        break;
                }
            }
        });
        findViewById(R.id.rl_hosp_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(location != null) {
                    Intent intent = new Intent(HosDetailInfoActivity.this, MultiRoutePlan.class);
                    intent.putExtra("destLoc", location);
                    startActivity(intent);
                } else {
                    Object[] obj = {CurrentHosInfo.getCurHosAddress()};
                    CustomProgressDialog pg = new CustomProgressDialog(HosDetailInfoActivity.this);
                    //geoCodeDecoder(obj, GeoType.Code);
                    GeoCoderDecoder.init(HosDetailInfoActivity.this);
                    GeoCoderDecoder.getInstance().getLocation(obj, GeoType.Code, pg);

                    //pg.setText("正在加载");
                    pg.show();
                }
            }
        });

        //启动第一个Fragment
        startHosSimpleInfoFragment();
    }

    private void startHosSimpleInfoFragment() {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        HosSimpleInfoFragment hosSimpleInfoFragment = new HosSimpleInfoFragment();
        ft.add(R.id.ll_ui_container, hosSimpleInfoFragment, "HosSimpleInfoFragment");
        //ft.addToBackStack("HosSimpleInfoFragment");
        ft.commit();
    }
    private void setTabSelection(int i) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        hideFragments(transaction);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        switch (i) {
            case 0:
                if (hosSimpleInfoFragment == null) {
                    hosSimpleInfoFragment = new HosSimpleInfoFragment();
                    transaction.add(R.id.ll_ui_container, hosSimpleInfoFragment, "hosSimpleInfoFragment");
                } else {
                    transaction.show(hosSimpleInfoFragment);
                }
                break;
            case 1:
                if (hosServiceFragment == null) {
                    hosServiceFragment = new HosServiceFragment();
                    transaction.add(R.id.ll_ui_container, hosServiceFragment, "hosServiceFragment");
                } else {
                    transaction.show(hosServiceFragment);
                }
                break;
            case 2:
                if (hosDepartmentFragment == null) {
                    hosDepartmentFragment = new HosDepartmentFragment();
                    transaction.add(R.id.ll_ui_container, hosDepartmentFragment, "hosDepartmentFragment");
                } else {
                    transaction.show(hosDepartmentFragment);
                }
                break;
        }
        transaction.commit();
    }
    private void hideFragments(FragmentTransaction transaction) {
        if (hosSimpleInfoFragment != null && hosSimpleInfoFragment.isVisible()) {
            transaction.hide(hosSimpleInfoFragment);
        }
        if (hosServiceFragment != null && hosServiceFragment.isVisible()) {
            transaction.hide(hosServiceFragment);
        }
        if (hosDepartmentFragment != null && hosDepartmentFragment.isVisible()) {
            transaction.hide(hosDepartmentFragment);
        }
    }

    private void replaceHosSimpleInfoFragment() {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.ll_ui_container, new HosSimpleInfoFragment(), "HosSimpleInfoFragment");
        ft.commit();
    }
    private void replaceHosServiceFragment() {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.ll_ui_container, new HosServiceFragment(), "HosServiceFragment");
        ft.commit();
    }
    private void replaceHosDepartmentFragment() {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.ll_ui_container, new HosDepartmentFragment(), "HosDepartmentFragment");
        ft.commit();
    }

    public void popAllFragments() {
        for (int i = 0, count = mFragmentManager.getBackStackEntryCount(); i < count; i++) {
            mFragmentManager.popBackStack();
        }
    }

    //点击返回按钮
	@Override
	public void onBackPressed() {
		super.onBackPressed();
        popAllFragments();
        finish();
	}
}
