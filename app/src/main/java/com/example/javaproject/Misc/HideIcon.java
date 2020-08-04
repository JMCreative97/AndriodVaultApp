package com.example.javaproject.Misc;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.javaproject.Authentication.LoginActivity;
import com.example.javaproject.BuildConfig;
import com.example.javaproject.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class HideIcon extends AppCompatActivity {

    private ImageView current_icon, calender_icon, camera_icon,
            calculator_icon, clock_icon, mountain_icon, settings_icon;
    private SharedPreferencesManager sharedPreferencesManager;
    private Button save_button;
    String currently_selected;
    Integer current_icon_int;
    private PackageManager mPackageManager;
    private ActivityManager mActivityManager;


    //Logout the user if they exit the app
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth != null){
            firebaseAuth.signOut();
        }
        Intent intent = new Intent(HideIcon.this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hide_icon);
        Toolbar toolbar = findViewById(R.id.toolbar_hide_icons);
        toolbar.setTitle("Hide Icon");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        sharedPreferencesManager = new SharedPreferencesManager(this);
        mActivityManager = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
        mPackageManager = getPackageManager();

        if (!sharedPreferencesManager.getBoolPref("DISABLED_ICONS")) {
            disableIcons();
        }

        current_icon = findViewById(R.id.current_icon);

        if (sharedPreferencesManager.getIntPref("CURRENT_ICON_INT") != 0) {
            System.out.println(sharedPreferencesManager.getIntPref("CURRENT_ICON_INT"));
            updateCurrentIcon((sharedPreferencesManager.getIntPref("CURRENT_ICON_INT")));
        }
        ;

        calender_icon = findViewById(R.id.calender_icon);
        calender_icon.setImageResource(R.mipmap.calender_icon);
        calender_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current_icon_int = R.mipmap.calender_icon;
                updateCurrentIcon(R.mipmap.calender_icon);
                currently_selected = "com.example.javaproject.calender_icon";
                System.out.println(currently_selected);

            }
        });

        camera_icon = findViewById(R.id.camera_icon);
        camera_icon.setImageResource(R.mipmap.camera_icon);
        camera_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current_icon_int = R.mipmap.camera_icon;
                updateCurrentIcon(R.mipmap.camera_icon);
                currently_selected = "com.example.javaproject.camera_icon";
                System.out.println(currently_selected);
            }
        });

        calculator_icon = findViewById(R.id.calculator_icon);
        calculator_icon.setImageResource(R.mipmap.calculator_icon);
        calculator_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current_icon_int = R.mipmap.calculator_icon;
                updateCurrentIcon(R.mipmap.calculator_icon);
                currently_selected = "com.example.javaproject.calculator_icon";
                System.out.println(currently_selected);
            }
        });

        clock_icon = findViewById(R.id.clock_icon);
        clock_icon.setImageResource(R.mipmap.clock_icon);
        clock_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current_icon_int = R.mipmap.clock_icon;
                updateCurrentIcon(R.mipmap.clock_icon);
                currently_selected = "com.example.javaproject.clock_icon";
                System.out.println(currently_selected);
            }
        });

        mountain_icon = findViewById(R.id.default_icon);
        mountain_icon.setImageResource(R.mipmap.default_icon);
        mountain_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current_icon_int = R.mipmap.mountain_icon;
                updateCurrentIcon(R.mipmap.mountain_icon);
                currently_selected = "com.example.javaproject.defualt_icon";
                System.out.println(currently_selected);
            }
        });

        settings_icon = findViewById(R.id.settings_icon);
        settings_icon.setImageResource(R.mipmap.settings_icon);
        settings_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current_icon_int = R.mipmap.settings_icon;
                updateCurrentIcon(R.mipmap.settings_icon);
                currently_selected = "com.example.javaproject.settings_icon";
                System.out.println(currently_selected);
            }
        });

        save_button = findViewById(R.id.save_button);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferencesManager.getStringPref("CURRENT_ICON_STR") == null) {

                    //Disable app icon
                    mPackageManager.setComponentEnabledSetting(
                            new ComponentName(BuildConfig.APPLICATION_ID, "com.example.javaproject.default_icon"),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
                    );

                    //Enable chosen app icon
                    mPackageManager.setComponentEnabledSetting(
                            new ComponentName(BuildConfig.APPLICATION_ID, currently_selected),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
                    );

                    sharedPreferencesManager.setStringPref("CURRENT_ICON_STR", currently_selected);
                    sharedPreferencesManager.setIntPref("CURRENT_ICON_INT", current_icon_int);

                    //Find launcher and kill
                    Intent intent = new Intent(Intent.ACTION_MAIN);

                    intent.addCategory(Intent.CATEGORY_HOME);

                    intent.addCategory(Intent.CATEGORY_DEFAULT);

                    List<ResolveInfo> resolves = mPackageManager.queryIntentActivities(intent, 0);

                    for (ResolveInfo res : resolves) {
                        if (res.activityInfo != null) {
                            mActivityManager.killBackgroundProcesses(res.activityInfo.packageName);
                        }
                    }

                } else {

                    if (sharedPreferencesManager.getStringPref("CURRENT_ICON_STR") == currently_selected) {
                        Toast.makeText(HideIcon.this, "Icon already present", Toast.LENGTH_SHORT).show();
                    } else {

                        System.out.println(sharedPreferencesManager.getStringPref("CURRENT_ICON_STR"));
                        System.out.println(sharedPreferencesManager.getIntPref("CURRENT_ICON_INT"));


                        //Disable app icon
                        mPackageManager.setComponentEnabledSetting(
                                new ComponentName(BuildConfig.APPLICATION_ID, sharedPreferencesManager.getStringPref("CURRENT_ICON_STR")),
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
                        );

                        //Enable chosen app icon
                        mPackageManager.setComponentEnabledSetting(
                                new ComponentName(BuildConfig.APPLICATION_ID, currently_selected),
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
                        );

                        sharedPreferencesManager.setStringPref("CURRENT_ICON_STR", currently_selected);
                        sharedPreferencesManager.setIntPref("CURRENT_ICON_INT", current_icon_int);

                        //Find launcher and kill
                        Intent killLauncher = new Intent(Intent.ACTION_MAIN);

                        killLauncher.addCategory(Intent.CATEGORY_HOME);

                        killLauncher.addCategory(Intent.CATEGORY_DEFAULT);

                        List<ResolveInfo> resolves = mPackageManager.queryIntentActivities(killLauncher, 0);
                        for (ResolveInfo resolveInfo : resolves) {
                            if (resolveInfo.activityInfo != null) {
                                mActivityManager.killBackgroundProcesses(resolveInfo.activityInfo.packageName);
                            }
                        }

                        Toast.makeText(HideIcon.this, "Icon Changed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void updateCurrentIcon(Integer icon) {
        current_icon.setImageResource(icon);
    }

    //Disable all alias' apart from default, only happens once
    private void disableIcons() {
        sharedPreferencesManager.setBoolPref("DISABLED_ICONS", true);
        String[] activityAlias = {
                "com.example.javaproject.calender_icon",
                "com.example.javaproject.camera_icon",
                "com.example.javaproject.calculator_icon",
                "com.example.javaproject.clock_icon",
                "com.example.javaproject.mountain_icon",
                "com.example.javaproject.settings_icon"
        };

        for (String alias : activityAlias) {
            mPackageManager.setComponentEnabledSetting(
                    new ComponentName(BuildConfig.APPLICATION_ID, alias),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            );
        }
    }


}
