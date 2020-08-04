package com.example.javaproject.AppLock;

import android.app.Notification;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.javaproject.Misc.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class AppService extends Service {

    private BroadcastReceiver receiver;
    Context mContext;
    private List<String> mPackageNames;
    private List<AppModel> mApps;
    private SharedPreferencesManager sharedPreferencesManager;
    private int flag = 0;

    private String PACKAGE_NAME = null;
    private String APP_TITLE = null;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
        sharedPreferencesManager = new SharedPreferencesManager(this);

        mPackageNames = new ArrayList<>();

        getApps();
        //System.out.println(needPermissionForBlocking(mContext));
//
//        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
//            getForgroundTask();
//        } else {
//            StartForground(1, new Notification());
//        }

    }

    public void stopService() {
//        timer.cancel();
        stopService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        //stopForeground(true);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        // startForeground(1338, buildForegroundNotification("test"));
        timer();
        System.out.println("ON START");
        return START_STICKY;
    }

    private void getApps() {

        mPackageNames = new ArrayList<>();

        mApps = new ArrayList<>();

        PackageManager pm = getPackageManager();
        List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
        for (PackageInfo pack : packages) {
            if ((pm.getLaunchIntentForPackage(pack.packageName) != null)) {
                AppModel app = new AppModel();

                app.name = pack.applicationInfo.loadLabel(getPackageManager()).toString();
                app.packName = pack.applicationInfo.packageName;
                app.icon = pack.applicationInfo.loadIcon(getPackageManager());

                mApps.add(app);

            }
        }


    }

    //Collect all locked apps
    private void Update() {

        for (AppModel app : mApps) {

            String packageName = app.packName;

            //If sharedPref is true, then app is intended to be locked
            if (sharedPreferencesManager.getBoolPref(packageName)) {
                if (!mPackageNames.contains(packageName)) {

                    mPackageNames.add(app.packName);

                }
            }

            //If sharedPref is altered, remove item from list
            if (!sharedPreferencesManager.getBoolPref(packageName)) {
                if (mPackageNames.contains(packageName)) {
                    mPackageNames.remove(app.packName);
                }
            }
        }

    }

    private boolean systemPackage(PackageInfo p) {
        return (p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    private Timer timer;
    private TimerTask timerTask;

    public void timer() {

        System.out.println("ENTERS TIMER");

        timer = new Timer();
        timerTask = new TimerTask() {

            String forgroundApp = "";
            String currentApp = "";

            @Override
            public void run() {
                boolean launchedActivity = false;
                forgroundApp = getForegroundPackageName();

                Update();

                System.out.println("Flag : " + flag);

                if (flag == 0) {

                    if (forgroundApp != null) {

                        if (mPackageNames.contains(forgroundApp)) {

                            flag = 1;
                            currentApp = getForegroundPackageName();


                            if (!launchedActivity) {
                                Intent intent = new Intent(mContext, LockScreen.class);
                                intent.putExtra("PACKAGE_NAME", forgroundApp);
                                intent.putExtra("APP_TITLE", APP_TITLE);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK /*| Intent.FLAG_ACTIVITY_SINGLE_TOP*/);
                                mContext.startActivity(intent);
                                System.out.println("Launching activity");
                                launchedActivity = true;
                            }

                        }

                        if (forgroundApp.equals("com.example.javaproject")) {
                            flag = 1;
                        }
                    }
                }

                if (flag == 1) {
                    if (!forgroundApp.equals("com.example.javaproject")) {
                        Update();
                        flag = 0;
                    }
                }

            }
        };
        timer.schedule(timerTask, 0, 1500);
    }


    public String getForegroundPackageName() {


        long INTERVAL = 5000;
        long end = System.currentTimeMillis();
        long begin = end - INTERVAL;

        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Service.USAGE_STATS_SERVICE);

        UsageEvents usageEvents = mUsageStatsManager.queryEvents(begin, end);

        while (usageEvents.hasNextEvent()) {

            UsageEvents.Event event = new UsageEvents.Event();

            usageEvents.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                PACKAGE_NAME = event.getPackageName();
                String[] split = PACKAGE_NAME.split("[.]");
                APP_TITLE = split[split.length - 1];

                Log.d(TAG, "PACKAGE : " + PACKAGE_NAME + ", APP : " + APP_TITLE);
            }
        }

        return PACKAGE_NAME;

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
