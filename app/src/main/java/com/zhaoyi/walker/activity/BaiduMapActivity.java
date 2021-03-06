package com.zhaoyi.walker.activity;

import android.app.Activity;
import android.os.Bundle;

import com.baidu.mapapi.map.MapView;
import com.zhaoyi.walker.R;


/**
 * 演示MapView的基本用法
 */
public class BaiduMapActivity extends Activity {
    MapView mMapView = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_baidu_map);
        //获取地图控件引用

        mMapView = (MapView) findViewById(R.id.bmapView);
    }
    private void setLocation() {

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
}
