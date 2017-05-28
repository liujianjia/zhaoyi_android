package com.zhaoyi.walker.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zhaoyi.walker.service.CheckOrderService;

/**
 * Created by jianjia Liu on 2017/5/9.
 */

public class TaskAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startIntent = new Intent(context, CheckOrderService.class);
        context.startService(startIntent);
    }
}
