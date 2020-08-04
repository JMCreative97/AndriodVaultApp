package com.example.javaproject.Authentication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.widget.Toast;

import com.example.javaproject.Misc.HubActivity;


public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private Context context;

    public FingerprintHandler(Context context) {
        this.context = context;
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
                Intent intent = new Intent((Activity) context, HubActivity.class);
                context.startActivity(intent);
            }
        }
    }
}


