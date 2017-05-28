package com.zhaoyi.walker;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;

import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.SDKInitializer;
import com.zhaoyi.walker.utils.LocalUserUtil;
import com.zhaoyi.walker.utils.OkHttpManager;

import java.util.Random;

/**
 * Created by jianjia Liu on 2017/4/3.
 */

public class ZyApplication extends Application {
    public static Context applicationContext;
    private static ZyApplication instance;
    // activity_login user name
    public final String PREF_USERNAME = "username";
    private JSONObject userJson;
    private String time="";

    private DisplayMetrics displayMetrics = null;
    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        instance = this;
        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);
        OkHttpManager.init(this);
        LocalUserUtil.init(this);
        //GeoCoderDecoder.init(this);
    }

    public static ZyApplication getApp() {
        if (instance != null && instance instanceof ZyApplication) {
            return (ZyApplication) instance;
        } else {
            instance = new ZyApplication();
            instance.onCreate();
            return (ZyApplication) instance;
        }
    }
    public static boolean sRunningOnIceCreamSandwich;

    static {
        sRunningOnIceCreamSandwich = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static ZyApplication getInstance() {
        return instance;
    }

    public  void setUserJson( JSONObject userJson){

        this.userJson=userJson;
        LocalUserUtil.getInstance().setUserJson(userJson);

    }
    public  JSONObject getUserJson(){
        if(userJson==null){
            userJson=LocalUserUtil.getInstance().getUserJson();
        }
        return  userJson;
    }
    public String getTime(){
        return time;

    }
    public void setTime(String time){
        this.time=time;
    }

    public static int getRandomStreamId() {
        Random random = new Random();
        int randint =(int)Math.floor((random.nextDouble()*10000.0 + 10000.0));
        return randint;
    }

    public float getScreenDensity() {
        if (this.displayMetrics == null) {
            setDisplayMetrics(getResources().getDisplayMetrics());
        }
        return this.displayMetrics.density;
    }

    public int getScreenHeight() {
        if (this.displayMetrics == null) {
            setDisplayMetrics(getResources().getDisplayMetrics());
        }
        return this.displayMetrics.heightPixels;
    }

    public int getScreenWidth() {
        if (this.displayMetrics == null) {
            setDisplayMetrics(getResources().getDisplayMetrics());
        }
        return this.displayMetrics.widthPixels;
    }

    public void setDisplayMetrics(DisplayMetrics DisplayMetrics) {
        this.displayMetrics = DisplayMetrics;
    }

    public int dp2px(float f)
    {
        return (int)(0.5F + f * getScreenDensity());
    }

    public int px2dp(float pxValue) {
        return (int) (pxValue / getScreenDensity() + 0.5f);
    }

    //获取应用的data/data/....File目录
    public String getFilesDirPath() {
        return getFilesDir().getAbsolutePath();
    }

    //获取应用的data/data/....Cache目录
    public String getCacheDirPath() {
        return getCacheDir().getAbsolutePath();
    }
}
