package com.example.administrator.myproject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.administrator.myproject.Util.Constants;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationCollextorService extends NotificationListenerService {
    private String TAG = "NotificationCollextorService";
    private Bundle bundle;
    private Notification notification;
    private String title,content;
    private int count = 0;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "开启服务：NotificationCollextorService");
    }
    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"监听服务被销毁");
    }
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        notification = sbn.getNotification();
        if (notification == null){
            return;
        }
        bundle = notification.extras;
        if (bundle != null){
            title = bundle.getString(Notification.EXTRA_TITLE,"");
            content = bundle.getString(Notification.EXTRA_TEXT,"");
            Log.e("接收包名：",sbn.getPackageName()+" 标题："+title+" 内容："+content);
            count++;
//           发送动态广播
            Intent localIntent = new Intent(Constants.BROADCAST_NOTIFICATION_POSTED_ACTION);
            localIntent.putExtra("packageName",sbn.getPackageName());
            localIntent.putExtra("title",title);
            localIntent.putExtra("content",content+"<"+count+">");
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        notification = sbn.getNotification();
        if (notification == null){
            return;
        }
        bundle = notification.extras;
        if (bundle != null){
            title = bundle.getString(Notification.EXTRA_TITLE,"");
            content = bundle.getString(Notification.EXTRA_TEXT,"");
            Log.e("删除包名：",sbn.getPackageName()+" 标题："+title+" 内容："+content);
//           发送动态广播
            Intent localIntent = new Intent(Constants.BROADCAST_NOTIFICATION_REMOVED_ACTION);
            localIntent.putExtra("packageName",sbn.getPackageName());
            localIntent.putExtra("title",title);
            localIntent.putExtra("content",content);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }
}
