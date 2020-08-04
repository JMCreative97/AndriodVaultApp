package com.example.javaproject.AppLock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.widget.Toast;

import com.example.javaproject.Misc.SharedPreferencesManager;


public class AppLockFingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private Context context;
    private String PACKAGE_NAME;
    private SharedPreferencesManager sharedPreferencesManager;

    public AppLockFingerprintHandler(Context context, String PACKAGE_NAME) {
        System.out.println(PACKAGE_NAME);
        this.context = context;
        sharedPreferencesManager = new SharedPreferencesManager(context);
        this.PACKAGE_NAME = PACKAGE_NAME;
    }

    public void authentication(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject) {

        CancellationSignal cancellationSignal = new CancellationSignal();

        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);

    }


    //Failed fingerprint authentication
    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        this.updateFingerprintHandlers("", 0);
    }

    //Successful fingerprint authentication
    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        this.updateFingerprintHandlers("", 1);
    }

    public void updateFingerprintHandlers(CharSequence error, Integer num) {
        switch (num) {
            case (0): {
//                Toast.makeText((Activity) context, "FINGERPRINT AUTHENTICATION FAILED", Toast.LENGTH_SHORT).show();
//                System.out.println("FINGERPRINT ERROR : " + error);
            }
            case (1): {
                Toast.makeText((Activity) context, "LOGIN SUCCESSFUL", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent((Activity)context, HubActivity.class);
//                context.startActivity(intent);

                sharedPreferencesManager.setBoolPref(PACKAGE_NAME, false);
                Toast.makeText((Activity) context, "LOGIN SUCCESSFUL", Toast.LENGTH_SHORT).show();
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(PACKAGE_NAME);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
               // ((Activity) context).finish();

            }
        }
    }
}


