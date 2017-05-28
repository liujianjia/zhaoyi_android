package com.zhaoyi.walker.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.zhaoyi.walker.location.MultiRoutePlan;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jianjia Liu on 2017/4/11.
 */

public class GeoCoderDecoder {
    private static Context mContext;
    private static GeoCoder geoCoder;
    private static GeoCoderDecoder geoCoderDecoder;

    public void GeoCoderDecoder(Context context) {
        mContext = context;
        geoCoder = GeoCoder.newInstance();
    }
    public static void init(Context context) {
        if(geoCoderDecoder == null) {
            geoCoderDecoder = new GeoCoderDecoder();
        }
        mContext = context;
        if(geoCoder == null) {
            geoCoder = GeoCoder.newInstance();
        }
    }
    public static GeoCoderDecoder getInstance() {
        return geoCoderDecoder;
    }
    public void getLocation(Object[] args, GeoType type, final ProgressDialog pg) {

        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                pg.dismiss();
                if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(mContext, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                Intent intent = new Intent(mContext, MultiRoutePlan.class);
                intent.putExtra("destLoc", geoCodeResult.getLocation());
                mContext.startActivity(intent);
                //String strInfo = String.format("纬度：%f 经度：%f",
                        //geoCodeResult.getLocation().latitude, geoCodeResult.getLocation().longitude);
                //Toast.makeText(mContext, strInfo, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                pg.dismiss();
                if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(mContext, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                Intent intent = new Intent(mContext, MultiRoutePlan.class);
                intent.putExtra("destLoc", reverseGeoCodeResult.getLocation());
                mContext.startActivity(intent);
                //Toast.makeText(mContext, reverseGeoCodeResult.getAddress(),
                        //Toast.LENGTH_LONG).show();
            }
        });
        if(type == GeoType.Code) {
            String[] str = ((String)args[0]).split("(省|市)");
            geoCoder.geocode(new GeoCodeOption().city(str[1]).address(str[2]));
        }
        else if(type == GeoType.Decode) {
            geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(new LatLng((float)args[0], (float)args[1])));
        }
    }
    public void updateDistance(ArrayList<String> address) {
        for(int i = 0; i < address.size(); i++) {
            String[] str = address.get(i).split("(省|市)");
            geoCoder.geocode(new GeoCodeOption().city(str[1]).address(str[2]));
        }
        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

                if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(mContext, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                //Double dis = DistanceUtil.getDistance(myLocation, geoCodeResult.getLocation());
                /* 更新医院坐标 */
                List<Param> params = new ArrayList<>();
                params.add(new Param("address", geoCodeResult.getAddress()));
                params.add(new Param("latitude", Double.toString(geoCodeResult.getLocation().latitude)));
                params.add(new Param("longitude", Double.toString(geoCodeResult.getLocation().longitude)));
                OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.UPDATELOCATION,
                        new OkHttpManager.HttpCallBack() {
                            @Override
                            public void onResponse(JSONObject json) {

                            }

                            @Override
                            public void onFailure(String errorMsg) {

                            }
                        });

            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(mContext, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                            .show();
                    return;
                }

                Toast.makeText(mContext, reverseGeoCodeResult.getAddress(),
                        Toast.LENGTH_LONG).show();
            }
        });

    }

}
