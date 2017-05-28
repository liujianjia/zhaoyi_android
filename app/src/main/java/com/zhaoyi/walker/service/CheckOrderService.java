package com.zhaoyi.walker.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.ZyApplication;
import com.zhaoyi.walker.activity.MyAppointmentActivity;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.OkHttpManager;
import com.zhaoyi.walker.utils.Param;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by jianjia Liu on 2017/5/9.
 */

public class CheckOrderService extends Service {
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        alarmManager  = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        String ALARM_ACTION = MyContants.ACTION_CHECK_ORDER_ALARM;
        Intent intentToFire = new Intent(ALARM_ACTION);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String isAutoCheck = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.ISREMIND);

        if(!TextUtils.isEmpty(isAutoCheck) && isAutoCheck.equals("1")) {  // 已开启挂号提醒
            int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
            long timeToRefresh = SystemClock.elapsedRealtime() + 24 * 60 * 60 * 1000;  // 设置重复周期
            alarmManager.setInexactRepeating(alarmType, timeToRefresh, 24 * 60 * 60 * 1000, alarmIntent);
        } else {
            alarmManager.cancel(alarmIntent);
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                onCheckOrderStatus();
            }
        });
        t.start();

        return START_NOT_STICKY;
    }

    private void onCheckOrderStatus() {
        try {
            queryOnServer();
        }
        finally {
            stopSelf();
        }
    }

    /**
     * 查询当前用户的所有订单
     */
    private void queryOnServer() {
        String userId = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.JSON_KEY_ZYID);
        if(TextUtils.isEmpty(userId)) {
            Log.e("Error: ", "userId can't be empty on checkOderStatus.");
            return;
        }
        List<Param> params = new ArrayList<>();
        params.add(new Param("userid", userId));

        OkHttpManager.getInstance().post(params, MyContants.BASE_URL + MyContants.GETORDER,
                new OkHttpManager.HttpCallBack() {
                    @Override
                    public void onResponse(JSONObject json) {
                        int code = json.getInteger("code");
                        if(code == 1000) {
                            Map<String, Object> result = json.getObject("result", Map.class);
                            JSONArray array = (JSONArray)result.get("OrderInfo");

                            int size = array.size();
                            ArrayList<String> mData = new ArrayList<>();
                            for(int i =  0; i < size; i++) {
                                Map<String, String> m = array.getObject(i, Map.class);
                                mData.add(m.get("visitTime"));
                            }

                            ArrayList<Date> mDate = new ArrayList<>();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            for(String s : mData) {
                                try {
                                    mDate.add(sdf.parse(s));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            compareOrderDate(mDate);
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {

                    }
                });
    }

    /**
     *
     * @param date 所有订单的预约时间
     *
     */
    private void compareOrderDate(ArrayList<Date> date) {
        Date nowDate = new Date(); // 当前时间

        for(Date d : date) {
            long isValidDate = (d.getTime() - nowDate.getTime()) / 1000 / 60 / 60;
            if(isValidDate > 0 && isValidDate < 24) {  // 如果预约时间跟当前时间相差一天
                String text = "预约时间 " + d.toString() + "请及时就诊";
                sendNotification(text);
            }
        }
    }

    private void sendNotification(String showText) {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        long when = System.currentTimeMillis();

        int NOTIFICATION_REF = 1;
        Intent intent = new Intent(this, MyAppointmentActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
		Notification.Builder builder = new Notification.Builder(this);
		builder.setSmallIcon(R.drawable.zy_notification_icon)
				.setWhen(when)
				.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
				.setVibrate(new long[] { 500, 500, 500, 500, 500, 500})
				.setLights(Color.WHITE, 1, 0)
				.setContentText(showText)
				.setContentIntent(pendingIntent);

		Notification notification = builder.getNotification();
		notificationManager.notify(NOTIFICATION_REF, notification);
    }
}
