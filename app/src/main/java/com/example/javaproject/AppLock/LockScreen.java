package com.example.javaproject.AppLock;

import android.app.KeyguardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.javaproject.Misc.SharedPreferencesManager;
import com.example.javaproject.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LockScreen extends AppCompatActivity {

    private FingerprintManager fingerprintManager;
    private EditText pinEntry;
    private Button submit, exit;

    private KeyguardManager keyguardManager;
    private TextView error_msg_handler;
    private ImageView fingerprintImgHandler;

    private int counter;

    private FirebaseAuth firebaseAuth;
    private SharedPreferencesManager sharedPreferencesManager;

    private ImageView icon;

    private String APP_TITLE;
    private String PACKAGE_NAME;
    Drawable APP_ICON;

    @Override
    protected void onPause() {
        super.onPause();

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Intent mStartActivity = new Intent(this, LockScreen.class);
//        int mPendingIntentId = 123456;
//        PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
//        AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
//        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
//        System.exit(0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent startHomescreen = new Intent(Intent.ACTION_MAIN);
        startHomescreen.addCategory(Intent.CATEGORY_HOME);
        startHomescreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startHomescreen);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);


        pinEntry = findViewById(R.id.lock_screen_pin_sign_in);
        submit = findViewById(R.id.btn_lock_screen_submit);
        exit = findViewById(R.id.btn_lock_screen_cancel);
        icon = findViewById(R.id.lock_screen_img);
        //error_msg_handler = findViewById(R.id.login_error_handler);
        counter = 0;

        System.out.println("LOCK SCREEN ACTIVITY LAUNCHED");

        APP_TITLE = getIntent().getStringExtra("APP_TITLE");
        PACKAGE_NAME = getIntent().getStringExtra("PACKAGE_NAME");
        if (PACKAGE_NAME != null) {
            try {
                CheckVersion();
                APP_ICON = getApplicationContext().getPackageManager().getApplicationIcon(PACKAGE_NAME);
                icon.setImageDrawable(APP_ICON);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }else{
            finish();
        }

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startHomescreen = new Intent(Intent.ACTION_MAIN);
                startHomescreen.addCategory(Intent.CATEGORY_HOME);
                startHomescreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(startHomescreen);
                finish();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(TextUtils.isEmpty(pinEntry.getText().toString()) && pinEntry.getText().toString().length() < 4)) {
                    authUser(pinEntry.getText().toString().trim());
                    pinEntry.setText("");
                } else {
                    pinEntry.setError("Please enter at least a 4 digit pin");
                }
            }


        });

    }

    private void authUser(String password) {
        firebaseAuth = FirebaseAuth.getInstance();
        sharedPreferencesManager = new SharedPreferencesManager(this);
        firebaseAuth.signInWithEmailAndPassword(sharedPreferencesManager.getStringPref("Email"), password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                pinEntry.setText("");
                sharedPreferencesManager.setBoolPref(PACKAGE_NAME, false);
                Toast.makeText(LockScreen.this, "LOGIN SUCCESSFUL", Toast.LENGTH_SHORT).show();
                Intent intent = getPackageManager().getLaunchIntentForPackage(PACKAGE_NAME);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pinEntry.setText("");
                // counter++;
                // Toast.makeText(LockScreen.this, "Login failed", Toast.LENGTH_SHORT).show();
                Intent startHomescreen = new Intent(Intent.ACTION_MAIN);
                startHomescreen.addCategory(Intent.CATEGORY_HOME);
                startHomescreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(startHomescreen);
            }
        });
    }

    private void CheckVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            System.out.println(PACKAGE_NAME);
            AppLockFingerprintHandler fingerprintHandler = new AppLockFingerprintHandler(this, PACKAGE_NAME);
            fingerprintHandler.authentication(fingerprintManager, null);
        }
    }

}

