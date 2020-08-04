package com.example.javaproject.AppLock;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaproject.Misc.HubActivity;
import com.example.javaproject.Misc.SharedPreferencesManager;
import com.example.javaproject.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class AppLockActivity extends AppCompatActivity {

    private Switch mSwitch;
    private List<String> mInstalledApps;
    private RecyclerView mRecyclerView;
    private AppLockAdapter mAdapter;
    private List<PackageInfo> packages;
    private List<AppModel> mApps;
    private AppService mBackgroundService;
    private Intent mIntent;
    private FirebaseAuth firebaseAuth;
    SharedPreferencesManager sharedPreferencesManager;

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
        if(sharedPreferencesManager.getBoolPref("appLockEnabled")==true)
        getApps();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if( sharedPreferencesManager!= null)
        sharedPreferencesManager.setBoolPref("appLockEnabled", false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock);
        Toolbar toolbar = findViewById(R.id.toolbar_app_lock);
        toolbar.setTitle("App Lock");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();

        sharedPreferencesManager = new SharedPreferencesManager(this);

        checkPermission();
        IntializeLayout();
        boolean enabled = sharedPreferencesManager.getBoolPref("appLockEnabled");
        if (enabled) {
            mSwitch.setChecked(true);
            getApps();
        }

        mIntent = new Intent(this, AppService.class);

        mSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // boolean pref = sharedPreferencesManager.getBoolPref("appLockEnabled");
                mSwitch.setChecked(sharedPreferencesManager.getBoolPref("appLockEnabled") == null ? false : sharedPreferencesManager.getBoolPref("appLockEnabled"));
                if (sharedPreferencesManager.getBoolPref("appLockEnabled") == null || sharedPreferencesManager.getBoolPref("appLockEnabled") == false) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("App Lock Service");
                    builder.setMessage("This feature may lead to a performance decrease, do you want to continue?");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sharedPreferencesManager.setBoolPref("appLockEnabled", true);
                            mSwitch.setChecked(true);
                            getApps();
                            //Check if service is running, if it isn't then start it
                            if (!isMyServiceRunning(AppService.class)) {
                                System.out.println("RUNNING SERVICE");
                                startService(mIntent);
                            }
                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.show();

                } else {
                    sharedPreferencesManager.setBoolPref("appLockEnabled", false);
                    mSwitch.setChecked(false);
                    mApps.clear();
                    mAdapter.notifyDataSetChanged();
                    if (isMyServiceRunning(AppService.class)) {
                        System.out.println("STOPING SERVICE");
                      //  AppService appService = new AppService();

                        stopService(mIntent);
                        // stopService(mIntent);
                    }

                }
            }
        });


    }

    private void checkPermission() {
        if (needPermissionForBlocking(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enable Usage Access Settings");
            builder.setMessage("To use this feature, please enable usage access settings so we can monitor what apps are being launched.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                }
            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(AppLockActivity.this, HubActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });

            builder.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public static boolean needPermissionForBlocking(Context context) {

        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return (mode != AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }


    private void IntializeLayout() {

        mSwitch = findViewById(R.id.app_lock_switch);

        mRecyclerView = findViewById(R.id.recycler_view_apps);

        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mApps = new ArrayList<AppModel>();

        //Use custom adapter for performance so that we only load the include the number of files we need
        mAdapter = new AppLockAdapter(AppLockActivity.this, mApps);

        mRecyclerView.setAdapter(mAdapter);

        mBackgroundService = new AppService();


    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void getApps() {

        mApps.clear();
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

            mAdapter.notifyDataSetChanged();
        }
    }
}

