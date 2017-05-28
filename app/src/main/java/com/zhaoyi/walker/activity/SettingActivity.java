package com.zhaoyi.walker.activity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.zhaoyi.walker.R;
import com.zhaoyi.walker.ZyApplication;
import com.zhaoyi.walker.service.UpdateService;
import com.zhaoyi.walker.update.UpdateInfo;
import com.zhaoyi.walker.update.UpdateInfoParse;
import com.zhaoyi.walker.utils.MyContants;
import com.zhaoyi.walker.utils.NetworkUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jianjia Liu on 2017/4/10.
 */

public class SettingActivity extends FragmentActivity implements View.OnClickListener {
    private Context context;
    private Switch sRemind;

    private final String TAG = this.getClass().getName();
    private final int UPDATA_NONEED = 0;
    private final int UPDATA_CLIENT = 1;
    private final int GET_UNDATAINFO_ERROR = 2;
    private final int SDCARD_NOMOUNTED = 3;
    private final int DOWN_ERROR = 4;
    private UpdateInfo info;
    private String localVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        context = SettingActivity.this;
        sRemind = (Switch) findViewById(R.id.s_remind);
        findViewById(R.id.btn_logout).setOnClickListener(this);
        findViewById(R.id.iv_top_back).setOnClickListener(this);
        findViewById(R.id.re_update_password).setOnClickListener(this);
        findViewById(R.id.re_about).setOnClickListener(this);
        findViewById(R.id.re_help).setOnClickListener(this);
        findViewById(R.id.re_advice).setOnClickListener(this);
        findViewById(R.id.re_check_version).setOnClickListener(this);
        ((TextView)findViewById(R.id.tv_top_title)).setText("设置");

        String isRemind = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.ISREMIND);
        if(!TextUtils.isEmpty(isRemind) && isRemind.equals("1")) {
            sRemind.setChecked(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_logout:
                logout();
                break;
            case R.id.iv_top_back:
                goBack();
                break;
            case R.id.re_update_password:
                startActivity(new Intent(context, RecoverPasswordActivity.class));
                break;
            case R.id.re_about:
                startActivity(new Intent(context, AboutActivity.class));
                break;
            case R.id.re_help:
                Intent intent = new Intent(SettingActivity.this, WebViewActivity.class);
                intent.putExtra("url", MyContants.BASE_URL + "help/faq.html");
                startActivity(intent);
                break;
            case R.id.re_advice:
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("反馈")
                        .setMessage("请发邮件至：gxzpljj@163.com")
                        .show();
                break;
            case R.id.re_check_version:
                try {
                    localVersion = getVersionName();
                    CheckVersionTask cv = new CheckVersionTask();
                    new Thread(cv).start();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            default:
                break;
        }
    }
    private void logout() {
        String isLogin = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.JSON_KEY_ISLOGIN);
        if(TextUtils.isEmpty(isLogin) || !isLogin.equals("0")) {
            JSONObject userJson = ZyApplication.getInstance().getUserJson();
            userJson.put(MyContants.JSON_KEY_ISLOGIN, "0");
            ZyApplication.getInstance().setUserJson(userJson);
        }
        Log.i("Id: ", ZyApplication.getInstance()
            .getUserJson()
            .getString(MyContants.JSON_KEY_ZYID) + " has been logout.");

        setResult(RESULT_OK);
        //startActivity(new Intent(SettingActivity.this, LoginActivity.class));
        finish();
    }
    private void goBack() {
        String isRemind = ZyApplication.getInstance()
                .getUserJson()
                .getString(MyContants.ISREMIND);

        if(sRemind.isChecked()) {
            if(TextUtils.isEmpty(isRemind) || !isRemind.equals("1")) {
                JSONObject userJson = ZyApplication.getInstance()
                        .getUserJson();
                userJson.put(MyContants.ISREMIND, "1");
                ZyApplication.getInstance().setUserJson(userJson);

                setRemindAlarm(true);  // 开启挂号提醒
            }
        } else {
            if(TextUtils.isEmpty(isRemind) || isRemind.equals("1")) {
                JSONObject userJson = ZyApplication.getInstance()
                        .getUserJson();
                userJson.put(MyContants.ISREMIND, "0");
                ZyApplication.getInstance().setUserJson(userJson);

                setRemindAlarm(false);  //关闭挂号提醒
            }
        }
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            goBack();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setRemindAlarm(boolean isOpen) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        String ALARM_ACTION = MyContants.ACTION_CHECK_ORDER_ALARM;
        Intent intentToFire = new Intent(ALARM_ACTION);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0);
        int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
        long timeToRefresh = SystemClock.elapsedRealtime() + 60 * 1000;

        if(isOpen) {
            alarmManager.set(alarmType, timeToRefresh, alarmIntent);
        } else {
            alarmManager.cancel(alarmIntent);
        }
    }

    /**
     * 后台下载更新
     */
    public void update() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                //启动服务
                Intent service = new Intent(SettingActivity.this,UpdateService.class);
                startService(service);
            }
        }).start();

    }

    /**
     *  获取当前应用的版本
     * @return
     * @throws Exception
     */
    private String getVersionName() throws Exception {
        //getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageManager packageManager = getPackageManager();
        PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),
                0);
        return packInfo.versionName;
    }

    public class CheckVersionTask implements Runnable {
        InputStream is;
        public void run() {
            try {
                String path = MyContants.BASE_URL + MyContants.VERSIONURL;
                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    // 从服务器获得一个输入流
                    is = conn.getInputStream();
                }
                info = UpdateInfoParse.getUpdateInfo(is);
                if (info.getVersion().equals(localVersion)) {
                    Log.i(TAG, "版本号相同");
                    Message msg = new Message();
                    msg.what = UPDATA_NONEED;
                    handler.sendMessage(msg);
                    // LoginMain();
                } else {
                    Log.i(TAG, "版本号不相同 ");
                    Message msg = new Message();
                    msg.what = UPDATA_CLIENT;
                    handler.sendMessage(msg);
                }
            } catch (Exception e) {
                Message msg = new Message();
                msg.what = GET_UNDATAINFO_ERROR;
                handler.sendMessage(msg);
                e.printStackTrace();
            }
        }
    }
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATA_NONEED:
                    Toast.makeText(context, "不需要更新",
                            Toast.LENGTH_SHORT).show();
                case UPDATA_CLIENT:
                    //对话框通知用户升级程序
                    showUpdateDialog();
                    break;
                case GET_UNDATAINFO_ERROR:
                    //服务器超时
                    Toast.makeText(context, "获取服务器更新信息失败", Toast.LENGTH_SHORT).show();
                    break;
                case DOWN_ERROR:
                    //下载apk失败
                    Toast.makeText(context, "下载新版本失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /*
	 * 弹出对话框通知用户更新程序
	 *
	 * 弹出对话框的步骤：
	 *  1.创建alertDialog的builder.
	 *  2.要给builder设置属性, 对话框的内容,样式,按钮
	 *  3.通过builder 创建一个对话框
	 *  4.对话框show()出来
	 */
    protected void showUpdateDialog() {
        AlertDialog.Builder builer = new AlertDialog.Builder(context);
        builer.setTitle("版本升级");
        builer.setMessage(info.getDescription());
        //当点确定按钮时从服务器上下载 新的apk 然后安装   װ
        builer.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if(NetworkUtils.isWifi(context)) {
                    update();
                    Log.i(TAG, "下载apk,更新");
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("提示")
                            .setMessage("当前为非Wifi网络，确定要下载吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    update();
                                    Log.i(TAG, "下载apk,更新");
                                }
                            })
                            .setPositiveButton("取消", null)
                            .show();
                }
            }
        });
        builer.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                //do sth
            }
        });
        builer.setMessage("检测到新版本，是否更新？");
        AlertDialog dialog = builer.create();
        dialog.show();
    }
}