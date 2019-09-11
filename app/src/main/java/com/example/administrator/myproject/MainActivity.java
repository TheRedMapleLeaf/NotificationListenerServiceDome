package com.example.administrator.myproject;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.myproject.Util.Constants;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "MainActivity";
    private TextView tvMsg;
    private ImageView iconMenu;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NotificationBroadcastReceiver notificationBroadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化窗口属性，让状态栏和导航栏透明
        if(Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        tvMsg = findViewById(R.id.msg);
        drawerLayout = findViewById(R.id.activity_na);
        navigationView = findViewById(R.id.nav);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                for (int i=0;i<navigationView.getMenu().size();i++){
                    navigationView.getMenu().getItem(i).setChecked(false);
                }
                menuItem.setCheckable(true);//设置选项可选
//                drawerLayout.closeDrawers();//关闭侧边菜单栏
                return true;
            }
        });
        Resources resource=(Resources)getBaseContext().getResources();
        @SuppressLint("ResourceType") ColorStateList csl=(ColorStateList)resource.getColorStateList(R.drawable.navigation_menu_item_color);
        navigationView.setItemTextColor(csl);
        navigationView.setItemIconTintList(csl);
        iconMenu = findViewById(R.id.menu);
        iconMenu.setOnClickListener(this);
        if (!isNotificationListenerEnabled(this)){
            AlertDialog enableNotificationListenerDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerDialog.show();
        }
        ensureServiceIsRunning();
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcastReceiver();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*注销动态广播*/
        unregisterReceiver(notificationBroadcastReceiver);
    }
    /**
     *  注册动态广播
     */
    private void registerBroadcastReceiver(){
        // 1. 实例化BroadcastReceiver子类 &  IntentFilter
        notificationBroadcastReceiver = new NotificationBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(Constants.BROADCAST_NOTIFICATION_POSTED_ACTION);
        intentFilter.addAction("NotificationBroadcastReceiver");
        // 3. 动态注册：调用Context的registerReceiver（）方法
        LocalBroadcastManager.getInstance(this).registerReceiver(notificationBroadcastReceiver,intentFilter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.menu:
                if (drawerLayout.isDrawerOpen(navigationView)){
                    drawerLayout.closeDrawer(navigationView);
                }else{
                    drawerLayout.openDrawer(navigationView);
                }
                break;
        }
    }

    /**
     * 广播接收器
     */
    public class NotificationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String packageName = intent.getStringExtra("packageName");
            String title = intent.getStringExtra("title");
            String content = intent.getStringExtra("content");
            tvMsg.append("接收包名："+packageName+" 标题："+title+" 内容："+content);
        }
    }
    /**
     * 确保NotificationListenerService在后台运行，所以通过判断服务是否在运行中的服务中来进行触发系统rebind操作
     */
    private void ensureServiceIsRunning(){
        ComponentName serviceComponent = new ComponentName(this, NotificationCollextorService.class);
        Log.d(TAG, "确保服务NotificationListenerExampleService正在运行");
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean isRunning = false;
        List<ActivityManager.RunningServiceInfo> runningServiceInfos = manager.getRunningServices(Integer.MAX_VALUE);
        if (runningServiceInfos == null) {
            Log.w(TAG, "运行中的服务为空");
            return;
        }
        for (ActivityManager.RunningServiceInfo serviceInfo : runningServiceInfos) {
            if (serviceInfo.service.equals(serviceComponent)){
                Log.w(TAG, "ensureServiceRunning service - pid: "+serviceInfo.pid
                        +",currentPID: " + Process.myPid()
                        +", clientPackage: "+serviceInfo.clientPackage
                        +", clientCount: " +serviceInfo.clientCount
                        +", clientLabel: "+((serviceInfo.clientLabel==0)?"0":"(" +getResources().getString(serviceInfo.clientLabel)+")"));
                if (serviceInfo.pid == Process.myPid()) {
                    isRunning = true;
                }
            }
        }
        if (isRunning) {
            Log.d(TAG, "ensureServiceIsRunning: 监听服务正在运行");
            return;
        }
        Log.d(TAG, "ensureServiceIsRunning: 服务没有运行，重启中...");
        toggleNotificationListenerService();
    }
    /**
     * 检测通知监听服务是否被授权
     */
    public boolean isNotificationListenerEnabled(Context context) {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName());
    }
    /**
     * 打开通知监听设置页面
     */
    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.app_name);
        alertDialogBuilder.setMessage(R.string.start_notification_monitor);
        alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this,"不授予通知读取权限Monitor将无法运行！",Toast.LENGTH_SHORT)
                        .show();
            }
        });
        return alertDialogBuilder.create();
    }
    /**
     * 把应用的NotificationListenerService实现类disable再enable，即可触发系统rebind操作
     */
    private void toggleNotificationListenerService() {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(this, com.example.administrator.myproject.NotificationCollextorService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(
                new ComponentName(this, com.example.administrator.myproject.NotificationCollextorService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

}
